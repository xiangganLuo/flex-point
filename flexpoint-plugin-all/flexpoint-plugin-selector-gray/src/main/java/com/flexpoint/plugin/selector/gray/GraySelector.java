package com.flexpoint.plugin.selector.gray;

import com.flexpoint.core.context.FlexPointContext;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtTags;
import com.flexpoint.core.selector.AbstractSelector;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 官方插件：灰度选择器。
 *
 * <p>对灰度 key（默认取 {@link FlexPointContext#getUid()}）做稳定哈希取模 100，
 * 结果 {@code < percentage} 视为命中灰度：过滤出带灰度标记的候选
 * （tag {@code gray == true} 或 tag {@code group == "gray"}）；否则过滤出非灰度候选。</p>
 *
 * <p>哈希基于字符串字节，跨 JVM 稳定且确定性，便于灰度分流可复现。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public class GraySelector extends AbstractSelector {

    /** 选择器名称。 */
    public static final String NAME = "graySelector";

    private final int percentage;
    private final Function<FlexPointContext, String> keyProvider;

    /**
     * 默认以 uid 作为灰度 key。
     *
     * @param percentage 灰度比例（0-100，越界自动裁剪）
     */
    public GraySelector(int percentage) {
        this(percentage, FlexPointContext::getUid);
    }

    /**
     * @param percentage  灰度比例（0-100，越界自动裁剪）
     * @param keyProvider 从上下文提取灰度 key 的函数（如 {@code ctx -> ctx.getLabel("deviceId")}）
     */
    public GraySelector(int percentage, Function<FlexPointContext, String> keyProvider) {
        this.percentage = Math.max(0, Math.min(100, percentage));
        this.keyProvider = keyProvider != null ? keyProvider : FlexPointContext::getUid;
    }

    /** 以指定 label 作为灰度 key 的便捷 keyProvider。 */
    public static Function<FlexPointContext, String> byLabel(String labelKey) {
        return ctx -> ctx.getLabel(labelKey);
    }

    @Override
    protected <T extends ExtAbility> List<T> filter(List<T> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        boolean grayHit = isGrayHit();
        List<T> result = candidates.stream()
                .filter(ext -> isGray(ext.getTags()) == grayHit)
                .collect(Collectors.toList());
        log.debug("[{}] grayHit={}, percentage={}, 候选={}, 命中={}", NAME, grayHit, percentage,
                candidates.size(), result.size());
        return result;
    }

    /** 计算当前上下文是否落入灰度区间。 */
    private boolean isGrayHit() {
        String key = keyProvider.apply(FlexPointContext.current());
        if (key == null || key.isEmpty()) {
            return false;
        }
        int bucket = Math.floorMod(stableHash(key), 100);
        return bucket < percentage;
    }

    /** 候选是否带灰度标记。 */
    private boolean isGray(ExtTags tags) {
        if (tags == null) {
            return false;
        }
        if (Boolean.TRUE.equals(tags.getBoolean("gray"))) {
            return true;
        }
        return "gray".equals(tags.getString("group"));
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
