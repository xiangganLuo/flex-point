package com.flexpoint.core.event.filter;

import com.flexpoint.core.event.EventContext;

/**
 * 事件过滤器接口
 * 用于过滤事件，决定是否将事件分发给订阅者
 *
 * @author xiangganluo
 */
@FunctionalInterface
public interface EventFilter {
    
    /**
     * 判断事件是否匹配
     *
     * @param eventContext 事件上下文
     * @return true=匹配，false=过滤
     */
    boolean matches(EventContext eventContext);
    
    /**
     * 创建始终匹配的过滤器
     */
    static EventFilter always() {
        return eventContext -> true;
    }
    
    /**
     * 创建始终不匹配的过滤器
     */
    static EventFilter never() {
        return eventContext -> false;
    }
    
    /**
     * 创建事件类型过滤器
     */
    static EventFilter byEventType(com.flexpoint.core.event.EventType eventType) {
        return eventContext -> eventContext.getEventType() == eventType;
    }
    
    /**
     * 创建扩展点类型过滤器
     */
    static EventFilter byExtType(Class<?> extType) {
        return eventContext -> extType.equals(eventContext.getExtType());
    }
    
    /**
     * 创建扩展点代码过滤器
     */
    static EventFilter byExtCode(String extCode) {
        return eventContext -> extCode.equals(eventContext.getExtCode());
    }
    
    /**
     * 创建选择器名称过滤器
     */
    static EventFilter bySelectorName(String selectorName) {
        return eventContext -> selectorName.equals(eventContext.getSelectorName());
    }
    
    /**
     * 创建属性过滤器
     */
    static EventFilter byAttribute(String key, Object value) {
        return eventContext -> value.equals(eventContext.getAttribute(key));
    }
} 