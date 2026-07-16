package com.flexpoint.plugin.selector.tag;

import com.flexpoint.core.context.FlexPointContext;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtTags;
import com.flexpoint.core.selector.AbstractSelector;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 官方插件：标签选择器。
 *
 * <p>按 {@link FlexPointContext#getLabels()} 与候选扩展点的 {@link ExtAbility#getTags()} 做匹配：
 * 当前上下文的<b>所有</b> label 键值都能在候选的 tags 中命中者入选（相当于 AND 语义）。</p>
 *
 * <p>路由信息完全来自标准上下文，无需业务方编写 Resolver，配合接入层填充 labels 即可「配置即装配」。
 * 若上下文没有任何 label，则视为无标签路由诉求，直接返回空候选（MISS）。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public class TagSelector extends AbstractSelector {

    /** 选择器名称。 */
    public static final String NAME = "tagSelector";

    @Override
    protected <T extends ExtAbility> List<T> filter(List<T> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, String> labels = FlexPointContext.current().getLabels();
        if (labels == null || labels.isEmpty()) {
            log.debug("[{}] 上下文无 label，返回空候选", NAME);
            return Collections.emptyList();
        }
        List<T> result = candidates.stream()
                .filter(ext -> matchesAll(ext.getTags(), labels))
                .collect(Collectors.toList());
        log.debug("[{}] 按 labels={} 过滤: 候选={}, 命中={}", NAME, labels, candidates.size(), result.size());
        return result;
    }

    /**
     * 判断候选 tags 是否命中上下文的全部 label 键值。
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
}
