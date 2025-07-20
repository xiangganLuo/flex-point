package com.flexpoint.core;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.config.FlexPointConfigValidator;
import com.flexpoint.core.ext.DefaultExtAbilityRegistry;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.monitor.MonitorFactory;
import com.flexpoint.core.selector.DefaultSelectorRegistry;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * 扩展点管理器建造者
 * 提供流式API来构建和配置FlexPoint实例
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class FlexPointBuilder {
    
    private ExtAbilityRegistry registry;
    private ExtMonitor monitor;
    private SelectorRegistry selectorRegistry;
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
    public FlexPointBuilder withRegistry(ExtAbilityRegistry registry) {
        this.registry = registry;
        return this;
    }
    
    /**
     * 使用自定义监控器
     */
    public FlexPointBuilder withMonitor(ExtMonitor monitor) {
        this.monitor = monitor;
        return this;
    }
    
    /**
     * 使用自定义选择器注册表
     */
    public FlexPointBuilder withSelectorRegistry(SelectorRegistry selectorRegistry) {
        this.selectorRegistry = selectorRegistry;
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
        
        if (selectorRegistry == null) {
            selectorRegistry = FlexPointComponentCreator.createSelectorRegistry();
        }
        
        return new FlexPoint(registry, monitor, selectorRegistry, config);
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
        public static ExtAbilityRegistry createRegistry(FlexPointConfig.RegistryConfig registryConfig) {
            return new DefaultExtAbilityRegistry(registryConfig);
        }

        /**
         * 根据配置创建监控器
         */
        public static ExtMonitor createMonitor(FlexPointConfig.MonitorConfig monitorConfig) {
            return MonitorFactory.createAuto(monitorConfig);
        }

        /**
         * 创建选择器注册表
         */
        public static SelectorRegistry createSelectorRegistry() {
            return new DefaultSelectorRegistry();
        }

    }

}