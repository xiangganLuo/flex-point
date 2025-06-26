package com.flexpoint.core;

import com.flexpoint.core.cache.DefaultExtensionCacheManager;
import com.flexpoint.core.cache.ExtensionCacheManager;
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
    private ExtensionCacheManager cacheManager;
    private ExtensionMonitor monitor;
    private ExtensionResolverFactory resolverFactory;
    
    /**
     * 使用默认组件构建
     */
    public static FlexPointBuilder create() {
        return new FlexPointBuilder();
    }
    
    /**
     * 使用自定义注册中心
     */
    public FlexPointBuilder withRegistry(ExtensionRegistry registry) {
        this.registry = registry;
        return this;
    }
    
    /**
     * 使用自定义缓存管理器
     */
    public FlexPointBuilder withCacheManager(ExtensionCacheManager cacheManager) {
        this.cacheManager = cacheManager;
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
     * 构建FlexPoint实例
     */
    public FlexPoint build() {
        // 使用默认组件（如果未指定）
        if (registry == null) {
            registry = new DefaultExtensionRegistry();
            log.debug("使用默认注册中心: DefaultExtensionRegistry");
        }
        
        if (cacheManager == null) {
            cacheManager = new DefaultExtensionCacheManager();
            log.debug("使用默认缓存管理器: DefaultExtensionCacheManager");
        }
        
        if (monitor == null) {
            monitor = new DefaultExtensionMonitor();
            log.debug("使用默认监控器: DefaultExtensionMonitor");
        }
        
        if (resolverFactory == null) {
            resolverFactory = new DefaultExtensionResolverFactory();
            log.debug("使用默认解析器工厂: DefaultExtensionResolverFactory");
        }
        
        ExtensionAbilityFactory factory = new ExtensionAbilityFactory(registry, cacheManager, monitor, resolverFactory);
        return new FlexPoint(factory, registry, cacheManager, monitor, resolverFactory);
    }
} 