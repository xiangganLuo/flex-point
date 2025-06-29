package com.flexpoint.core;

import com.flexpoint.common.annotations.ExtensionResolverSelector;
import com.flexpoint.common.exception.ExtensionResolverNotFoundException;
import com.flexpoint.common.utils.ExtensionUtil;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.extension.ExtensionAbilityRegistry;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.resolution.DefaultExtensionResolutionStrategyRegistry;
import com.flexpoint.core.resolution.ExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ExtensionResolutionStrategyRegistry;
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
public class FlexPoint {

    @Getter
    private final ExtensionAbilityRegistry extensionAbilityRegistry;
    @Getter
    private final ExtensionMonitor extensionMonitor;
    @Getter
    private final ExtensionResolutionStrategyRegistry strategyRegistry;
    
    public FlexPoint(ExtensionAbilityRegistry extensionAbilityRegistry, ExtensionMonitor extensionMonitor) {
        this(extensionAbilityRegistry, extensionMonitor, new DefaultExtensionResolutionStrategyRegistry());
    }

    public FlexPoint(ExtensionAbilityRegistry extensionAbilityRegistry, ExtensionMonitor extensionMonitor, ExtensionResolutionStrategyRegistry strategyRegistry) {
        this.extensionAbilityRegistry = extensionAbilityRegistry;
        this.extensionMonitor = extensionMonitor;
        this.strategyRegistry = strategyRegistry == null ? new DefaultExtensionResolutionStrategyRegistry() : strategyRegistry;
    }
    
    // ==================== 扩展点查找方法 ====================
    
    /**
     * 查找扩展点
     * 使用默认解析策略选择扩展点
     */
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType) {
        String extensionId = null;
        try {
            // 1. 获取解析策略
            ExtensionResolutionStrategy resolver = getResolver(extensionType);

            // 2. 从注册中心获取所有扩展点
            List<T> extensions = extensionAbilityRegistry.getAllExtensionAbility(extensionType);
            if (extensions.isEmpty()) {
                log.warn("未找到扩展点实现: type={}", extensionType.getSimpleName());
                return null;
            }
            
            // 3. 使用解析策略选择扩展点
            T selectedExtension = resolver.resolve(extensions);
            if (selectedExtension == null) {
                log.warn("解析策略未找到匹配的扩展点: type={}", extensionType.getSimpleName());
                return null;
            }

            // 4. 生成正确的extensionId用于监控
            String code = selectedExtension.getCode();
            String version = selectedExtension.version();
            extensionId = ExtensionUtil.getExtensionId(code, version);

            log.debug("成功获取扩展点: type={}, code={}, version={}, id={}, class={}",
                    extensionType.getSimpleName(), code, version, extensionId, selectedExtension.getClass().getName());
            return selectedExtension;
        } catch (Exception e) {
            log.error("获取扩展点失败: type={}, id={}", extensionType.getSimpleName(), extensionId, e);
            throw e;
        }
    }
    
    /**
     * 查找扩展点（返回Optional）
     */
    public <T extends ExtensionAbility> Optional<T> findAbilityOpt(Class<T> extensionType) {
        return Optional.ofNullable(findAbility(extensionType));
    }

    /**
     * 根据扩展点ID查找扩展点
     */
    public <T extends ExtensionAbility> T findAbilityById(String extensionId) {
        return extensionAbilityRegistry.getExtensionById(extensionId);
    }

    /**
     * 根据业务代码查找扩展点
     */
    public <T extends ExtensionAbility> T findAbilityByCode(String code) {
        return extensionAbilityRegistry.getExtensionById(ExtensionUtil.getExtensionId(code, null));
    }

    /**
     * 根据业务代码和版本查找扩展点
     */
    public <T extends ExtensionAbility> T findAbilityByCodeAndVersion(String code, String version) {
        return extensionAbilityRegistry.getExtensionById(ExtensionUtil.getExtensionId(code, version));
    }
    
    // ==================== 扩展点注册方法 ====================
    
    /**
     * 注册扩展点
     */
    public void register(ExtensionAbility extension) {
        extensionAbilityRegistry.register(extension);
        log.info("注册扩展点: code={}, version={}, class={}", 
                extension.getCode(), extension.version(), extension.getClass().getName());
    }
    
    /**
     * 注销扩展点
     */
    public void unregister(String extensionId) {
        extensionAbilityRegistry.unregister(extensionId);
        log.info("注销扩展点: id={}", extensionId);
    }
    
    // ==================== 扩展点查询方法 ====================
    
    /**
     * 获取指定类型的所有扩展点
     */
    public <T extends ExtensionAbility> List<T> getAllExtensions(Class<T> extensionType) {
        return extensionAbilityRegistry.getAllExtensionAbility(extensionType);
    }
    
    /**
     * 获取注册的扩展点总数
     */
    public int getExtensionCount() {
        return extensionAbilityRegistry.getAllExtensionAbility(ExtensionAbility.class).size();
    }
    
    /**
     * 检查扩展点是否存在
     */
    public boolean exists(String extensionId) {
        return extensionAbilityRegistry.exists(extensionId);
    }
    
    // ==================== 监控方法 ====================
    
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
     * 记录扩展点调用
     */
    public void recordInvocation(String extensionId, long duration, boolean success) {
        this.extensionMonitor.recordInvocation(extensionId, duration, success);
    }
    
    /**
     * 记录扩展点异常
     */
    public void recordException(String extensionId, Throwable exception) {
        this.extensionMonitor.recordException(extensionId, exception);
    }
    
    /**
     * 重置扩展点指标
     */
    public void resetMetrics(String extensionId) {
        this.extensionMonitor.resetMetrics(extensionId);
    }
    
    // ==================== 解析策略管理 ====================
    
    /**
     * 注册自定义解析策略
     */
    public void registerResolver(ExtensionResolutionStrategy resolver) {
        if (strategyRegistry instanceof DefaultExtensionResolutionStrategyRegistry) {
            strategyRegistry.registerStrategy(resolver);
            log.info("注册解析策略: {}", resolver.getStrategyName());
        } else {
            log.warn("当前解析策略注册表不支持动态注册: {}", strategyRegistry.getClass().getSimpleName());
        }
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 获取解析策略
     */
    private ExtensionResolutionStrategy getResolver(Class<?> extensionType) {
        ExtensionResolutionStrategy resolver = Optional.ofNullable(extensionType.getAnnotation(ExtensionResolverSelector.class))
                .map(annotation -> strategyRegistry.getStrategy(annotation.value()))
                .orElse(strategyRegistry.getStrategy(null));

        if (resolver == null) {
            throw new ExtensionResolverNotFoundException("未找到ExtensionResolutionStrategy解析器!");
        }

        return resolver;
    }
    
}