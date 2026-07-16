package com.flexpoint.core.event;

import com.flexpoint.core.ext.ExtAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;

/**
 * 事件分发器（实例级）
 * 负责将扩展点生命周期/调用事件发送到指定 EventBus。
 */
@Slf4j
@RequiredArgsConstructor
public class EventDispatcher {

    private final EventBus eventBus;

    public EventBus getEventBus() {
        return eventBus;
    }

    public void shutdown() {
        if (eventBus != null) {
            eventBus.shutdown();
        }
    }

    public void publishExtFound(Class<? extends ExtAbility> extType) {
        EventContext eventContext = EventContext.create(EventType.EXT_FOUND);
        eventContext.setExtType(extType);
        publishEvent(eventContext);
    }

    public void publishExtNotFound(Class<? extends ExtAbility> extType) {
        EventContext eventContext = EventContext.create(EventType.EXT_NOT_FOUND);
        eventContext.setExtType(extType);
        publishEvent(eventContext);
    }

    public void publishExtSelected(ExtAbility extAbility, String selectorName) {
        EventContext eventContext = EventContext.createExtEvent(EventType.EXT_SELECTED, extAbility);
        eventContext.setSelectorName(selectorName);
        publishEvent(eventContext);
    }

    public void publishExtSelectionFailed(Class<? extends ExtAbility> extType, String selectorName, String reason) {
        EventContext eventContext = EventContext.create(EventType.EXT_SELECTION_FAILED);
        eventContext.setExtType(extType);
        eventContext.setSelectorName(selectorName);
        eventContext.withAttribute("reason", reason);
        publishEvent(eventContext);
    }

    public void publishInvokeBefore(ExtAbility extAbility, String methodName, Object[] args) {
        EventContext eventContext = EventContext.createInvokeEvent(
                EventType.INVOKE_BEFORE, extAbility, methodName, args, null, null, null);
        publishEvent(eventContext);
    }

    public void publishInvokeSuccess(ExtAbility extAbility, String methodName, Object[] args, Object result, Long duration) {
        EventContext eventContext = EventContext.createInvokeEvent(
                EventType.INVOKE_SUCCESS, extAbility, methodName, args, result, null, duration);
        publishEvent(eventContext);
    }

    public void publishInvokeException(ExtAbility extAbility, String methodName, Object[] args, Throwable exception, Long duration) {
        EventContext eventContext = EventContext.createInvokeEvent(
                EventType.INVOKE_EXCEPTION, extAbility, methodName, args, null, exception, duration);
        publishEvent(eventContext);
    }

    /**
     * 发布调用失败事件（业务异常）
     */
    public void publishInvokeFail(ExtAbility extAbility, String methodName, Object[] args, Throwable exception, Long duration) {
        Throwable real = exception;
        if (exception instanceof InvocationTargetException
                && ((InvocationTargetException) exception).getTargetException() != null) {
            real = ((InvocationTargetException) exception).getTargetException();
        }
        EventContext eventContext = EventContext.createInvokeEvent(
                EventType.INVOKE_FAIL, extAbility, methodName, args, null, real, duration);
        publishEvent(eventContext);
    }

    public void publishSelectorRegistered(String selectorName) {
        EventContext eventContext = EventContext.create(EventType.SELECTOR_REGISTERED);
        eventContext.setSelectorName(selectorName);
        publishEvent(eventContext);
    }

    public void publishSelectorUnregistered(String selectorName) {
        EventContext eventContext = EventContext.create(EventType.SELECTOR_UNREGISTERED);
        eventContext.setSelectorName(selectorName);
        publishEvent(eventContext);
    }

    public void publishSelectorFound(String selectorName) {
        EventContext eventContext = EventContext.create(EventType.SELECTOR_FOUND);
        eventContext.setSelectorName(selectorName);
        publishEvent(eventContext);
    }

    public void publishSelectorNotFound(String selectorName) {
        EventContext eventContext = EventContext.create(EventType.SELECTOR_NOT_FOUND);
        eventContext.setSelectorName(selectorName);
        publishEvent(eventContext);
    }

    public void publishEventAsync(EventContext eventContext) {
        if (eventBus != null) {
            try {
                log.debug("异步发布事件: eventType={}, eventId={}", eventContext.getEventType(), eventContext.getEventId());
                eventBus.publishAsync(eventContext);
            } catch (Exception e) {
                log.error("异步发布事件失败: eventType={}", eventContext.getEventType(), e);
            }
        }
    }

    public CompletableFuture<Void> publishEvent(EventContext eventContext) {
        if (eventBus != null) {
            try {
                log.debug("同步发布事件: eventType={}, eventId={}", eventContext.getEventType(), eventContext.getEventId());
                eventBus.publish(eventContext);
            } catch (Exception e) {
                log.error("发布事件失败: eventType={}", eventContext.getEventType(), e);
                CompletableFuture<Void> f = new CompletableFuture<>();
                f.completeExceptionally(e);
                return f;
            }
        }
        return CompletableFuture.completedFuture(null);
    }
}
