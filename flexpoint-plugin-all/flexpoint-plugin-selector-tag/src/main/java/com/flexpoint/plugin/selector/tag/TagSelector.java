package com.flexpoint.plugin.selector.tag;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtTags;
import com.flexpoint.core.selector.AbstractSelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 官方插件：标签选择器。
 *
 * <p>按业务方 {@link LabelResolver} 提供的 labels 与候选扩展点的 {@link ExtAbility#getTags()} 做匹配：
 * labels 的<b>所有</b>键值都能在候选的 tags 中命中者入选（相当于 AND 语义）。</p>
 *
 * <p>路由所需的 labels 由业务方实现 {@link LabelResolver} 提供（如从请求头/线程变量读取）；
 * labels 为空视为无标签路由诉求，直接返回空候选（MISS）。</p>
 *
 * @author xiangganluo
 */
@Slf4j
@RequiredArgsConstructor
public class TagSelector extends AbstractSelector {

    /** 选择器名称。 */
    public static final String NAME = "tagSelector";

    protected final LabelResolver resolver;

    @Override
    protected <T extends ExtAbility> List<T> filter(List<T> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        if (resolver == null) {
            throw new IllegalStateException(NAME + " 的 LabelResolver 不能为空，请注册业务实现！");
        }
        Map<String, String> labels = resolver.resolveLabels();
        if (labels == null || labels.isEmpty()) {
            log.debug("[{}] labels 为空，返回空候选", NAME);
            return Collections.emptyList();
        }
        List<T> result = candidates.stream()
                .filter(ext -> matchesAll(ext.getTags(), labels))
                .collect(Collectors.toList());
        log.debug("[{}] 按 labels={} 过滤: 候选={}, 命中={}", NAME, labels, candidates.size(), result.size());
        return result;
    }

    /**
     * 判断候选 tags 是否命中全部 label 键值。
     */
    private boolean matchesAll(ExtTags tags, Map<String, String> labels) {
        if (tags == null) {
            return false;
        }
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            if (!entry.getValue().equals(tags.getString(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    /** 业务方实现用于解析当前路由 labels 的接口。 */
    public interface LabelResolver {
        Map<String, String> resolveLabels();
    }
}
