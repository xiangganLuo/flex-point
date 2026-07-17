package com.flexpoint.plugin.selector.cache;

import com.flexpoint.common.context.FlexPointContext;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.selector.SelectionResult;
import com.flexpoint.core.selector.Selector;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 官方插件：缓存选择器（装饰器）。
 *
 * <p>包装另一个 {@link Selector delegate}，以「上下文关键字段 + 候选 extId 集合」为 key
 * 缓存 {@link SelectionResult}。命中缓存直接返回 delegate 结果，避免重复选择计算。</p>
 *
 * <p>缓存 key 反映上下文差异（tenantId/appCode/version/uid + labels）与候选集合（extId 排序拼接），
 * 因此不同上下文或不同候选会得到不同缓存项。TTL 通过构造参数 {@code ttlMillis} 控制，
 * {@code <= 0} 表示永不过期。选择器名称默认沿用 delegate 名称，也可构造传入。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public class CachingSelector implements Selector {

    private final Selector delegate;
    private final long ttlMillis;
    private final String name;
    private final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<>();

    /** 永不过期，名称沿用 delegate。 */
    public CachingSelector(Selector delegate) {
        this(delegate, 0L);
    }

    /** 指定 TTL，名称沿用 delegate。 */
    public CachingSelector(Selector delegate, long ttlMillis) {
        this(delegate, ttlMillis, null);
    }

    /**
     * @param delegate   被包装的选择器（不可为空）
     * @param ttlMillis  缓存有效期毫秒，{@code <= 0} 表示永不过期
     * @param name       选择器名称，为空时沿用 {@code delegate.getName()}
     */
    public CachingSelector(Selector delegate, long ttlMillis, String name) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.ttlMillis = ttlMillis;
        this.name = (name != null && !name.isEmpty()) ? name : delegate.getName();
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

    /** 构造反映上下文与候选集合差异的缓存 key。 */
    private <T extends ExtAbility> String buildKey(List<T> candidates) {
        FlexPointContext ctx = FlexPointContext.current();
        StringBuilder sb = new StringBuilder();
        sb.append(ctx.getTenantId()).append('|')
                .append(ctx.getAppCode()).append('|')
                .append(ctx.getVersion()).append('|')
                .append(ctx.getUid()).append('|');
        // labels 有序拼接，避免 HashMap 迭代顺序影响 key
        Map<String, String> sortedLabels = new TreeMap<>(ctx.getLabels());
        sb.append(sortedLabels).append('#');
        // 候选 extId 排序拼接
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
