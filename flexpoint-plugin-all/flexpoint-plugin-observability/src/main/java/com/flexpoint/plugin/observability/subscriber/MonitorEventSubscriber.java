package com.flexpoint.plugin.observability.subscriber;

import com.flexpoint.core.event.EventContext;
import com.flexpoint.core.event.EventSubscriber;
import com.flexpoint.core.event.EventType;
import com.flexpoint.core.event.filter.CompositeEventFilter;
import com.flexpoint.core.event.filter.EventFilter;
import com.flexpoint.core.monitor.ExtMonitor;
import lombok.extern.slf4j.Slf4j;

/**
 * 插件域：监控事件订阅者
 * 将调用/异常事件转发给 {@link ExtMonitor} 做统一记录。
 *
 * @author xiangganluo
 */
@Slf4j
public class MonitorEventSubscriber implements EventSubscriber {
    private final ExtMonitor extMonitor;

    public MonitorEventSubscriber(ExtMonitor extMonitor) {
        this.extMonitor = extMonitor;
    }

    @Override
    public void onEvent(EventContext eventContext) {
        if (extMonitor == null || eventContext == null) return;
        EventType eventType = eventContext.getEventType();
        log.debug("监控订阅者收到事件: eventType={}, eventId={}", eventType, eventContext.getEventId());
        switch (eventType) {
            case INVOKE_SUCCESS:
                extMonitor.recordInvocation(
                        eventContext.getExtAbility(),
                        eventContext.getDuration() != null ? eventContext.getDuration() : 0L,
                        true
                );
                break;
            case INVOKE_FAIL:
                extMonitor.recordInvocation(
                        eventContext.getExtAbility(),
                        eventContext.getDuration() != null ? eventContext.getDuration() : 0L,
                        false
                );
                break;
            case INVOKE_EXCEPTION:
                extMonitor.recordException(
                        eventContext.getExtAbility(),
                        eventContext.getException()
                );
                break;
            default:
        }
    }

    @Override
    public String getName() { return "MonitorEventSubscriber"; }

    @Override
    public int getPriority() { return 200; }

    @Override
    public EventFilter getEventFilter() {
        return CompositeEventFilter.or(
                EventFilter.byEventType(EventType.INVOKE_SUCCESS),
                EventFilter.byEventType(EventType.INVOKE_FAIL),
                EventFilter.byEventType(EventType.INVOKE_EXCEPTION)
        );
    }
}
