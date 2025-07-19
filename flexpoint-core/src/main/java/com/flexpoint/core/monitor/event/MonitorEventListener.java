package com.flexpoint.core.monitor.event;

import com.flexpoint.core.monitor.enums.EventType;

import java.util.Map;

/**
 * 监控事件监听器扩展接口
 * @author xiangganluo
 */
public interface MonitorEventListener {

    /**
     * 监控事件回调
     * @param eventType 事件类型
     * @param extensionId 扩展点ID
     * @param context 业务上下文
     */
    void onEvent(EventType eventType, String extensionId, Map<String, Object> context);
} 