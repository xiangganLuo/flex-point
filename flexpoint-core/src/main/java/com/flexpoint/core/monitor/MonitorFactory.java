package com.flexpoint.core.monitor;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.monitor.handler.MonitorHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 监控工厂类
 * 根据配置创建不同类型的监控器
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class MonitorFactory {

    /**
     * 创建监控器
     *
     * @param config 监控配置
     * @param handlers 处理器链
     * @return 监控器实例
     */
    public static ExtMonitor createMonitor(FlexPointConfig.MonitorConfig config, List<MonitorHandler> handlers) {
        ExtMonitor monitor;
        
        if (config.isAsyncEnabled()) {
            log.info("创建异步监控器");
            monitor = new AsyncExtMonitor(config);
        } else {
            log.info("创建同步监控器");
            monitor = new DefaultExtMonitor(config);
        }
        
        // 添加处理器到监控器
        if (handlers != null) {
            for (MonitorHandler handler : handlers) {
                monitor.addHandler(handler);
            }
        }
        
        return monitor;
    }

    /**
     * 创建默认监控器
     *
     * @param config 监控配置
     * @return 监控器实例
     */
    public static ExtMonitor createDefaultMonitor(FlexPointConfig.MonitorConfig config) {
        return createMonitor(config, null);
    }
} 