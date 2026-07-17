package com.flexpoint.plugin.selector.cache;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.selector.SelectionResult;
import com.flexpoint.core.selector.Selector;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 官方插件：缓存选择器（装饰器）。
 *
 * <p>包装另一个 {@link Selector delegate}，以「候选 extId 集合（+ 可选业务 key）」为 key
 * 缓存 {@link SelectionResult}。命中缓存直接返回 delegate 结果，避免重复选择计算。</p>
 *
 * <p>默认缓存 key 仅由候选集合（extId 排序拼接）构成；若业务的选择结果随请求维度（如租户/uid）
 * 变化，请提供 {@link CacheKeyResolver} 将该维度纳入 key，避免不同请求命中同一缓存项。
 * TTL 通过构造参数 {@code ttlMillis} 控制，{@code <= 0} 表示永不过期。
 * 选择器名称默认沿用 delegate 名称，也可构造传入。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public class CachingSelector implements Selector {

    private final Selector delegate;
    private final long ttlMillis;
    private final String name;
    private final CacheKeyResolver keyResolver;
    private final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<>();

    /** 永不过期，名称沿用 delegate。 */
    public CachingSelector(Selector delegate) {
        this(delegate, 0L);
    }

    /** 指定 TTL，名称沿用 delegate。 */
    public CachingSelector(Selector delegate, long ttlMillis) {
        this(delegate, ttlMillis, null);
    }

    /** 指定 TTL 与名称。 */
    public CachingSelector(Selector delegate, long ttlMillis, String name) {
        this(delegate, ttlMillis, name, null);
    }

    /**
     * @param delegate    被包装的选择器（不可为空）
     * @param ttlMillis   缓存有效期毫秒，{@code <= 0} 表示永不过期
     * @param name        选择器名称，为空时沿用 {@code delegate.getName()}
     * @param keyResolver 业务维度 key 解析器，可为 {@code null}（则缓存 key 仅取候选集合）
     */
    public CachingSelector(Selector delegate, long ttlMillis, String name, CacheKeyResolver keyResolver) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.ttlMillis = ttlMillis;
        this.name = (name != null && !name.isEmpty()) ? name : delegate.getName();
        this.keyResolver = keyResolver;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ExtAbility> SelectionResult<T> select(List<T> candidates) {
        String key = buildKey(candidates);
        long now = System.currentTimeMillis();

        Entry entry = cache.get(key);
        if (entry != null && !entry.isExpired(now)) {
            log.debug("[{}] 缓存命中: key={}", name, key);
            return (SelectionResult<T>) entry.result;
        }

        SelectionResult<T> result = delegate.select(candidates);
        cache.put(key, new Entry(result, expireAt(now)));
        log.debug("[{}] 缓存未命中，调用 delegate 并写入: key={}", name, key);
        return result;
    }

    /** 清空缓存。 */
    public void invalidate() {
        cache.clear();
    }

    /** 当前缓存条目数（用于诊断/测试）。 */
    public int cacheSize() {
        return cache.size();
    }

    private long expireAt(long now) {
        return ttlMillis <= 0 ? Long.MAX_VALUE : now + ttlMillis;
    }

    /** 构造缓存 key：可选业务维度 key + 候选集合（extId 排序拼接）。 */
    private <T extends ExtAbility> String buildKey(List<T> candidates) {
        StringBuilder sb = new StringBuilder();
        if (keyResolver != null) {
            sb.append(keyResolver.resolveKey()).append('#');
        }
        List<String> extIds = new ArrayList<>();
        if (candidates != null) {
            for (T c : candidates) {
                extIds.add(c == null ? "null" : c.getExtId());
            }
        }
        java.util.Collections.sort(extIds);
        sb.append(extIds);
        return sb.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    /** 业务方实现用于将请求维度纳入缓存 key 的接口（可选）。 */
    public interface CacheKeyResolver {
        String resolveKey();
    }

    /** 缓存条目：结果 + 过期时间戳。 */
    private static final class Entry {
        final SelectionResult<?> result;
        final long expireAt;

        Entry(SelectionResult<?> result, long expireAt) {
            this.result = result;
            this.expireAt = expireAt;
        }

        boolean isExpired(long now) {
            return now >= expireAt;
        }
    }
}
