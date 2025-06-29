package com.flexpoint.core;

import com.flexpoint.common.annotations.ExtensionResolver;
import com.flexpoint.common.constants.FlexPointConstants;
import com.flexpoint.common.exception.ExtensionResolverNotFoundException;
import com.flexpoint.common.utils.ExtensionUtil;
import com.flexpoint.core.registry.ExtensionAbility;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.registry.ExtensionAbilityRegistry;
import com.flexpoint.core.registry.metadata.ExtensionMetadata;
import com.flexpoint.core.resolution.DefaultExtensionResolverFactory;
import com.flexpoint.core.resolution.ExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ExtensionResolverFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 扩展点管理器
 * 提供扩展点的注册、查找、监控等统一管理功能
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class FlexPointManager {

    @Getter
    private final ExtensionAbilityRegistry extensionAbilityRegistry;
    @Getter
    private final ExtensionMonitor extensionMonitor;
    @Getter
    private final ExtensionResolverFactory resolverFactory;
    
    public FlexPointManager(ExtensionAbilityRegistry extensionAbilityRegistry, ExtensionMonitor extensionMonitor) {
        this(extensionAbilityRegistry, extensionMonitor, new DefaultExtensionResolverFactory());
    }

    public FlexPointManager(ExtensionAbilityRegistry extensionAbilityRegistry, ExtensionMonitor extensionMonitor, ExtensionResolverFactory resolverFactory) {
        this.extensionAbilityRegistry = extensionAbilityRegistry;
        this.extensionMonitor = extensionMonitor;
        this.resolverFactory = resolverFactory == null ? new DefaultExtensionResolverFactory() : resolverFactory;
    }
    
    /**
     * 查找扩展点（带上下文）
     */
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType, Map<String, Object> context) {
        String extensionId = null;
        try {
            // 1. 获取解析策略
            ExtensionResolutionStrategy resolver = getResolver(extensionType, context);

            // 2. 从注册中心获取所有扩展点
            List<T> extensions = extensionAbilityRegistry.getExtensions(extensionType);
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

    /**
     * 查找扩展点
     */
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType) {
        return findAbility(extensionType, null);
    }
    
    /**
     * 查找扩展点（返回Optional）
     */
    public <T extends ExtensionAbility> Optional<T> findAbilityOpt(Class<T> extensionType) {
        return Optional.ofNullable(findAbility(extensionType));
    }
    
    /**
     * 根据ID查找扩展点
     */
    public <T extends ExtensionAbility> T findAbilityById(Class<T> extensionType, String extensionId) {
        return extensionAbilityRegistry.getExtensionById(extensionType, extensionId);
    }
    
    /**
     * 注册扩展点
     */
    public <T extends ExtensionAbility> void register(Class<T> extensionType, T extension, ExtensionMetadata metadata) {
        extensionAbilityRegistry.register(extensionType, extension, metadata);
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
        return extensionAbilityRegistry.getExtensions(extensionType);
    }
    
    /**
     * 获取扩展点元数据
     */
    public ExtensionMetadata getExtensionMetadata(Class<? extends ExtensionAbility> extensionType, String extensionId) {
        return extensionAbilityRegistry.getExtensionMetadata(extensionType, extensionId);
    }
    
    /**
     * 获取扩展点监控指标
     */
    public ExtensionMonitor.ExtensionMetrics getExtensionMetrics(String extensionId) {
        return extensionMonitor.getMetrics(extensionId);
    }
    
    /**
     * 获取所有扩展点监控指标
     */
    public Map<String, ExtensionMonitor.ExtensionMetrics> getAllExtensionMetrics() {
        return extensionMonitor.getAllMetrics();
    }

    /**
     * 注册自定义解析器
     */
    public void registerResolver(ExtensionResolutionStrategy resolver) {
        if (resolverFactory instanceof DefaultExtensionResolverFactory) {
            resolverFactory.registerResolver(resolver);
        }
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