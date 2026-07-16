package com.flexpoint.plugin.observe.slowcall;

import com.flexpoint.core.event.EventContext;
import com.flexpoint.core.event.EventType;
import com.flexpoint.core.ext.ExtAbility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SlowCallSubscriber} 单元测试。
 */
class SlowCallSubscriberTest {

    /** 测试用扩展点。 */
    static class TestExt implements ExtAbility {
        @Override
        public String getCode() {
            return "test-code";
        }

        @Override
        public String getExtId() {
            return "TestExt#slow";
        }
    }

    private EventContext invoke(long duration) {
        return EventContext.createInvokeEvent(
                EventType.INVOKE_SUCCESS, new TestExt(), "doWork", new Object[]{}, "ok", null, duration);
    }

    @Test
    void triggersListenerWhenDurationExceedsThreshold() {
        AtomicInteger count = new AtomicInteger();
        AtomicLong reportedDuration = new AtomicLong();
        SlowCallListener listener = (extId, method, durationMs, thresholdMs) -> {
            count.incrementAndGet();
            reportedDuration.set(durationMs);
        };
        SlowCallSubscriber subscriber = new SlowCallSubscriber(200L, listener);

        subscriber.onEvent(invoke(500L));

        assertEquals(1, count.get());
        assertEquals(500L, reportedDuration.get());
    }

    @Test
    void doesNotTriggerWhenBelowOrEqualThreshold() {
        AtomicInteger count = new AtomicInteger();
        SlowCallListener listener = (extId, method, durationMs, thresholdMs) -> count.incrementAndGet();
        SlowCallSubscriber subscriber = new SlowCallSubscriber(200L, listener);

        subscriber.onEvent(invoke(100L));
        subscriber.onEvent(invoke(200L)); // 恰好等于阈值，不触发

        assertEquals(0, count.get());
    }

    @Test
    void ignoresNullDurationAndNullContext() {
        AtomicInteger count = new AtomicInteger();
        SlowCallListener listener = (extId, method, durationMs, thresholdMs) -> count.incrementAndGet();
        SlowCallSubscriber subscriber = new SlowCallSubscriber(200L, listener);

        subscriber.onEvent(null);
        subscriber.onEvent(EventContext.createInvokeEvent(
                EventType.INVOKE_SUCCESS, new TestExt(), "doWork", new Object[]{}, "ok", null, null));

        assertEquals(0, count.get());
    }

    @Test
    void worksWithoutListener() {
        SlowCallSubscriber subscriber = new SlowCallSubscriber(200L, null);
        // 无 listener 时仅记录日志，不应抛异常
        subscriber.onEvent(invoke(500L));
    }

    @Test
    void filterMatchesInvokeSuccessAndFail() {
        SlowCallSubscriber subscriber = new SlowCallSubscriber(200L, null);
        assertTrue(subscriber.getEventFilter().matches(EventContext.create(EventType.INVOKE_SUCCESS)));
        assertTrue(subscriber.getEventFilter().matches(EventContext.create(EventType.INVOKE_FAIL)));
    }
}
