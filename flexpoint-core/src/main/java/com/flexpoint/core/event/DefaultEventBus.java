package com.flexpoint.core.event;

import com.flexpoint.core.event.router.EventRouter;
import com.flexpoint.core.event.router.FilterEventRouter;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 默认事件总线实现
 * 提供完整的事件发布订阅功能，支持过滤和路由
 *
 * @author xiangganluo
 */
@Slf4j
public class DefaultEventBus implements EventBus {
    
    /**
     * 事件类型 -> 订阅者列表
     */
    private final List<EventSubscriber> subscribers = new CopyOnWriteArrayList<>();
    
    /**
     * 异步处理线程池
     */
    private final ExecutorService asyncExecutor;
    
    /**
     * 是否已关闭
     */
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    
    /**
     * 事件路由器
     */
    private EventRouter eventRouter = new FilterEventRouter();
    
    public DefaultEventBus() {
        // TODO 后续需要支持FlexPointConfig进行配置
        this(Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "flexpoint-async-event-" + Thread.currentThread().getId());
            t.setDaemon(true);
            return t;
        }));
    }
    
    public DefaultEventBus(ExecutorService asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }
    
    @Override
    public void publish(EventContext eventContext) {
        if (shutdown.get()) {
            log.warn("事件总线已关闭，忽略事件: {}", eventContext.getEventType());
            return;
        }

        if (eventContext == null || eventContext.getEventType() == null) {
            log.warn("事件上下文或事件类型为空，忽略事件");
            return;
        }

        if (subscribers.isEmpty()) {
            return;
        }
        
        // 使用路由器决定哪些订阅者接收此事件
        List<EventSubscriber> targetSubscribers = eventRouter.route(eventContext, subscribers);
        
        if (targetSubscribers.isEmpty()) {
            log.debug("事件类型[{}]经过路由后没有匹配的订阅者", eventContext.getEventType());
            return;
        }
        
        // 按优先级排序
        List<EventSubscriber> sortedSubscribers = targetSubscribers.stream()
            .filter(EventSubscriber::isEnabled)
            .sorted(Comparator.comparingInt(EventSubscriber::getPriority))
            .collect(Collectors.toList());
        
        for (EventSubscriber subscriber : sortedSubscribers) {
            try {
                if (subscriber.isAsync()) {
                    // 异步处理
                    asyncExecutor.submit(() -> handleEvent(subscriber, eventContext));
                } else {
                    // 同步处理
                    handleEvent(subscriber, eventContext);
                }
            } catch (Exception e) {
                log.error("发布事件失败: eventType={}, subscriber={}", 
                    eventContext.getEventType(), subscriber.getName(), e);
            }
        }
    }
    
    @Override
    public CompletableFuture<Void> publishAsync(EventContext eventContext) {
        return CompletableFuture.runAsync(() -> publish(eventContext), asyncExecutor);
    }

    @Override
    public void subscribe(EventSubscriber subscriber) {
        if (subscriber == null) {
            return;
        }
        subscribers.add(subscriber);
        log.info("订阅成功: subscriber={}", subscriber.getName());
    }

    @Override
    public void unsubscribe(EventSubscriber subscriber) {
        if (subscriber == null) {
            return;
        }
        subscribers.remove(subscriber);
        log.info("取消订阅成功: subscriber={}", subscriber.getName());
    }
    
    @Override
    public int getTotalSubscriberCount() {
        return subscribers.size();
    }
    
    @Override
    public void setEventRouter(EventRouter router) {
        this.eventRouter = router;
    }
    
    @Override
    public EventRouter getEventRouter() {
        return eventRouter;
    }
    
    @Override
    public void clear() {
        subscribers.clear();
    }
    
    @Override
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            asyncExecutor.shutdown();
            log.info("事件总线已关闭");
        }
    }
    
    /**
     * 处理单个事件
     */
    private void handleEvent(EventSubscriber subscriber, EventContext eventContext) {
        try {
            subscriber.onEvent(eventContext);
            log.debug("事件处理成功: eventType={}, subscriber={}", eventContext.getEventType(), subscriber.getName());
        } catch (Exception e) {
            log.error("事件处理失败: eventType={}, subscriber={}", eventContext.getEventType(), subscriber.getName(), e);
        }
    }

}