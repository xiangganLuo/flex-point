package com.flexpoint.core.selector;

import com.flexpoint.core.ext.ExtAbility;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 选择器抽象基类
 * 提供模板方法，子类只需实现具体的过滤逻辑 {@link #filter(List)}。
 *
 * <p>选择语义：过滤后空→MISS，唯一→HIT，多个→AMBIGUOUS（不在此抛异常，
 * 由上层根据结论决定如何处理，便于后续引入多候选收敛策略）。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
@Slf4j
public abstract class AbstractSelector implements Selector {

    @Override
    public <T extends ExtAbility> SelectionResult<T> select(List<T> candidates) {
        int total = candidates == null ? 0 : candidates.size();
        List<T> filtered = filter(candidates);
        log.debug("选择器[{}]过滤: 候选={}, 命中={}", getName(), total, filtered.size());

        if (filtered.isEmpty()) {
            log.debug("选择器[{}]未命中任何候选", getName());
            return SelectionResult.miss(DecisionExplanation.miss(getName(), candidates, "无候选通过过滤"));
        }
        if (filtered.size() == 1) {
            T selected = filtered.get(0);
            log.debug("选择器[{}]命中唯一候选: extId={}", getName(), selected.getExtId());
            return SelectionResult.hit(selected, DecisionExplanation.hit(getName(), candidates, filtered, selected.getExtId()));
        }
        log.debug("选择器[{}]命中多个候选({})，判定为歧义", getName(), filtered.size());
        return SelectionResult.ambiguous(DecisionExplanation.ambiguous(getName(), candidates, filtered));
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
