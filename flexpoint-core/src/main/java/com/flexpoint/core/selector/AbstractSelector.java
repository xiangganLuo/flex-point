package com.flexpoint.core.selector;

import com.flexpoint.common.exception.MultipleExtMatchedException;
import com.flexpoint.core.ext.ExtAbility;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 选择器抽象基类
 * 提供模板方法，子类实现具体的过滤逻辑
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
@Slf4j
public abstract class AbstractSelector implements Selector {

    @Override
    public <T extends ExtAbility> T select(List<T> candidates) {
        int total = candidates == null ? 0 : candidates.size();
        List<T> filtered = filter(candidates);
        log.debug("选择器[{}]过滤: 候选={}, 命中={}", getName(), total, filtered.size());

        if (filtered.isEmpty()) {
            log.debug("选择器[{}]未命中任何候选", getName());
            return null;
        }

        if (filtered.size() == 1) {
            log.debug("选择器[{}]命中唯一候选: extId={}", getName(), filtered.get(0).getExtId());
            return filtered.get(0);
        }

        // 有多个匹配结果，抛出专门的异常
        log.debug("选择器[{}]命中多个候选({})，判定为歧义", getName(), filtered.size());
        throw new MultipleExtMatchedException(getName(), filtered.size());
    }

    /**
     * 基于过滤链路产出决策解释：命中 / 未命中 / 歧义。
     */
    @Override
    public <T extends ExtAbility> DecisionExplanation explain(List<T> candidates) {
        List<T> filtered = filter(candidates);
        if (filtered.isEmpty()) {
            return DecisionExplanation.miss(getName(), candidates, "无候选通过过滤");
        }
        if (filtered.size() == 1) {
            return DecisionExplanation.hit(getName(), candidates, filtered, filtered.get(0).getExtId());
        }
        return DecisionExplanation.ambiguous(getName(), candidates, filtered);
    }

    /**
     * 从候选列表中过滤匹配的扩展点
     *
     * @param candidates 候选扩展点列表
     * @param <T> 扩展点类型
     * @return 匹配的扩展点列表
     */
    protected abstract <T extends ExtAbility> List<T> filter(List<T> candidates);

}
