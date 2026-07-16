package com.flexpoint.test.selector;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.selector.AbstractSelector;
import com.flexpoint.core.selector.DecisionExplanation;
import com.flexpoint.core.selector.SelectionResult;
import com.flexpoint.core.selector.Selector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 决策解释用例：命中 / 未命中 / 歧义（经由 select() 返回的 SelectionResult 携带解释），
 * 以及 AbstractSelector 通用实现与直接实现 Selector 时的便捷推导。
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
        SelectionResult<DemoAbility> r = new CodeEq("b").select(candidates("a", "b", "c"));
        Assertions.assertTrue(r.isHit());
        Assertions.assertNotNull(r.getSelected());
        DecisionExplanation e = r.getExplanation();
        Assertions.assertEquals(DecisionExplanation.Outcome.HIT, e.getOutcome());
        Assertions.assertNotNull(e.getSelectedExtId());
        Assertions.assertEquals(3, e.getCandidateExtIds().size());
        Assertions.assertEquals(1, e.getFilteredExtIds().size());
        Assertions.assertEquals("codeEq", e.getSelectorName());
    }

    @Test
    void abstract_selector_explains_miss() {
        SelectionResult<DemoAbility> r = new CodeEq("x").select(candidates("a", "b"));
        Assertions.assertTrue(r.isMiss());
        Assertions.assertNull(r.getSelected());
        DecisionExplanation e = r.getExplanation();
        Assertions.assertEquals(DecisionExplanation.Outcome.MISS, e.getOutcome());
        Assertions.assertNull(e.getSelectedExtId());
        Assertions.assertTrue(e.getFilteredExtIds().isEmpty());
    }

    @Test
    void abstract_selector_explains_ambiguous() {
        SelectionResult<DemoAbility> r = new CodeEq("dup").select(candidates("dup", "dup", "other"));
        Assertions.assertTrue(r.isAmbiguous());
        Assertions.assertNull(r.getSelected());
        DecisionExplanation e = r.getExplanation();
        Assertions.assertEquals(DecisionExplanation.Outcome.AMBIGUOUS, e.getOutcome());
        Assertions.assertNull(e.getSelectedExtId());
        Assertions.assertEquals(2, e.getFilteredExtIds().size());
    }

    @Test
    void raw_selector_can_use_selection_result_helper() {
        Selector raw = new Selector() {
            @Override public String getName() { return "raw"; }
            @Override public <T extends ExtAbility> SelectionResult<T> select(List<T> candidates) {
                T picked = candidates.isEmpty() ? null : candidates.get(0);
                return SelectionResult.of(getName(), candidates, picked);
            }
        };

        SelectionResult<DemoAbility> hit = raw.select(candidates("a", "b"));
        Assertions.assertTrue(hit.isHit());
        Assertions.assertNotNull(hit.getExplanation().getSelectedExtId());

        SelectionResult<DemoAbility> miss = raw.select(candidates());
        Assertions.assertTrue(miss.isMiss());
    }
}
