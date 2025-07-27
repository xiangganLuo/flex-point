package com.flexpoint.core.event;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.utils.ExtUtil;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扩展点事件上下文
 * 携带扩展点事件相关的所有信息
 *
 * @author xiangganluo
 */
@Data
@Builder
public class EventContext {
    
    /**
     * 事件ID，全局唯一
     */
    private String eventId;
    
    /**
     * 事件类型
     */
    private EventType eventType;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime timestamp;
    
    /**
     * 扩展点类型
     */
    private Class<? extends ExtAbility> extType;
    
    /**
     * 扩展点实例
     */
    private ExtAbility extAbility;
    
    /**
     * 扩展点代码
     */
    private String extCode;
    
    /**
     * 选择器名称
     */
    private String selectorName;
    
    /**
     * 调用方法名
     */
    private String methodName;
    
    /**
     * 调用参数
     */
    private Object[] methodArgs;
    
    /**
     * 调用结果
     */
    private Object result;
    
    /**
     * 异常信息
     */
    private Throwable exception;
    
    /**
     * 调用耗时（毫秒）
     */
    private Long duration;
    
    /**
     * 自定义属性
     */
    private Map<String, Object> attributes;
    
    /**
     * 线程信息
     */
    private String threadName;
    
    /**
     * 创建事件上下文
     */
    public static EventContext create(EventType eventType) {
        return EventContext.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(eventType)
            .timestamp(LocalDateTime.now())
            .threadName(Thread.currentThread().getName())
            .attributes(new ConcurrentHashMap<>())
            .build();
    }
    
    /**
     * 创建扩展点相关事件上下文
     */
    public static EventContext createExtEvent(EventType eventType, ExtAbility extAbility) {
        return EventContext.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(eventType)
            .timestamp(LocalDateTime.now())
            .extType(extAbility != null ? extAbility.getClass() : null)
            .extAbility(extAbility)
            .extCode(extAbility != null ? extAbility.getCode() : null)
            .threadName(Thread.currentThread().getName())
            .attributes(new ConcurrentHashMap<>())
            .build();
    }
    
    /**
     * 创建调用相关事件上下文
     */
    public static EventContext createInvokeEvent(EventType eventType, ExtAbility extAbility, 
                                               String methodName, Object[] args, Object result, 
                                               Throwable exception, Long duration) {
        return EventContext.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(eventType)
            .timestamp(LocalDateTime.now())
            .extType(extAbility != null ? extAbility.getClass() : null)
            .extAbility(extAbility)
            .extCode(extAbility != null ? extAbility.getCode() : null)
            .methodName(methodName)
            .methodArgs(args)
            .result(result)
            .exception(exception)
            .duration(duration)
            .threadName(Thread.currentThread().getName())
            .attributes(new ConcurrentHashMap<>())
            .build();
    }
    
    /**
     * 添加自定义属性
     */
    public EventContext withAttribute(String key, Object value) {
        if (this.attributes == null) {
            this.attributes = new ConcurrentHashMap<>();
        }
        this.attributes.put(key, value);
        return this;
    }
    
    /**
     * 获取自定义属性
     */
    public Object getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }

    /**
     * 获取扩展点ID
     */
    public String getExtId() {
        return ExtUtil.getExtId(extAbility);
    }
} 