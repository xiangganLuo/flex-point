package com.flexpoint.plugin.observe.slowcall;

import com.flexpoint.core.event.EventContext;
import com.flexpoint.core.event.EventSubscriber;
import com.flexpoint.core.event.EventType;
import com.flexpoint.core.event.filter.CompositeEventFilter;
import com.flexpoint.core.event.filter.EventFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * 慢调用事件订阅者。
 *
 * <p>订阅 INVOKE_SUCCESS/INVOKE_FAIL 事件，当调用耗时超过阈值时输出 warn 告警日志，
 * 并回调 {@link SlowCallListener}。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public class SlowCallSubscriber implements EventSubscriber {

    private final long thresholdMs;
    private final SlowCallListener listener;

    public SlowCallSubscriber(long thresholdMs, SlowCallListener listener) {
        this.thresholdMs = thresholdMs;
        this.listener = listener;
    }

    @Override
    public void onEvent(EventContext ctx) {
        if (ctx == null) {
            return;
        }
        Long duration = ctx.getDuration();
        if (duration == null || duration <= thresholdMs) {
            return;
        }
        String extId = ctx.getExtId();
        String methodName = ctx.getMethodName();
        log.warn("SLOW_CALL extId={} method={} duration={}ms threshold={}ms event={}",
                extId, methodName, duration, thresholdMs, ctx.getEventType());
        if (listener != null) {
            try {
                listener.onSlowCall(extId, methodName, duration, thresholdMs);
            } catch (Exception e) {
                log.warn("SlowCallListener 回调异常: extId={}, method={}", extId, methodName, e);
            }
        }
    }

    @Override
    public String getName() {
        return "SlowCallSubscriber";
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public EventFilter getEventFilter() {
        return CompositeEventFilter.or(
                EventFilter.byEventType(EventType.INVOKE_SUCCESS),
                EventFilter.byEventType(EventType.INVOKE_FAIL)
        );
    }
}
