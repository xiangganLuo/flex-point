package com.flexpoint.core.event.router;

import com.flexpoint.core.event.EventContext;
import com.flexpoint.core.event.EventSubscriber;

import java.util.List;

/**
 * <p>DefaultEventRouter</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public class DefaultEventRouter implements EventRouter {
    @Override
    public List<EventSubscriber> route(EventContext eventContext, List<EventSubscriber> allSubscribers) {
        return allSubscribers;
    }
}
