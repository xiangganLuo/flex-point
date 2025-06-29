package com.flexpoint.core;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.config.FlexPointConfigValidator;
import com.flexpoint.core.monitor.DefaultExtensionMonitor;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.extension.DefaultExtensionAbilityRegistry;
import com.flexpoint.core.extension.ExtensionAbilityRegistry;
import com.flexpoint.core.resolution.DefaultExtensionResolutionStrategyRegistry;
import com.flexpoint.core.resolution.ExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ExtensionResolutionStrategyRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * 扩展点管理器建造者
 * 提供流式API来构建和配置FlexPointManager实例
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class FlexPointBuilder {
    
    private ExtensionAbilityRegistry registry;
    private ExtensionMonitor monitor;
    private ExtensionResolutionStrategyRegistry strategyRegistry;
    private FlexPointConfig config;
    
    /**
     * 使用默认组件构建
     */
    public static FlexPointBuilder create() {
        return new FlexPointBuilder();
    }
    
    /**
     * 使用配置构建
     */
    public static FlexPointBuilder create(FlexPointConfig config) {
        FlexPointBuilder builder = new FlexPointBuilder();
        builder.config = FlexPointConfigValidator.validateAndProcess(config);
        return builder;
    }
    
    /**
     * 使用自定义注册中心
     */
    public FlexPointBuilder withRegistry(ExtensionAbilityRegistry registry) {
        this.registry = registry;
        return this;
    }
    
    /**
     * 使用自定义监控器
     */
    public FlexPointBuilder withMonitor(ExtensionMonitor monitor) {
        this.monitor = monitor;
        return this;
    }
    
    /**
     * 使用自定义解析策略注册表
     */
    public FlexPointBuilder withStrategyRegistry(ExtensionResolutionStrategyRegistry strategyRegistry) {
        this.strategyRegistry = strategyRegistry;
        return this;
    }
    
    /**
     * 注册自定义解析策略
     */
    public FlexPointBuilder withResolver(ExtensionResolutionStrategy resolver) {
        if (this.strategyRegistry == null) {
            this.strategyRegistry = new DefaultExtensionResolutionStrategyRegistry();
        }
        this.strategyRegistry.registerStrategy(resolver);
        return this;
    }
    
    /**
     * 使用配置
     */
    public FlexPointBuilder withConfig(FlexPointConfig config) {
        this.config = FlexPointConfigValidator.validateAndProcess(config);
        return this;
    }
    
    /**
     * 构建FlexPointManager实例
     */
    public FlexPoint build() {
        // 如果没有配置，使用默认配置
        if (config == null) {
            config = FlexPointConfig.defaultConfig();
            log.debug("使用默认配置");
        }
        
        // 如果框架被禁用，抛出异常
        if (!config.isEnabled()) {
            throw new IllegalStateException("Flex Point框架已禁用，无法构建实例");
        }
        
        // 使用默认组件（如果未指定）
        if (registry == null) {
            registry = FlexPointComponentCreator.createRegistry(config.getRegistry());
        }

        if (monitor == null) {
            monitor = FlexPointComponentCreator.createMonitor(config.getMonitor());
        }
        
        if (strategyRegistry == null) {
            strategyRegistry = FlexPointComponentCreator.createStrategyRegistry();
        }
        
        return new FlexPoint(registry, monitor, strategyRegistry);
    }

    /**
     * 扩展点组件工厂
     * 根据配置创建不同的组件实例
     *
     * @author xiangganluo
     * @version 1.0.0
     */
    @Slf4j
    public static class FlexPointComponentCreator {

        /**
         * 根据配置创建注册中心
         */
        public static ExtensionAbilityRegistry createRegistry(FlexPointConfig.RegistryConfig registryConfig) {
            return new DefaultExtensionAbilityRegistry(registryConfig);
        }

        /**
         * 根据配置创建监控器
         */
        public static ExtensionMonitor createMonitor(FlexPointConfig.MonitorConfig monitorConfig) {
            return new DefaultExtensionMonitor(monitorConfig);
        }

        /**
         * 创建解析策略注册表
         */
        public static ExtensionResolutionStrategyRegistry createStrategyRegistry() {
            return new DefaultExtensionResolutionStrategyRegistry();
        }
    }

} 