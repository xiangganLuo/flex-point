package com.flexpoint.core.monitor.event;

import com.flexpoint.core.monitor.enums.EventType;

/**
 * 监控事件监听器扩展接口
 * @author xiangganluo
 */
public interface MonitorEventListener {

    /**
     * 监控事件回调
     * @param eventType 事件类型
     * @param ext 扩展点ID
     */
    void onEvent(EventType eventType, String ext);
} 