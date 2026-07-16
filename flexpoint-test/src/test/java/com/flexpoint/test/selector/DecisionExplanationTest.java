package com.flexpoint.test.selector;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.selector.AbstractSelector;
import com.flexpoint.core.selector.DecisionExplanation;
import com.flexpoint.core.selector.Selector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 决策解释 v1 用例：命中 / 未命中 / 歧义，以及 AbstractSelector 通用实现与
 * 直接实现 Selector 时的便捷推导。
 *
 * @author xiangganluo
 */
public class DecisionExplanationTest {

    interface DemoAbility extends ExtAbility {}

    static class Impl implements DemoAbility {
        private final String code;
        Impl(String code) { this.code = code; }
        @Override public String getCode() { return code; }
    }

    /** 基于 code 过滤的 AbstractSelector 实现。 */
    static class CodeEq extends AbstractSelector {
        private final String target;
        CodeEq(String target) { this.target = target; }
        @Override protected <T extends ExtAbility> List<T> filter(List<T> candidates) {
            return candidates.stream().filter(c -> target.equals(c.getCode())).collect(Collectors.toList());
        }
        @Override public String getName() { return "codeEq"; }
    }

    private List<DemoAbility> candidates(String... codes) {
        List<DemoAbility> list = new ArrayList<>();
        for (String c : codes) list.add(new Impl(c));
        return list;
    }

    @Test
    void abstract_selector_explains_hit() {
        DecisionExplanation e = new CodeEq("b").explain(candidates("a", "b", "c"));
        Assertions.assertEquals(DecisionExplanation.Outcome.HIT, e.getOutcome());
        Assertions.assertNotNull(e.getSelectedExtId());
        Assertions.assertEquals(3, e.getCandidateExtIds().size());
        Assertions.assertEquals(1, e.getFilteredExtIds().size());
        Assertions.assertEquals("codeEq", e.getSelectorName());
    }

    @Test
    void abstract_selector_explains_miss() {
        DecisionExplanation e = new CodeEq("x").explain(candidates("a", "b"));
        Assertions.assertEquals(DecisionExplanation.Outcome.MISS, e.getOutcome());
        Assertions.assertNull(e.getSelectedExtId());
        Assertions.assertTrue(e.getFilteredExtIds().isEmpty());
    }

    @Test
    void abstract_selector_explains_ambiguous() {
        DecisionExplanation e = new CodeEq("dup").explain(candidates("dup", "dup", "other"));
        Assertions.assertEquals(DecisionExplanation.Outcome.AMBIGUOUS, e.getOutcome());
        Assertions.assertNull(e.getSelectedExtId());
        Assertions.assertEquals(2, e.getFilteredExtIds().size());
    }

    @Test
    void raw_selector_can_use_from_selection_helper() {
        Selector raw = new Selector() {
            @Override public <T extends ExtAbility> T select(List<T> candidates) {
                return candidates.isEmpty() ? null : candidates.get(0);
            }
            @Override public String getName() { return "raw"; }
            @Override public <T extends ExtAbility> DecisionExplanation explain(List<T> candidates) {
                return DecisionExplanation.fromSelection(getName(), candidates, select(candidates));
            }
        };

        DecisionExplanation hit = raw.explain(candidates("a", "b"));
        Assertions.assertEquals(DecisionExplanation.Outcome.HIT, hit.getOutcome());
        Assertions.assertNotNull(hit.getSelectedExtId());

        DecisionExplanation miss = raw.explain(candidates());
        Assertions.assertEquals(DecisionExplanation.Outcome.MISS, miss.getOutcome());
    }
}
