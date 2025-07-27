package com.flexpoint.core.event;

import com.flexpoint.core.event.filter.EventFilter;

/**
 * 事件订阅者接口
 * 处理具体的事件逻辑
 *
 * @author xiangganluo
 */
public interface EventSubscriber{
    
    /**
     * 处理事件
     *
     * @param eventContext 事件上下文
     */
    void onEvent(EventContext eventContext);
    
    /**
     * 获取订阅者名称
     * 用于标识和调试
     *
     * @return 订阅者名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 获取订阅者优先级
     * 数值越小优先级越高，默认为0
     *
     * @return 优先级
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * 是否异步处理
     * 默认为false，同步处理
     *
     * @return 是否异步
     */
    default boolean isAsync() {
        return false;
    }
    
    /**
     * 是否启用
     * 默认为true，启用状态
     *
     * @return 是否启用
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * 获取事件过滤器
     * 用于过滤事件，决定是否接收此事件
     * 默认为null，表示接收所有事件
     *
     * @return 事件过滤器
     */
    default EventFilter getEventFilter() {
        return EventFilter.always();
    }

}