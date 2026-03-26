package com.flexpoint.core.plugin.official.event.subscriber;

import com.flexpoint.core.event.EventContext;
import com.flexpoint.core.event.EventSubscriber;
import com.flexpoint.core.event.EventType;
import com.flexpoint.core.event.filter.CompositeEventFilter;
import com.flexpoint.core.event.filter.EventFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * 事件审计订阅者（示例玩法）：
 * - 记录选择器命中/未命中的关键信息；
 * - 可扩展为将事件发送到外部系统（此处仅日志）。
 */
@Slf4j
public class AuditEventSubscriber implements EventSubscriber {

    @Override
    public void onEvent(EventContext ctx) {
        if (ctx == null || ctx.getEventType() == null) return;
        switch (ctx.getEventType()) {
            case EXT_SELECTED:
                log.info("audit.select.ok selector={} ext={} code={} type={}",
                        ctx.getSelectorName(),
                        ctx.getExtAbility() != null ? ctx.getExtAbility().getExtId() : null,
                        ctx.getExtCode(),
                        ctx.getExtType() != null ? ctx.getExtType().getSimpleName() : null);
                break;
            case EXT_SELECTION_FAILED:
                log.warn("audit.select.fail selector={} type={} reason={}",
                        ctx.getSelectorName(),
                        ctx.getExtType() != null ? ctx.getExtType().getSimpleName() : null,
                        String.valueOf(ctx.getAttribute("reason")));
                break;
            case INVOKE_BEFORE:
                log.debug("audit.invoke.before ext={} method={}",
                        ctx.getExtAbility() != null ? ctx.getExtAbility().getExtId() : null,
                        ctx.getMethodName());
                break;
            default:
        }
    }

    @Override
    public String getName() { return "AuditEventSubscriber"; }

    @Override
    public int getPriority() { return 300; }

    @Override
    public EventFilter getEventFilter() {
        return CompositeEventFilter.or(
                EventFilter.byEventType(EventType.EXT_SELECTED),
                EventFilter.byEventType(EventType.EXT_SELECTION_FAILED),
                EventFilter.byEventType(EventType.INVOKE_BEFORE)
        );
    }
}

