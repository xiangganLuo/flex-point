package com.flexpoint.test.event;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.event.EventContext;
import com.flexpoint.core.event.EventDispatcher;
import com.flexpoint.core.event.EventType;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.selector.AbstractSelector;
import com.flexpoint.core.selector.DecisionExplanation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * P7：选择相关事件携带决策解释（decisionExplanation 属性）。
 *
 * @author xiangganluo
 */
public class DecisionExplanationEventTest {

    @FpSelector("codeEqSelector")
    interface DemoAbility extends ExtAbility {}

    static class A implements DemoAbility { @Override public String getCode() { return "a"; } }

    static class CodeEqSelector extends AbstractSelector {
        private final String target;
        CodeEqSelector(String target) { this.target = target; }
        @Override protected <T extends ExtAbility> List<T> filter(List<T> c) {
            c.removeIf(x -> !target.equals(x.getCode()));
            return c;
        }
        @Override public String getName() { return "codeEqSelector"; }
    }

    @Test
    void ext_selected_event_carries_decision_explanation() {
        FlexPoint fp = FlexPointBuilder.create().build();
        fp.registerSelector(new CodeEqSelector("a"));
        fp.register(new A());

        AtomicReference<EventContext> selected = new AtomicReference<>();
        fp.getEventBus().subscribe(ctx -> {
            if (ctx.getEventType() == EventType.EXT_SELECTED) selected.set(ctx);
        });

        Assertions.assertNotNull(fp.findAbility(DemoAbility.class));

        EventContext ctx = selected.get();
        Assertions.assertNotNull(ctx, "应发布 EXT_SELECTED 事件");
        Object exp = ctx.getAttribute(EventDispatcher.ATTR_DECISION_EXPLANATION);
        Assertions.assertTrue(exp instanceof DecisionExplanation, "EXT_SELECTED 应携带决策解释");
        Assertions.assertEquals(DecisionExplanation.Outcome.HIT, ((DecisionExplanation) exp).getOutcome());

        fp.shutdown();
    }

    @Test
    void ext_selection_failed_event_carries_decision_explanation() {
        FlexPoint fp = FlexPointBuilder.create().build();
        fp.registerSelector(new CodeEqSelector("zzz")); // 不命中
        fp.register(new A());

        AtomicReference<EventContext> failed = new AtomicReference<>();
        fp.getEventBus().subscribe(ctx -> {
            if (ctx.getEventType() == EventType.EXT_SELECTION_FAILED) failed.set(ctx);
        });

        Assertions.assertNull(fp.findAbility(DemoAbility.class));

        EventContext ctx = failed.get();
        Assertions.assertNotNull(ctx, "应发布 EXT_SELECTION_FAILED 事件");
        Object exp = ctx.getAttribute(EventDispatcher.ATTR_DECISION_EXPLANATION);
        Assertions.assertTrue(exp instanceof DecisionExplanation);
        Assertions.assertEquals(DecisionExplanation.Outcome.MISS, ((DecisionExplanation) exp).getOutcome());

        fp.shutdown();
    }
}
