package com.flexpoint.plugin.selector.ab;

import com.flexpoint.common.context.FlexPointContext;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.selector.AbstractSelector;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 官方插件：A/B 分流选择器。
 *
 * <p>依据桶配置（如 {@code {A:50, B:50}}）对分流 key（默认取 {@link FlexPointContext#getUid()}）
 * 做稳定哈希落桶，再过滤出 tag {@code bucket} 等于命中桶的候选。</p>
 *
 * <p>桶按名称排序后累加权重划分区间，保证分流结果确定且可复现。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public class AbTestSelector extends AbstractSelector {

    /** 选择器名称。 */
    public static final String NAME = "abSelector";

    /** 候选桶标签键。 */
    public static final String BUCKET_TAG = "bucket";

    private final TreeMap<String, Integer> buckets;
    private final int totalWeight;
    private final Function<FlexPointContext, String> keyProvider;

    /**
     * 默认以 uid 作为分流 key。
     *
     * @param buckets 桶配置：桶名 -> 权重（权重需为正）
     */
    public AbTestSelector(Map<String, Integer> buckets) {
        this(buckets, FlexPointContext::getUid);
    }

    /**
     * @param buckets     桶配置：桶名 -> 权重（权重需为正）
     * @param keyProvider 从上下文提取分流 key 的函数
     */
    public AbTestSelector(Map<String, Integer> buckets, Function<FlexPointContext, String> keyProvider) {
        this.buckets = new TreeMap<>();
        int sum = 0;
        if (buckets != null) {
            for (Map.Entry<String, Integer> e : buckets.entrySet()) {
                int w = e.getValue() == null ? 0 : e.getValue();
                if (e.getKey() != null && w > 0) {
                    this.buckets.put(e.getKey(), w);
                    sum += w;
                }
            }
        }
        this.totalWeight = sum;
        this.keyProvider = keyProvider != null ? keyProvider : FlexPointContext::getUid;
    }

    /** 以指定 label 作为分流 key 的便捷 keyProvider。 */
    public static Function<FlexPointContext, String> byLabel(String labelKey) {
        return ctx -> ctx.getLabel(labelKey);
    }

    @Override
    protected <T extends ExtAbility> List<T> filter(List<T> candidates) {
        if (candidates == null || candidates.isEmpty() || totalWeight <= 0) {
            return Collections.emptyList();
        }
        String bucket = resolveBucket();
        if (bucket == null) {
            log.debug("[{}] 无法确定分流桶（key 为空），返回空候选", NAME);
            return Collections.emptyList();
        }
        List<T> result = candidates.stream()
                .filter(ext -> bucket.equals(ext.getTags().getString(BUCKET_TAG)))
                .collect(Collectors.toList());
        log.debug("[{}] 命中桶={}, 候选={}, 命中={}", NAME, bucket, candidates.size(), result.size());
        return result;
    }

    /** 计算当前上下文命中的桶名。 */
    private String resolveBucket() {
        String key = keyProvider.apply(FlexPointContext.current());
        if (key == null || key.isEmpty()) {
            return null;
        }
        int pos = Math.floorMod(stableHash(key), totalWeight);
        int cumulative = 0;
        for (Map.Entry<String, Integer> e : buckets.entrySet()) {
            cumulative += e.getValue();
            if (pos < cumulative) {
                return e.getKey();
            }
        }
        // 理论不可达；兜底返回最后一个桶
        return buckets.lastKey();
    }

    /** 稳定哈希（等价于 String.hashCode 的显式实现，保证语义固定）。 */
    private static int stableHash(String key) {
        int h = 0;
        for (int i = 0; i < key.length(); i++) {
            h = 31 * h + key.charAt(i);
        }
        return h;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
