package com.flexpoint.core.event.filter;

import com.flexpoint.core.event.EventContext;

import java.util.Arrays;
import java.util.List;

/**
 * 组合事件过滤器
 * 支持多个过滤条件的组合
 *
 * @author xiangganluo
 */
public class CompositeEventFilter implements EventFilter {
    
    private final List<EventFilter> filters;

    /**
     * true=AND逻辑，false=OR逻辑
     */
    private final boolean and;
    
    public CompositeEventFilter(List<EventFilter> filters, boolean and) {
        this.filters = filters;
        this.and = and;
    }
    
    public CompositeEventFilter(EventFilter... filters) {
        this(Arrays.asList(filters), true);
    }
    
    @Override
    public boolean matches(EventContext eventContext) {
        if (filters.isEmpty()) {
            return true;
        }
        
        if (and) {
            // AND逻辑：所有过滤器都必须匹配
            return filters.stream().allMatch(filter -> filter.matches(eventContext));
        } else {
            // OR逻辑：任一过滤器匹配即可
            return filters.stream().anyMatch(filter -> filter.matches(eventContext));
        }
    }
    
    /**
     * 创建AND组合过滤器
     */
    public static CompositeEventFilter and(EventFilter... filters) {
        return new CompositeEventFilter(Arrays.asList(filters), true);
    }
    
    /**
     * 创建OR组合过滤器
     */
    public static CompositeEventFilter or(EventFilter... filters) {
        return new CompositeEventFilter(Arrays.asList(filters), false);
    }
    
    /**
     * 添加过滤器（AND逻辑）
     */
    public CompositeEventFilter and(EventFilter filter) {
        filters.add(filter);
        return this;
    }
    
    /**
     * 添加过滤器（OR逻辑）
     */
    public CompositeEventFilter or(EventFilter filter) {
        filters.add(filter);
        return this;
    }
} 