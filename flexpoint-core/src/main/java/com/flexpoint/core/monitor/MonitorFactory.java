package com.flexpoint.core.monitor;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.monitor.enums.MonitorType;
import lombok.extern.slf4j.Slf4j;

/**
 * 监控工厂类
 * 提供不同类型的监控器创建方法
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class MonitorFactory {
    
    /**
     * 创建默认监控器
     */
    public static ExtMonitor create() {
        return create(new FlexPointConfig.MonitorConfig());
    }
    
    /**
     * 创建默认监控器
     */
    public static ExtMonitor create(FlexPointConfig.MonitorConfig config) {
        return new DefaultExtMonitor(config);
    }
    
    /**
     * 创建异步监控器
     */
    public static ExtMonitor createAsync() {
        return createAsync(new FlexPointConfig.MonitorConfig());
    }
    
    /**
     * 创建异步监控器
     */
    public static ExtMonitor createAsync(FlexPointConfig.MonitorConfig config) {
        return new AsyncExtMonitor(new DefaultExtMonitor(config));
    }
    
    /**
     * 根据类型创建监控器
     */
    public static ExtMonitor create(MonitorType type) {
        return create(type, new FlexPointConfig.MonitorConfig());
    }
    
    /**
     * 根据类型创建监控器
     */
    public static ExtMonitor create(MonitorType type, FlexPointConfig.MonitorConfig config) {
        if (config.isEnabled()) {
            log.info("创建监控器: logInvocation={}, isLogSelection={}, logExceptionDetails={}, performanceStatsEnabled={}, asyncEnabled={}",
                    config.isLogInvocation(), config.isLogSelection(),
                    config.isLogExceptionDetails(), config.isPerformanceStatsEnabled(),
                    config.isAsyncEnabled()
            );
        }
        switch (type) {
            case DEFAULT:
                return create(config);
            case ASYNC:
                return createAsync(config);
            default:
                log.warn("未知的监控器类型: {}, 使用默认监控器", type);
                return create(config);
        }
    }

    /**
     * 根据配置自动选择合适的监控器类型
     */
    public static ExtMonitor createAuto(FlexPointConfig.MonitorConfig config) {
        if (config.isAsyncEnabled()) {
            return createAsync(config);
        } else {
            return create(config);
        }
    }
} 