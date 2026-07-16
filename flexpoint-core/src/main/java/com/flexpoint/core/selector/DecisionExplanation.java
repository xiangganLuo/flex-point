package com.flexpoint.core.selector;

import com.flexpoint.core.ext.ExtAbility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 选择决策解释（v1，调试级）。
 *
 * <p>用于回答「为什么命中 / 为什么未命中」：记录候选快照、过滤后的候选、
 * 命中目标与结论原因。默认在 Debug 级别输出，便于本地与预发排查路由问题。</p>
 *
 * <p>不可变值对象；由选择器在 {@link Selector#explain(List)} 中产出。</p>
 *
 * @author xiangganluo
 */
public final class DecisionExplanation {

    /** 决策结论。 */
    public enum Outcome {
        /** 命中唯一候选 */
        HIT,
        /** 无候选通过过滤 */
        MISS,
        /** 命中多个候选（歧义） */
        AMBIGUOUS
    }

    private final String selectorName;
    private final List<String> candidateExtIds;
    private final List<String> filteredExtIds;
    private final String selectedExtId;
    private final Outcome outcome;
    private final String reason;

    private DecisionExplanation(String selectorName,
                                List<String> candidateExtIds,
                                List<String> filteredExtIds,
                                String selectedExtId,
                                Outcome outcome,
                                String reason) {
        this.selectorName = selectorName;
        this.candidateExtIds = Collections.unmodifiableList(candidateExtIds);
        this.filteredExtIds = Collections.unmodifiableList(filteredExtIds);
        this.selectedExtId = selectedExtId;
        this.outcome = outcome;
        this.reason = reason;
    }

    // =============== 工厂方法 ===============

    /** 命中：唯一候选被选中。 */
    public static DecisionExplanation hit(String selectorName,
                                          List<? extends ExtAbility> candidates,
                                          List<? extends ExtAbility> filtered,
                                          String selectedExtId) {
        return new DecisionExplanation(selectorName, extIds(candidates), extIds(filtered),
                selectedExtId, Outcome.HIT, "命中");
    }

    /** 未命中：无候选通过过滤。 */
    public static DecisionExplanation miss(String selectorName,
                                           List<? extends ExtAbility> candidates,
                                           String reason) {
        return new DecisionExplanation(selectorName, extIds(candidates), Collections.<String>emptyList(),
                null, Outcome.MISS, reason);
    }

    /** 歧义：多个候选通过过滤。 */
    public static DecisionExplanation ambiguous(String selectorName,
                                                List<? extends ExtAbility> candidates,
                                                List<? extends ExtAbility> filtered) {
        int size = filtered == null ? 0 : filtered.size();
        return new DecisionExplanation(selectorName, extIds(candidates), extIds(filtered),
                null, Outcome.AMBIGUOUS, "命中多个候选(" + size + ")");
    }

    /**
     * 便捷工厂：根据一次已完成的选择结果推导解释。
     * <p>供未继承 {@code AbstractSelector} 的选择器复用，避免样板代码。</p>
     */
    public static <T extends ExtAbility> DecisionExplanation fromSelection(String selectorName,
                                                                          List<T> candidates,
                                                                          T selected) {
        if (selected == null) {
            return miss(selectorName, candidates, "选择器未命中匹配候选");
        }
        return new DecisionExplanation(selectorName, extIds(candidates),
                Collections.singletonList(selected.getExtId()), selected.getExtId(), Outcome.HIT, "命中");
    }

    private static List<String> extIds(List<? extends ExtAbility> list) {
        List<String> ids = new ArrayList<>();
        if (list != null) {
            for (ExtAbility a : list) {
                ids.add(a == null ? "null" : a.getExtId());
            }
        }
        return ids;
    }

    // =============== 访问器 ===============

    public String getSelectorName() { return selectorName; }
    public List<String> getCandidateExtIds() { return candidateExtIds; }
    public List<String> getFilteredExtIds() { return filteredExtIds; }
    public String getSelectedExtId() { return selectedExtId; }
    public Outcome getOutcome() { return outcome; }
    public String getReason() { return reason; }

    @Override
    public String toString() {
        return "DecisionExplanation{selector=" + selectorName
                + ", outcome=" + outcome
                + ", selected=" + selectedExtId
                + ", reason='" + reason + '\''
                + ", candidates=" + candidateExtIds
                + ", filtered=" + filteredExtIds
                + '}';
    }
}
