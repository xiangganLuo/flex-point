package com.flexpoint.core;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.extension.ExtensionAbilityFactory;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.registry.ExtensionRegistry;
import com.flexpoint.core.registry.metadata.ExtensionMetadata;
import com.flexpoint.core.resolution.ExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ExtensionResolverFactory;
import lombok.Getter;
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
    
    @Getter
    private final ExtensionAbilityFactory abilityFactory;
    @Getter
    private final ExtensionRegistry registry;
    @Getter
    private final ExtensionMonitor monitor;
    @Getter
    private final ExtensionResolverFactory resolverFactory;
    @Getter
    private final FlexPointConfig config;
    
    FlexPoint(ExtensionAbilityFactory factory, ExtensionRegistry registry, 
              ExtensionMonitor monitor,
              ExtensionResolverFactory resolverFactory, FlexPointConfig config) {
        this.abilityFactory = factory;
        this.registry = registry;
        this.monitor = monitor;
        this.resolverFactory = resolverFactory;
        this.config = config;
    }

    /**
     * 查找扩展点
     */
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType) {
        return abilityFactory.findAbility(extensionType);
    }
    
    /**
     * 查找扩展点（带上下文）
     */
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType, Map<String, Object> context) {
        return abilityFactory.findAbility(extensionType, context);
    }
    
    /**
     * 查找扩展点（返回Optional）
     */
    public <T extends ExtensionAbility> Optional<T> findAbilityOpt(Class<T> extensionType) {
        return abilityFactory.findAbilityOpt(extensionType);
    }
    
    /**
     * 根据ID查找扩展点
     */
    public <T extends ExtensionAbility> T findAbilityById(Class<T> extensionType, String extensionId) {
        return abilityFactory.findAbilityById(extensionType, extensionId);
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
        return registry.getExtensions(extensionType);
    }
    
    /**
     * 获取扩展点元数据
     */
    public ExtensionMetadata getExtensionMetadata(Class<? extends ExtensionAbility> extensionType, String extensionId) {
        return abilityFactory.getExtensionMetadata(extensionType, extensionId);
    }
    
    /**
     * 获取扩展点监控指标
     */
    public ExtensionMonitor.ExtensionMetrics getExtensionMetrics(String extensionId) {
        return abilityFactory.getExtensionMetrics(extensionId);
    }
    
    /**
     * 获取所有扩展点监控指标
     */
    public Map<String, ExtensionMonitor.ExtensionMetrics> getAllExtensionMetrics() {
        return abilityFactory.getAllExtensionMetrics();
    }

    /**
     * 注册自定义解析器
     */
    public void registerResolver(ExtensionResolutionStrategy resolver) {
        if (resolverFactory instanceof com.flexpoint.core.resolution.DefaultExtensionResolverFactory) {
            resolverFactory.registerResolver(resolver);
        }
    }

}