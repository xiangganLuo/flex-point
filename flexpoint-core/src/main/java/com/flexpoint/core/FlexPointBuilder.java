package com.flexpoint.core;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.config.FlexPointConfigValidator;
import com.flexpoint.core.event.DefaultEventBus;
import com.flexpoint.core.event.EventDispatcher;
import com.flexpoint.core.ext.DefaultExtAbilityRegistry;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.monitor.MonitorFactory;
import com.flexpoint.core.selector.DefaultSelectorRegistry;
import com.flexpoint.core.selector.SelectorRegistry;
import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.manage.DefaultPluginManager;
import com.flexpoint.core.plugin.manage.PluginManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private EventDispatcher eventDispatcher;
    private FlexPointConfig config;

    // 插件相关
    private final List<Plugin> plugins = new CopyOnWriteArrayList<>();
    
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
     * 使用自定义事件分发器
     */
    public FlexPointBuilder withEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
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
     * 增加单个插件
     */
    public FlexPointBuilder withPlugin(Plugin plugin) {
        if (plugin != null) this.plugins.add(plugin);
        return this;
    }

    /**
     * 批量增加插件
     */
    public FlexPointBuilder withPlugins(Collection<Plugin> plugins) {
        if (plugins != null) this.plugins.addAll(plugins);
        return this;
    }
    
    /**
     * 构建FlexPoint实例
     */
    public FlexPoint build() {
        // 如果没有配置，使用默认配置
        FlexPointConfig resolvedConfig = config;
        if (resolvedConfig == null) {
            resolvedConfig = FlexPointConfig.defaultConfig();
            log.debug("使用默认配置");
        }
        
        // 如果框架被禁用，抛出异常
        if (!resolvedConfig.isEnabled()) {
            throw new IllegalStateException("Flex Point框架已禁用，无法构建实例");
        }

        // 使用默认组件（如果未指定）
        // 注意：默认 dispatcher 每次 build 都新建，确保多实例隔离；
        // 仅当显式 withEventDispatcher(...) 时才复用
        EventDispatcher resolvedEventDispatcher;
        if (this.eventDispatcher != null) {
            resolvedEventDispatcher = this.eventDispatcher;
        } else {
            resolvedEventDispatcher = FlexPointComponentCreator.createEventDispatcher();
        }

        ExtAbilityRegistry resolvedRegistry;
        if (this.registry != null) {
            resolvedRegistry = this.registry;
        } else {
            resolvedRegistry = FlexPointComponentCreator.createRegistry(resolvedConfig.getRegistry(), resolvedEventDispatcher);
        }

        ExtMonitor resolvedMonitor;
        if (this.monitor != null) {
            resolvedMonitor = this.monitor;
        } else {
            resolvedMonitor = FlexPointComponentCreator.createMonitor(resolvedConfig.getMonitor());
        }

        SelectorRegistry resolvedSelectorRegistry;
        if (this.selectorRegistry != null) {
            resolvedSelectorRegistry = this.selectorRegistry;
        } else {
            resolvedSelectorRegistry = FlexPointComponentCreator.createSelectorRegistry(resolvedEventDispatcher);
        }

        // 如果未提供插件，保持历史行为
        if (plugins.isEmpty()) {
            return new FlexPoint(resolvedRegistry, resolvedMonitor, resolvedSelectorRegistry, resolvedEventDispatcher, resolvedConfig);
        }

        // 插件装配
        PluginManager pm = new DefaultPluginManager(
                resolvedRegistry, resolvedSelectorRegistry, resolvedEventDispatcher.getEventBus(), resolvedMonitor, resolvedConfig);
        pm.registerAll(plugins);
        pm.resolve();
        pm.installAll();

        return new FlexPoint(resolvedRegistry, resolvedMonitor, resolvedSelectorRegistry, resolvedEventDispatcher, resolvedConfig);
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
        public static ExtAbilityRegistry createRegistry(FlexPointConfig.RegistryConfig registryConfig, EventDispatcher eventDispatcher) {
            return new DefaultExtAbilityRegistry(registryConfig, eventDispatcher);
        }

        /**
         * 根据配置创建监控器
         */
        public static ExtMonitor createMonitor(FlexPointConfig.MonitorConfig monitorConfig) {
            return MonitorFactory.createDefaultMonitor(monitorConfig);
        }

        /**
         * 创建选择器注册表
         */
        public static SelectorRegistry createSelectorRegistry(EventDispatcher eventDispatcher) {
            return new DefaultSelectorRegistry(eventDispatcher);
        }

        /**
         * 创建默认事件总线
         */
        public static EventDispatcher createEventDispatcher() {
            return new EventDispatcher(new DefaultEventBus());
        }

    }

}
