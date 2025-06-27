package com.flexpoint.core.extension;

import com.flexpoint.common.annotations.ExtensionResolver;
import com.flexpoint.common.constants.FlexPointConstants;
import com.flexpoint.common.exception.ExtensionResolverNotFoundException;
import com.flexpoint.common.utils.ExtensionUtil;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.registry.ExtensionRegistry;
import com.flexpoint.core.registry.metadata.ExtensionMetadata;
import com.flexpoint.core.resolution.DefaultExtensionResolverFactory;
import com.flexpoint.core.resolution.ExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ExtensionResolverFactory;
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
    private final ExtensionMonitor extensionMonitor;
    private final ExtensionResolverFactory resolverFactory;
    
    public ExtensionAbilityFactory(ExtensionRegistry extensionRegistry, ExtensionMonitor extensionMonitor) {
        this(extensionRegistry, extensionMonitor, new DefaultExtensionResolverFactory());
    }

    public ExtensionAbilityFactory(ExtensionRegistry extensionRegistry, ExtensionMonitor extensionMonitor, ExtensionResolverFactory resolverFactory) {
        this.extensionRegistry = extensionRegistry;
        this.extensionMonitor = extensionMonitor;
        this.resolverFactory = resolverFactory == null ? new DefaultExtensionResolverFactory() : resolverFactory;
    }
    
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType, Map<String, Object> context) {
        String extensionId = null;
        try {
            // 1. 获取解析策略
            ExtensionResolutionStrategy resolver = getResolver(extensionType, context);

            // 2. 从注册中心获取所有扩展点
            List<T> extensions = extensionRegistry.getExtensions(extensionType);
            if (extensions.isEmpty()) {
                log.warn("未找到扩展点实现: type={}", extensionType.getSimpleName());
                return null;
            }

            // 3. 使用解析策略选择扩展点
            T selectedExtension = resolver.resolve(extensions, context);
            if (selectedExtension == null) {
                log.warn("解析策略未找到匹配的扩展点: type={}, context={}", extensionType.getSimpleName(), context);
                return null;
            }

            // 4. 生成包含code的extensionId用于缓存
            String code = selectedExtension.getCode();
            extensionId = ExtensionUtil.getExtensionId(extensionType, code);

            log.debug("成功获取扩展点: type={}, code={}, id={}, class={}",
                    extensionType.getSimpleName(), code, extensionId, selectedExtension.getClass().getName());
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
    
    public ExtensionMonitor.ExtensionMetrics getExtensionMetrics(String extensionId) {
        return extensionMonitor.getMetrics(extensionId);
    }
    
    public Map<String, ExtensionMonitor.ExtensionMetrics> getAllExtensionMetrics() {
        return extensionMonitor.getAllMetrics();
    }

    /**
     * 获取解析策略
     */
    private ExtensionResolutionStrategy getResolver(Class<?> extensionType, Map<String, Object> context) {
        ExtensionResolutionStrategy resolver = Optional.ofNullable(extensionType.getAnnotation(ExtensionResolver.class))
                .map(annotation -> resolverFactory.getResolver(annotation.value()))
                .orElse(resolverFactory.getResolver(
                            Optional.ofNullable(context)
                            .map(ctx -> ctx.getOrDefault(FlexPointConstants.CODE, "").toString())
                            .orElse(null)
                        )
                );

        if (resolver == null) {
            throw new ExtensionResolverNotFoundException("未找到ExtensionResolutionStrategy解析器!");
        }

        return resolver;
    }

} 