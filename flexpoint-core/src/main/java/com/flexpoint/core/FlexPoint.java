package com.flexpoint.core;

import com.flexpoint.common.ExtensionAbility;
import com.flexpoint.core.cache.ExtensionCacheManager;
import com.flexpoint.core.metadata.ExtensionMetadata;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.registry.ExtensionRegistry;
import com.flexpoint.core.resolution.ExtensionResolverFactory;
import com.flexpoint.core.resolution.ExtensionResolutionStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * FlexPoint 门面类
 * 提供简洁的API来操作扩展点
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class FlexPoint {
    
    private final ExtensionAbilityFactory factory;
    private final ExtensionRegistry registry;
    private final ExtensionCacheManager cacheManager;
    private final ExtensionMonitor monitor;
    private final ExtensionResolverFactory resolverFactory;
    
    FlexPoint(ExtensionAbilityFactory factory, ExtensionRegistry registry, 
              ExtensionCacheManager cacheManager, ExtensionMonitor monitor, ExtensionResolverFactory resolverFactory) {
        this.factory = factory;
        this.registry = registry;
        this.cacheManager = cacheManager;
        this.monitor = monitor;
        this.resolverFactory = resolverFactory;
    }
    
    /**
     * 查找扩展点
     */
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType) {
        return factory.findAbility(extensionType);
    }
    
    /**
     * 查找扩展点（带上下文）
     */
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType, Map<String, Object> context) {
        return factory.findAbility(extensionType, context);
    }
    
    /**
     * 查找扩展点（返回Optional）
     */
    public <T extends ExtensionAbility> Optional<T> findAbilityOpt(Class<T> extensionType) {
        return factory.findAbilityOpt(extensionType);
    }
    
    /**
     * 根据ID查找扩展点
     */
    public <T extends ExtensionAbility> T findAbilityById(Class<T> extensionType, String extensionId) {
        return factory.findAbilityById(extensionType, extensionId);
    }
    
    /**
     * 注册扩展点
     */
    public <T extends ExtensionAbility> void register(Class<T> extensionType, T extension, ExtensionMetadata metadata) {
        registry.register(extensionType, extension, metadata);
        log.info("注册扩展点: type={}, class={}", extensionType.getSimpleName(), extension.getClass().getName());
    }
    
    /**
     * 注册扩展点（无元数据）
     */
    public <T extends ExtensionAbility> void register(Class<T> extensionType, T extension) {
        register(extensionType, extension, null);
    }
    
    /**
     * 获取扩展点列表
     */
    public <T extends ExtensionAbility> List<T> getExtensions(Class<T> extensionType) {
        return registry.getExtensions(extensionType, null);
    }
    
    /**
     * 获取扩展点列表（带上下文）
     */
    public <T extends ExtensionAbility> List<T> getExtensions(Class<T> extensionType, Map<String, Object> context) {
        return registry.getExtensions(extensionType, context);
    }
    
    /**
     * 获取扩展点元数据
     */
    public ExtensionMetadata getExtensionMetadata(Class<? extends ExtensionAbility> extensionType, String extensionId) {
        return factory.getExtensionMetadata(extensionType, extensionId);
    }
    
    /**
     * 清除缓存
     */
    public void invalidateCache(Class<? extends ExtensionAbility> extensionType) {
        factory.invalidateCache(extensionType);
        log.info("清除扩展点缓存: type={}", extensionType.getSimpleName());
    }
    
    /**
     * 获取缓存统计
     */
    public ExtensionCacheManager.CacheStatistics getCacheStatistics() {
        return factory.getCacheStatistics();
    }
    
    /**
     * 获取扩展点监控指标
     */
    public ExtensionMonitor.ExtensionMetrics getExtensionMetrics(String extensionId) {
        return factory.getExtensionMetrics(extensionId);
    }
    
    /**
     * 获取所有扩展点监控指标
     */
    public Map<String, ExtensionMonitor.ExtensionMetrics> getAllExtensionMetrics() {
        return factory.getAllExtensionMetrics();
    }
    
    /**
     * 获取底层组件（高级用法）
     */
    public ExtensionRegistry getRegistry() {
        return registry;
    }
    
    public ExtensionCacheManager getCacheManager() {
        return cacheManager;
    }
    
    public ExtensionMonitor getMonitor() {
        return monitor;
    }
    
    public ExtensionAbilityFactory getFactory() {
        return factory;
    }
    
    /**
     * 注册自定义解析器
     */
    public void registerResolver(ExtensionResolutionStrategy resolver) {
        if (resolverFactory instanceof com.flexpoint.core.resolution.DefaultExtensionResolverFactory) {
            resolverFactory.registerResolver(resolver);
        }
    }

    /**
     * 获取解析器工厂
     */
    public ExtensionResolverFactory getResolverFactory() {
        return resolverFactory;
    }
} 