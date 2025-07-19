package com.flexpoint.core.config;

import lombok.Data;

/**
 * Flex Point框架配置
 * 提供框架的核心配置支持
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Data
public class FlexPointConfig {
    
    /**
     * 是否启用Flex Point框架
     */
    private boolean enabled = true;

    /**
     * 监控配置
     */
    private MonitorConfig monitor = new MonitorConfig();

    /**
     * 注册配置
     */
    private RegistryConfig registry = new RegistryConfig();
    
    /**
     * 监控配置
     */
    @Data
    public static class MonitorConfig {
        /**
         * 是否启用监控
         */
        private boolean enabled = true;
        
        /**
         * 是否记录扩展点调用日志
         */
        private boolean logInvocation = true;
        
        /**
         * 是否记录扩展点选择日志
         */
        private boolean logSelection = true;

        /**
         * 是否记录异常详情
         */
        private boolean logExceptionDetails = true;

        /**
         * 是否启用性能统计
         */
        private boolean performanceStatsEnabled = true;

        /**
         * 是否启用异步处理
         */
        private boolean asyncEnabled = false;
        
        /**
         * 异步处理队列大小
         */
        private int asyncQueueSize = 1000;
        
        /**
         * 异步监控核心线程数
         */
        private int asyncCorePoolSize = 2;
        
        /**
         * 异步监控最大线程数
         */
        private int asyncMaxPoolSize = 4;
        
        /**
         * 异步监控线程保活时间（秒）
         */
        private long asyncKeepAliveTime = 60L;
        
    }
    
    /**
     * 注册配置
     */
    @Data
    public static class RegistryConfig {

        /**
         * 是否启用自动注册
         */
        private boolean enabled = true;

    }

    /**
     * 创建默认配置
     */
    public static FlexPointConfig defaultConfig() {
        return new FlexPointConfig();
    }
    
}