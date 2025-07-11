package com.flexpoint.core.config;

import lombok.Data;

import java.util.List;
import java.util.Map;

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
     * 选择器配置
     * defaultChain: [tenantSelector, versionSelector, defaultSelector]
     */
    private SelectorConfig selector = new SelectorConfig();
    
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
        
        /**
         * 是否允许重复注册
         */
        private boolean allowDuplicateRegistration = false;
    }

    @Data
    public static class SelectorConfig {

        /**
         * 选择器链
         */
        private Map<String, List<String>> chains;

    }

    /**
     * 创建默认配置
     */
    public static FlexPointConfig defaultConfig() {
        return new FlexPointConfig();
    }
    
}