package com.flexpoint.plugin.selector.gray;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtTags;
import com.flexpoint.core.selector.AbstractSelector;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 官方插件：灰度选择器。
 *
 * <p>对业务方 {@link GrayKeyResolver} 提供的灰度 key 做稳定哈希取模 100，
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
    private final GrayKeyResolver resolver;

    /**
     * @param percentage 灰度比例（0-100，越界自动裁剪）
     * @param resolver   业务方提供灰度 key 的实现（不可为空）
     */
    public GraySelector(int percentage, GrayKeyResolver resolver) {
        this.percentage = Math.max(0, Math.min(100, percentage));
        this.resolver = resolver;
    }

    @Override
    protected <T extends ExtAbility> List<T> filter(List<T> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        boolean grayHit = isGrayHit();
        List<T> result = candidates.stream()
                .filter(ext -> isGray(ext.getTags()) == grayHit)
                .collect(Collectors.toList());
        log.debug("[{}] grayHit={}, percentage={}, 候选={}, 命中={}", NAME, grayHit, percentage,
                candidates.size(), result.size());
        return result;
    }

    /** 计算当前请求是否落入灰度区间。 */
    private boolean isGrayHit() {
        if (resolver == null) {
            throw new IllegalStateException(NAME + " 的 GrayKeyResolver 不能为空，请注册业务实现！");
        }
        String key = resolver.resolveKey();
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

    /** 业务方实现用于解析灰度 key（如 uid/deviceId）的接口。 */
    public interface GrayKeyResolver {
        String resolveKey();
    }
}
