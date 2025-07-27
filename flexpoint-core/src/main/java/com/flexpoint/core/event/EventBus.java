package com.flexpoint.core.event;

import com.flexpoint.core.event.router.EventRouter;

import java.util.concurrent.CompletableFuture;

/**
 * 事件总线接口
 * 负责事件的发布和订阅管理
 *
 * @author xiangganluo
 */
public interface EventBus {
    
    /**
     * 发布事件（同步）
     *
     * @param eventContext 事件上下文
     */
    void publish(EventContext eventContext);
    
    /**
     * 发布事件（异步）
     *
     * @param eventContext 事件上下文
     * @return CompletableFuture，用于异步处理结果
     */
    CompletableFuture<Void> publishAsync(EventContext eventContext);
    
    /**
     * 订阅事件
     *
     * @param eventType 事件类型
     * @param subscriber 事件订阅者
     */
    void subscribe(EventSubscriber subscriber);
    
    /**
     * 取消所有订阅
     *
     * @param subscriber 事件订阅者
     */
    void unsubscribe(EventSubscriber subscriber);
    
    /**
     * 获取总订阅者数量
     *
     * @return 总订阅者数量
     */
    int getTotalSubscriberCount();
    
    /**
     * 设置事件路由器
     *
     * @param router 事件路由器
     */
    void setEventRouter(EventRouter router);
    
    /**
     * 获取事件路由器
     *
     * @return 事件路由器
     */
    EventRouter getEventRouter();
    
    /**
     * 清空所有订阅
     */
    void clear();
    
    /**
     * 关闭事件总线
     */
    void shutdown();
} 