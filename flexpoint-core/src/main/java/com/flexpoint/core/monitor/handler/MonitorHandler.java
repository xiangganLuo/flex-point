package com.flexpoint.core.monitor.handler;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.ExtMetrics;

/**
 * 监控责任链节点接口
 *
 * @author xiangganluo
 */
public interface MonitorHandler {
    /**
     * 处理扩展点调用
     */
    void handleInvocation(ExtAbility extAbility, long duration, boolean success, ExtMetrics metrics);

    /**
     * 处理扩展点异常
     */
    void handleException(ExtAbility extAbility, Throwable exception, ExtMetrics metrics);
} 