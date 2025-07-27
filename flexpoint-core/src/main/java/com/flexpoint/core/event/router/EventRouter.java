package com.flexpoint.core.event.router;

import com.flexpoint.core.event.EventContext;
import com.flexpoint.core.event.EventSubscriber;

import java.util.List;

/**
 * 事件路由器接口
 * 负责根据事件上下文决定将事件路由到哪些订阅者
 *
 * @author xiangganluo
 */
public interface EventRouter {
    
    /**
     * 根据事件上下文决定路由到哪些订阅者
     *
     * @param eventContext 事件上下文
     * @param allSubscribers 所有订阅者列表
     * @return 应该接收此事件的订阅者列表
     */
    List<EventSubscriber> route(EventContext eventContext, List<EventSubscriber> allSubscribers);

}