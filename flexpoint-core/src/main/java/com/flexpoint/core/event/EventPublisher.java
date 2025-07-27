package com.flexpoint.core.event;

import com.flexpoint.core.ext.ExtAbility;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 扩展点事件发布器工具类
 * 提供便捷的扩展点事件发布方法
 *
 * @author xiangganluo
 */
@Slf4j
public class EventPublisher {

    private static EventBus eventBus;
    
    /**
     * 设置事件总线
     */
    public static void setEventBus(EventBus eventBus) {
        EventPublisher.eventBus = eventBus;
    }

    /**
     * 获取事件总线
     */
    public static EventBus getEventBus() {
        return eventBus;
    }

    // ==================== 扩展点生命周期事件 ====================
    
    /**
     * 发布扩展点注册事件
     */
    public static void publishExtRegistered(ExtAbility extAbility) {
        publishEvent(EventType.EXT_REGISTERED, extAbility);
    }
    
    /**
     * 发布扩展点注销事件
     */
    public static void publishExtUnregistered(ExtAbility extAbility) {
        publishEvent(EventType.EXT_UNREGISTERED, extAbility);
    }
    
    /**
     * 发布扩展点查找事件
     */
    public static void publishExtFound(Class<? extends ExtAbility> extType) {
        EventContext eventContext = EventContext.create(EventType.EXT_FOUND);
        eventContext.setExtType(extType);
        publishEvent(eventContext);
    }
    
    /**
     * 发布扩展点未找到事件
     */
    public static void publishExtNotFound(Class<? extends ExtAbility> extType) {
        EventContext eventContext = EventContext.create(EventType.EXT_NOT_FOUND);
        eventContext.setExtType(extType);
        publishEvent(eventContext);
    }
    
    /**
     * 发布扩展点选择事件
     */
    public static void publishExtSelected(ExtAbility extAbility, String selectorName) {
        EventContext eventContext = EventContext.createExtEvent(EventType.EXT_SELECTED, extAbility);
        eventContext.setSelectorName(selectorName);
        publishEvent(eventContext);
    }
    
    /**
     * 发布扩展点选择失败事件
     */
    public static void publishExtSelectionFailed(Class<? extends ExtAbility> extType, String selectorName, String reason) {
        EventContext eventContext = EventContext.create(EventType.EXT_SELECTION_FAILED);
        eventContext.setExtType(extType);
        eventContext.setSelectorName(selectorName);
        eventContext.withAttribute("reason", reason);
        publishEvent(eventContext);
    }
    
    // ==================== 扩展点调用事件 ====================
    
    /**
     * 发布调用前事件
     */
    public static void publishInvokeBefore(ExtAbility extAbility, String methodName, Object[] args) {
        EventContext eventContext = EventContext.createInvokeEvent(
            EventType.INVOKE_BEFORE, extAbility, methodName, args, null, null, null);
        publishEvent(eventContext);
    }
    
    /**
     * 发布调用成功事件
     */
    public static void publishInvokeSuccess(ExtAbility extAbility, String methodName, Object[] args, Object result, Long duration) {
        EventContext eventContext = EventContext.createInvokeEvent(
            EventType.INVOKE_SUCCESS, extAbility, methodName, args, result, null, duration);
        publishEvent(eventContext);
    }
    
    /**
     * 发布调用失败事件
     */
    public static void publishInvokeFail(ExtAbility extAbility, String methodName, Object[] args, Object result, Long duration) {
        EventContext eventContext = EventContext.createInvokeEvent(
            EventType.INVOKE_FAIL, extAbility, methodName, args, result, null, duration);
        publishEvent(eventContext);
    }
    
    /**
     * 发布调用异常事件
     */
    public static void publishInvokeException(ExtAbility extAbility, String methodName, Object[] args, Throwable exception, Long duration) {
        EventContext eventContext = EventContext.createInvokeEvent(
            EventType.INVOKE_EXCEPTION, extAbility, methodName, args, null, exception, duration);
        publishEvent(eventContext);
    }
    
    // ==================== 选择器事件 ====================
    
    /**
     * 发布选择器注册事件
     */
    public static void publishSelectorRegistered(String selectorName) {
        EventContext eventContext = EventContext.create(EventType.SELECTOR_REGISTERED);
        eventContext.setSelectorName(selectorName);
        publishEvent(eventContext);
    }
    
    /**
     * 发布选择器注销事件
     */
    public static void publishSelectorUnregistered(String selectorName) {
        EventContext eventContext = EventContext.create(EventType.SELECTOR_UNREGISTERED);
        eventContext.setSelectorName(selectorName);
        publishEvent(eventContext);
    }
    
    /**
     * 发布选择器查找事件
     */
    public static void publishSelectorFound(String selectorName) {
        EventContext eventContext = EventContext.create(EventType.SELECTOR_FOUND);
        eventContext.setSelectorName(selectorName);
        publishEvent(eventContext);
    }
    
    /**
     * 发布选择器未找到事件
     */
    public static void publishSelectorNotFound(String selectorName) {
        EventContext eventContext = EventContext.create(EventType.SELECTOR_NOT_FOUND);
        eventContext.setSelectorName(selectorName);
        publishEvent(eventContext);
    }
    
    // ==================== 通用事件发布方法 ====================
    
    /**
     * 发布事件（同步）
     */
    public static void publishEvent(EventType eventType) {
        publishEvent(EventContext.create(eventType));
    }
    
    /**
     * 发布事件（同步）
     */
    public static void publishEvent(EventType eventType, ExtAbility extAbility) {
        publishEvent(EventContext.createExtEvent(eventType, extAbility));
    }
    
    /**
     * 发布事件（同步）
     */
    public static void publishEvent(EventContext eventContext) {
        if (eventBus != null) {
            try {
                eventBus.publish(eventContext);
            } catch (Exception e) {
                log.error("发布事件失败: eventType={}", eventContext.getEventType(), e);
            }
        }
    }

    /**
     * 发布事件（异步）
     */
    public static CompletableFuture<Void> publishEventAsync(EventContext eventContext) {
        if (eventBus != null) {
            try {
                return eventBus.publishAsync(eventContext);
            } catch (Exception e) {
                log.error("异步发布事件失败: eventType={}", eventContext.getEventType(), e);
                CompletableFuture<Void> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
        }
        return CompletableFuture.completedFuture(null);
    }

} 