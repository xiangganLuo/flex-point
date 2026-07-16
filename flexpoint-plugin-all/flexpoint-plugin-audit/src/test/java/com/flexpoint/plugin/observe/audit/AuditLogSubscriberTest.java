package com.flexpoint.plugin.observe.audit;

import com.flexpoint.core.event.EventContext;
import com.flexpoint.core.event.EventDispatcher;
import com.flexpoint.core.event.EventType;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.selector.DecisionExplanation;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AuditLogSubscriber} 单元测试。
 */
class AuditLogSubscriberTest {

    /** 测试用扩展点。 */
    static class TestExt implements ExtAbility {
        @Override
        public String getCode() {
            return "test-code";
        }

        @Override
        public String getExtId() {
            return "TestExt#audit";
        }
    }

    @Test
    void defaultSubscriber_filtersSelectionAndInvocationEvents() {
        AuditLogSubscriber subscriber = new AuditLogSubscriber(true, true);

        assertTrue(subscriber.getEventFilter().matches(EventContext.create(EventType.EXT_SELECTED)));
        assertTrue(subscriber.getEventFilter().matches(EventContext.create(EventType.EXT_SELECTION_FAILED)));
        assertTrue(subscriber.getEventFilter().matches(EventContext.create(EventType.INVOKE_SUCCESS)));
        assertTrue(subscriber.getEventFilter().matches(EventContext.create(EventType.INVOKE_FAIL)));
        assertTrue(subscriber.getEventFilter().matches(EventContext.create(EventType.INVOKE_EXCEPTION)));
        // 非选择/调用类事件不应匹配
        assertFalse(subscriber.getEventFilter().matches(EventContext.create(EventType.EXT_REGISTERED)));
    }

    @Test
    void selectionDisabled_onlyInvocationMatches() {
        AuditLogSubscriber subscriber = new AuditLogSubscriber(false, true);
        assertFalse(subscriber.getEventFilter().matches(EventContext.create(EventType.EXT_SELECTED)));
        assertTrue(subscriber.getEventFilter().matches(EventContext.create(EventType.INVOKE_SUCCESS)));
    }

    @Test
    void invocationDisabled_onlySelectionMatches() {
        AuditLogSubscriber subscriber = new AuditLogSubscriber(true, false);
        assertTrue(subscriber.getEventFilter().matches(EventContext.create(EventType.EXT_SELECTED)));
        assertFalse(subscriber.getEventFilter().matches(EventContext.create(EventType.INVOKE_SUCCESS)));
    }

    @Test
    void bothDisabled_neverMatches() {
        AuditLogSubscriber subscriber = new AuditLogSubscriber(false, false);
        assertFalse(subscriber.getEventFilter().matches(EventContext.create(EventType.EXT_SELECTED)));
        assertFalse(subscriber.getEventFilter().matches(EventContext.create(EventType.INVOKE_SUCCESS)));
    }

    @Test
    void onEvent_handlesSelectionWithDecisionExplanation() {
        AuditLogSubscriber subscriber = new AuditLogSubscriber(true, true);
        TestExt ext = new TestExt();
        DecisionExplanation explanation = DecisionExplanation.hit(
                "testSelector", Collections.singletonList(ext), Collections.singletonList(ext), ext.getExtId());
        EventContext ctx = EventContext.createExtEvent(EventType.EXT_SELECTED, ext);
        ctx.setSelectorName("testSelector");
        ctx.withAttribute(EventDispatcher.ATTR_DECISION_EXPLANATION, explanation);

        assertDoesNotThrow(() -> subscriber.onEvent(ctx));
    }

    @Test
    void onEvent_handlesInvocationSuccessAndException() {
        AuditLogSubscriber subscriber = new AuditLogSubscriber(true, true);
        TestExt ext = new TestExt();

        EventContext success = EventContext.createInvokeEvent(
                EventType.INVOKE_SUCCESS, ext, "doWork", new Object[]{}, "ok", null, 12L);
        EventContext exception = EventContext.createInvokeEvent(
                EventType.INVOKE_EXCEPTION, ext, "doWork", new Object[]{}, null,
                new IllegalStateException("boom"), 5L);

        assertDoesNotThrow(() -> subscriber.onEvent(success));
        assertDoesNotThrow(() -> subscriber.onEvent(exception));
    }

    @Test
    void onEvent_ignoresNullAndUnrelatedEvents() {
        AuditLogSubscriber subscriber = new AuditLogSubscriber(true, true);
        assertDoesNotThrow(() -> subscriber.onEvent(null));
        assertDoesNotThrow(() -> subscriber.onEvent(EventContext.create(EventType.SELECTOR_REGISTERED)));
    }
}
