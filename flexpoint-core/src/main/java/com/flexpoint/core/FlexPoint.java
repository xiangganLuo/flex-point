package com.flexpoint.core;

import com.flexpoint.common.annotations.Selector;
import com.flexpoint.common.exception.ExtensionSelectorNotFoundException;
import com.flexpoint.common.utils.ExtensionUtil;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.extension.ExtensionAbilityRegistry;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.selector.DefaultSelectorRegistry;
import com.flexpoint.core.selector.ExtensionSelector;
import com.flexpoint.core.selector.SelectorRegistry;
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
    private final SelectorRegistry selectorRegistry;

    public FlexPoint(ExtensionAbilityRegistry extensionAbilityRegistry, ExtensionMonitor extensionMonitor) {
        this(extensionAbilityRegistry, extensionMonitor, new DefaultSelectorRegistry());
    }

    public FlexPoint(ExtensionAbilityRegistry extensionAbilityRegistry, ExtensionMonitor extensionMonitor, SelectorRegistry selectorRegistry) {
        this.extensionAbilityRegistry = extensionAbilityRegistry;
        this.extensionMonitor = extensionMonitor;
        this.selectorRegistry = selectorRegistry == null ? new DefaultSelectorRegistry() : selectorRegistry;
    }

    // ==================== 扩展点查找方法 ====================

    /**
     * 查找扩展点
     */
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType) {
        String extensionId = null;
        try {
            // 1. 获取选择器
            ExtensionSelector selector = getSelector(extensionType);

            // 2. 从注册中心获取所有扩展点
            List<T> extensions = extensionAbilityRegistry.getAllExtensionAbility(extensionType);
            if (extensions.isEmpty()) {
                log.warn("未找到扩展点实现: type={}", extensionType.getSimpleName());
                return null;
            }

            // 3. 使用选择器选择扩展点
            T selectedExtension = selector.resolve(extensions);
            if (selectedExtension == null) {
                log.warn("选择器未找到匹配的扩展点: type={}", extensionType.getSimpleName());
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

    // ==================== 选择器管理 ====================

    /**
     * 注册自定义选择器
     */
    public void registerSelector(ExtensionSelector selector) {
        if (selectorRegistry instanceof DefaultSelectorRegistry) {
            selectorRegistry.registerSelector(selector);
            log.info("注册选择器: {}", selector.getName());
        } else {
            log.warn("当前选择器注册表不支持动态注册: {}", selectorRegistry.getClass().getSimpleName());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 获取选择器
     */
    private ExtensionSelector getSelector(Class<?> extensionType) {
        Selector selector = extensionType.getAnnotation(Selector.class);
        if (selector == null) {
            throw new ExtensionSelectorNotFoundException("扩展点类型 " + extensionType.getSimpleName() + " 未指定@Selector，无法确定选择器！");
        }
        ExtensionSelector extensionSelector = selectorRegistry.getSelector(selector.value());
        if (extensionSelector == null) {
            throw new ExtensionSelectorNotFoundException("未找到指定名称的ExtensionSelector选择器: " + selector.value());
        }
        return extensionSelector;
    }
    
}