package com.flexpoint.plugin.observe.audit;

import com.flexpoint.core.event.EventContext;
import com.flexpoint.core.event.EventDispatcher;
import com.flexpoint.core.event.EventSubscriber;
import com.flexpoint.core.event.EventType;
import com.flexpoint.core.event.filter.CompositeEventFilter;
import com.flexpoint.core.event.filter.EventFilter;
import com.flexpoint.core.selector.DecisionExplanation;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 审计事件订阅者
 *
 * <p>将扩展点「选择」与「调用」事件以结构化单行日志输出，便于日志采集与检索。
 * 输出内容包含 extId、selectorName、methodName、duration、success/异常类型，
 * 以及（若有）决策解释摘要。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public class AuditLogSubscriber implements EventSubscriber {

    private final boolean logSelection;
    private final boolean logInvocation;

    public AuditLogSubscriber(boolean logSelection, boolean logInvocation) {
        this.logSelection = logSelection;
        this.logInvocation = logInvocation;
    }

    @Override
    public void onEvent(EventContext ctx) {
        if (ctx == null) {
            return;
        }
        EventType type = ctx.getEventType();
        if (type == null) {
            return;
        }
        switch (type) {
            case EXT_SELECTED:
            case EXT_SELECTION_FAILED:
                if (logSelection) {
                    logSelection(ctx);
                }
                break;
            case INVOKE_SUCCESS:
            case INVOKE_FAIL:
            case INVOKE_EXCEPTION:
                if (logInvocation) {
                    logInvocation(ctx);
                }
                break;
            default:
        }
    }

    private void logSelection(EventContext ctx) {
        StringBuilder sb = new StringBuilder("AUDIT category=SELECT");
        append(sb, "event", ctx.getEventType());
        append(sb, "extId", ctx.getExtId());
        append(sb, "extType", ctx.getExtType() != null ? ctx.getExtType().getSimpleName() : null);
        append(sb, "selector", ctx.getSelectorName());
        append(sb, "reason", ctx.getAttribute("reason"));
        appendDecision(sb, ctx);
        log.info(sb.toString());
    }

    private void logInvocation(EventContext ctx) {
        StringBuilder sb = new StringBuilder("AUDIT category=INVOKE");
        append(sb, "event", ctx.getEventType());
        append(sb, "extId", ctx.getExtId());
        append(sb, "method", ctx.getMethodName());
        append(sb, "duration", ctx.getDuration() != null ? ctx.getDuration() + "ms" : null);
        boolean success = ctx.getEventType() == EventType.INVOKE_SUCCESS;
        append(sb, "success", success);
        Throwable ex = ctx.getException();
        if (ex != null) {
            append(sb, "exception", ex.getClass().getName());
            append(sb, "message", ex.getMessage());
        }
        log.info(sb.toString());
    }

    private void appendDecision(StringBuilder sb, EventContext ctx) {
        Object exp = ctx.getAttribute(EventDispatcher.ATTR_DECISION_EXPLANATION);
        if (exp instanceof DecisionExplanation) {
            DecisionExplanation de = (DecisionExplanation) exp;
            append(sb, "outcome", de.getOutcome());
            append(sb, "selected", de.getSelectedExtId());
            append(sb, "decisionReason", de.getReason());
        }
    }

    private void append(StringBuilder sb, String key, Object value) {
        if (value == null) {
            return;
        }
        sb.append(' ').append(key).append('=').append(value);
    }

    @Override
    public String getName() {
        return "AuditLogSubscriber";
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public EventFilter getEventFilter() {
        List<EventFilter> filters = new ArrayList<>();
        if (logSelection) {
            filters.add(EventFilter.byEventType(EventType.EXT_SELECTED));
            filters.add(EventFilter.byEventType(EventType.EXT_SELECTION_FAILED));
        }
        if (logInvocation) {
            filters.add(EventFilter.byEventType(EventType.INVOKE_SUCCESS));
            filters.add(EventFilter.byEventType(EventType.INVOKE_FAIL));
            filters.add(EventFilter.byEventType(EventType.INVOKE_EXCEPTION));
        }
        if (filters.isEmpty()) {
            return EventFilter.never();
        }
        return CompositeEventFilter.or(filters.toArray(new EventFilter[0]));
    }
}
