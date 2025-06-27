package com.flexpoint.core;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.config.FlexPointConfigValidator;
import com.flexpoint.core.extension.ExtensionAbilityFactory;
import com.flexpoint.core.monitor.DefaultExtensionMonitor;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.registry.DefaultExtensionRegistry;
import com.flexpoint.core.registry.ExtensionRegistry;
import com.flexpoint.core.resolution.DefaultExtensionResolverFactory;
import com.flexpoint.core.resolution.ExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ExtensionResolverFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * FlexPoint 建造者
 * 提供流式API来构建和配置FlexPoint实例
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class FlexPointBuilder {
    
    private ExtensionRegistry registry;
    private ExtensionMonitor monitor;
    private ExtensionResolverFactory resolverFactory;
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
     * 使用默认配置构建
     */
    public static FlexPointBuilder createWithDefaultConfig() {
        return create(FlexPointConfig.defaultConfig());
    }
    
    /**
     * 使用自定义注册中心
     */
    public FlexPointBuilder withRegistry(ExtensionRegistry registry) {
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
     * 使用自定义解析器工厂
     */
    public FlexPointBuilder withResolverFactory(ExtensionResolverFactory resolverFactory) {
        this.resolverFactory = resolverFactory;
        return this;
    }
    
    /**
     * 注册自定义解析器
     */
    public FlexPointBuilder withResolver(ExtensionResolutionStrategy resolver) {
        if (this.resolverFactory == null) {
            this.resolverFactory = new DefaultExtensionResolverFactory();
        }
        this.resolverFactory.registerResolver(resolver);
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
     * 构建FlexPoint实例
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
        
        if (resolverFactory == null) {
            resolverFactory = FlexPointComponentCreator.createResolverFactory();
        }
        
        ExtensionAbilityFactory factory = new ExtensionAbilityFactory(registry, monitor, resolverFactory);
        return new FlexPoint(factory, registry, monitor, resolverFactory, config);
    }

    /**
     * Flex Point组件工厂
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
        public static ExtensionRegistry createRegistry(FlexPointConfig.RegistryConfig registryConfig) {
            return new DefaultExtensionRegistry(registryConfig);
        }

        /**
         * 根据配置创建监控器
         */
        public static ExtensionMonitor createMonitor(FlexPointConfig.MonitorConfig monitorConfig) {
            return new DefaultExtensionMonitor(monitorConfig);
        }

        /**
         * 创建解析器工厂
         */
        public static ExtensionResolverFactory createResolverFactory() {
            return new DefaultExtensionResolverFactory();
        }
    }

} 