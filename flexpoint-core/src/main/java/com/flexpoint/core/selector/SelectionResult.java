package com.flexpoint.core.selector;

import com.flexpoint.core.ext.ExtAbility;

import java.util.List;

/**
 * 选择结果：一次选择同时承载「命中实现 + 决策解释 + 结论」。
 *
 * <p>统一选择语义（避免 null / 异常两条通道分散）：结论由 {@link DecisionExplanation.Outcome}
 * 表达（HIT/MISS/AMBIGUOUS）；命中实现仅在 HIT 时非空。决策解释随结果一并产出，
 * 避免 select 与 explain 重复过滤。</p>
 *
 * @param <T> 扩展点类型
 * @author xiangganluo
 */
public final class SelectionResult<T extends ExtAbility> {

    private final T selected;
    private final DecisionExplanation explanation;

    private SelectionResult(T selected, DecisionExplanation explanation) {
        this.selected = selected;
        this.explanation = explanation;
    }

    // =============== 工厂 ===============

    /** 命中：selected 非空，explanation 为 HIT。 */
    public static <T extends ExtAbility> SelectionResult<T> hit(T selected, DecisionExplanation explanation) {
        return new SelectionResult<>(selected, explanation);
    }

    /** 未命中。 */
    public static <T extends ExtAbility> SelectionResult<T> miss(DecisionExplanation explanation) {
        return new SelectionResult<>(null, explanation);
    }

    /** 歧义：多候选未收敛。 */
    public static <T extends ExtAbility> SelectionResult<T> ambiguous(DecisionExplanation explanation) {
        return new SelectionResult<>(null, explanation);
    }

    /**
     * 便捷工厂：由一次选择结果推导（{@code selected != null} → HIT，否则 MISS）。
     * <p>供直接实现 {@link Selector} 的选择器一行返回结果，无需自行构造解释。</p>
     */
    public static <T extends ExtAbility> SelectionResult<T> of(String selectorName, List<T> candidates, T selected) {
        return new SelectionResult<>(selected, DecisionExplanation.fromSelection(selectorName, candidates, selected));
    }

    // =============== 访问器 ===============

    /** 命中的实现（仅 HIT 时非空）。 */
    public T getSelected() {
        return selected;
    }

    public DecisionExplanation getExplanation() {
        return explanation;
    }

    public DecisionExplanation.Outcome getOutcome() {
        return explanation.getOutcome();
    }

    public boolean isHit() {
        return explanation.getOutcome() == DecisionExplanation.Outcome.HIT;
    }

    public boolean isMiss() {
        return explanation.getOutcome() == DecisionExplanation.Outcome.MISS;
    }

    public boolean isAmbiguous() {
        return explanation.getOutcome() == DecisionExplanation.Outcome.AMBIGUOUS;
    }
}
