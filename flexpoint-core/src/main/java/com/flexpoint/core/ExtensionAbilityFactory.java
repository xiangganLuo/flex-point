package com.flexpoint.core;

import com.flexpoint.common.ExtensionAbility;
import com.flexpoint.common.annotations.ExtensionResolver;
import com.flexpoint.common.utils.ExtensionUtil;
import com.flexpoint.core.cache.ExtensionCacheManager;
import com.flexpoint.core.metadata.ExtensionMetadata;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.registry.ExtensionRegistry;
import com.flexpoint.core.resolution.DefaultExtensionResolverFactory;
import com.flexpoint.core.resolution.ExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ExtensionResolverFactory;
import com.flexpoint.common.exception.ExtensionResolverNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 增强的扩展点工厂
 * 提供扩展点的查找、缓存、监控等功能
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class ExtensionAbilityFactory {

    private final ExtensionRegistry extensionRegistry;
    private final ExtensionCacheManager extensionCacheManager;
    private final ExtensionMonitor extensionMonitor;
    private final ExtensionResolverFactory resolverFactory;
    
    public ExtensionAbilityFactory(ExtensionRegistry extensionRegistry, ExtensionCacheManager extensionCacheManager, ExtensionMonitor extensionMonitor) {
        this(extensionRegistry, extensionCacheManager, extensionMonitor, new DefaultExtensionResolverFactory());
    }

    public ExtensionAbilityFactory(ExtensionRegistry extensionRegistry, ExtensionCacheManager extensionCacheManager, ExtensionMonitor extensionMonitor, ExtensionResolverFactory resolverFactory) {
        this.extensionRegistry = extensionRegistry;
        this.extensionCacheManager = extensionCacheManager;
        this.extensionMonitor = extensionMonitor;
        this.resolverFactory = resolverFactory == null ? new DefaultExtensionResolverFactory() : resolverFactory;
    }
    
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType, Map<String, Object> context) {
        String extensionId = ExtensionUtil.getExtensionId(extensionType, context);
        try {
            // 1. 尝试从缓存获取
            T cachedExtension = extensionCacheManager.getCachedExtension(extensionType, extensionId);
            if (cachedExtension != null) {
                log.debug("从缓存获取扩展点: type={}, id={}", extensionType.getSimpleName(), extensionId);
                return cachedExtension;
            }
            
            // 2. 从注册中心获取
            List<T> extensions = extensionRegistry.getExtensions(extensionType, context);
            if (extensions.isEmpty()) {
                log.warn("未找到扩展点实现: type={}", extensionType.getSimpleName());
                return null;
            }
            
            // 3. 使用解析策略选择扩展点
            ExtensionResolutionStrategy resolver = Optional.ofNullable(extensionType.getAnnotation(ExtensionResolver.class))
                    .map(annotation -> resolverFactory.getResolver(annotation.value()))
                    .orElse(resolverFactory.getResolver(null));
            if (resolver == null) {
                throw new ExtensionResolverNotFoundException("未找到ExtensionResolutionStrategy解析器!");
            }
            T selectedExtension = resolver.resolve(extensions, context);
            if (selectedExtension == null) {
                log.warn("解析策略未找到匹配的扩展点: type={}, context={}", extensionType.getSimpleName(), context);
                return null;
            }
            
            // 4. 缓存扩展点
            extensionCacheManager.cacheExtension(extensionType, extensionId, selectedExtension);
            log.debug("成功获取扩展点: type={}, id={}, class={}",
                    extensionType.getSimpleName(), extensionId, selectedExtension.getClass().getName());
            return selectedExtension;
        } catch (Exception e) {
            log.error("获取扩展点失败: type={}, id={}", extensionType.getSimpleName(), extensionId, e);
            throw e;
        }
    }
    
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType) {
        return findAbility(extensionType, null);
    }
    
    public <T extends ExtensionAbility> Optional<T> findAbilityOpt(Class<T> extensionType) {
        return Optional.ofNullable(findAbility(extensionType));
    }
    
    public <T extends ExtensionAbility> T findAbilityById(Class<T> extensionType, String extensionId) {
        return extensionRegistry.getExtensionById(extensionType, extensionId);
    }
    
    public ExtensionMetadata getExtensionMetadata(Class<? extends ExtensionAbility> extensionType, String extensionId) {
        return extensionRegistry.getExtensionMetadata(extensionType, extensionId);
    }
    
    public void invalidateCache(Class<? extends ExtensionAbility> extensionType) {
        extensionCacheManager.invalidateCache(extensionType);
    }
    
    public ExtensionCacheManager.CacheStatistics getCacheStatistics() {
        return extensionCacheManager.getCacheStatistics();
    }
    
    public ExtensionMonitor.ExtensionMetrics getExtensionMetrics(String extensionId) {
        return extensionMonitor.getMetrics(extensionId);
    }
    
    public Map<String, ExtensionMonitor.ExtensionMetrics> getAllExtensionMetrics() {
        return extensionMonitor.getAllMetrics();
    }
} 