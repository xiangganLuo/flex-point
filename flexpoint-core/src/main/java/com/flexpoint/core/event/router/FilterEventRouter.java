package com.flexpoint.core.event.router;

import com.flexpoint.core.event.EventContext;
import com.flexpoint.core.event.EventSubscriber;
import com.flexpoint.core.event.filter.EventFilter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于过滤器的事件路由器
 * 根据订阅者的过滤器决定是否将事件路由给订阅者
 *
 * @author xiangganluo
 */
public class FilterEventRouter implements EventRouter {
    
    @Override
    public List<EventSubscriber> route(EventContext eventContext, List<EventSubscriber> allSubscribers) {
        return allSubscribers.stream()
                .filter(subscriber -> {
                    EventFilter filter = subscriber.getEventFilter();
                    return filter == null || filter.matches(eventContext);
                })
                .collect(Collectors.toList());
    }
}
