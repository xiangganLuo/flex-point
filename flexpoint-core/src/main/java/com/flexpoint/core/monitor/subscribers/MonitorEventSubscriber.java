package com.flexpoint.core.monitor.subscribers;

import com.flexpoint.core.event.EventContext;
import com.flexpoint.core.event.EventSubscriber;
import com.flexpoint.core.event.EventType;
import com.flexpoint.core.event.filter.CompositeEventFilter;
import com.flexpoint.core.event.filter.EventFilter;
import com.flexpoint.core.monitor.ExtMonitor;
import lombok.extern.slf4j.Slf4j;

/**
 * 监控事件订阅者（事件转发到ExtMonitor）
 * 只负责将事件内容转发给ExtMonitor统一记录
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
        if (extMonitor == null || eventContext == null) {
            return;
        }
        EventType eventType = eventContext.getEventType();
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
    public String getName() {
        return "MonitorEventSubscriber";
    }

    @Override
    public int getPriority() {
        return 200;
    }


    @Override
    public EventFilter getEventFilter() {
        return CompositeEventFilter.or(
            EventFilter.byEventType(EventType.INVOKE_SUCCESS),
            EventFilter.byEventType(EventType.INVOKE_FAIL),
            EventFilter.byEventType(EventType.INVOKE_EXCEPTION)
        );
    }
} 