package com.flexpoint.core;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.common.exception.SelectorNotFoundException;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.context.Context;
import com.flexpoint.core.context.ContextManager;
import com.flexpoint.core.context.ContextProvider;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.extension.ExtensionAbilityRegistry;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.selector.Selector;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 扩展点管理器
 * 负责扩展点注册、查找、监控，选择器通过名称管理
 * @author xiangganluo
 */
@Slf4j
public class FlexPoint {

    @Getter
    private final ExtensionAbilityRegistry extensionAbilityRegistry;
    @Getter
    private final ExtensionMonitor extensionMonitor;
    @Getter
    private final SelectorRegistry selectorRegistry;
    @Getter
    private final ContextManager contextManager;

    @Getter
    private final FlexPointConfig flexPointConfig;

    public FlexPoint(ExtensionAbilityRegistry extensionAbilityRegistry,
                     ExtensionMonitor extensionMonitor,
                     SelectorRegistry selectorRegistry,
                     ContextManager contextManager,
                     FlexPointConfig flexPointConfig
    ) {
        this.extensionAbilityRegistry = extensionAbilityRegistry;
        this.extensionMonitor = extensionMonitor;
        this.selectorRegistry = selectorRegistry;
        this.contextManager = contextManager;
        this.flexPointConfig = flexPointConfig;
    }

    /**
     * ==================extension==================
     */
    
    /**
     * 查找扩展点（使用指定的上下文）
     */
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType, Context context) {
        try {
            // 从扩展点接口的@FpSelector注解获取选择器名称
            FpSelector selectorAnno = extensionType.getAnnotation(FpSelector.class);
            if (selectorAnno == null) {
                log.warn("扩展点类型[{}]缺少@FpSelector注解", extensionType.getSimpleName());
                return null;
            }
            
            String selectorName = selectorAnno.value();
            Selector selector = selectorRegistry.getSelector(selectorName);
            if (selector == null) {
                log.warn("未找到名称为[{}]的选择器", selectorName);
                throw new SelectorNotFoundException(selectorName, extensionType.getSimpleName());
            }
            
            List<T> extensions = extensionAbilityRegistry.getAllExtensionAbility(extensionType);
            if (extensions.isEmpty()) {
                log.warn("未找到扩展点实现: type={}", extensionType.getSimpleName());
                return null;
            }
            
            T selected = selector.select(extensions, context);
            if (selected == null) {
                log.warn("选择器[{}]未找到匹配的扩展点: type={}", selectorName, extensionType.getSimpleName());
                return null;
            }
            
            log.debug("成功获取扩展点: type={}, code={}, selector={}, class={}",
                    extensionType.getSimpleName(), selected.getCode(), selectorName, selected.getClass().getName());
            return selected;
        } catch (Exception e) {
            log.error("获取扩展点失败: type={}", extensionType.getSimpleName(), e);
            throw e;
        }
    }

    /**
     * 查找扩展点（使用指定的上下文）- Optional版本
     */
    public <T extends ExtensionAbility> Optional<T> findAbilityOpt(Class<T> extensionType, Context context) {
        return Optional.ofNullable(findAbility(extensionType, context));
    }

    /**
     * 查找扩展点（使用默认上下文）
     */
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType) {
        Context context = new Context();
        return findAbility(extensionType, context);
    }

    /**
     * 查找扩展点（使用默认上下文）- Optional版本
     */
    public <T extends ExtensionAbility> Optional<T> findAbilityOpt(Class<T> extensionType) {
        return Optional.ofNullable(findAbility(extensionType));
    }

    public <T extends ExtensionAbility> List<T> getAllExtensions(Class<T> extensionType) {
        return extensionAbilityRegistry.getAllExtensionAbility(extensionType);
    }

    public int getExtensionCount() {
        return extensionAbilityRegistry.getAllExtensionAbility(ExtensionAbility.class).size();
    }

    public void register(ExtensionAbility extension) {
        extensionAbilityRegistry.register(extension);
        log.info("注册扩展点: code={}, tags={}, class={}", extension.getCode(), extension.getTags(), extension.getClass().getName());
    }

    /**
     * ==================selector==================
     */
    
    /**
     * 注册选择器
     */
    public void registerSelector(Selector selector) {
        selectorRegistry.registerSelector(selector);
        log.info("注册选择器[{}]", selector.getName());
    }

    /**
     * 移除指定名称的选择器
     */
    public void unregisterSelector(String selectorName) {
        selectorRegistry.unregisterSelector(selectorName);
        log.info("移除选择器[{}]", selectorName);
    }

    /**
     * 检查指定名称的选择器是否已注册
     */
    public boolean hasSelector(String selectorName) {
        return selectorRegistry.hasSelector(selectorName);
    }

    /**
     * ==================context==================
     */
    
    /**
     * 注册上下文提供者
     */
    public void registerContextProvider(ContextProvider provider) {
        contextManager.registerProvider(provider);
        log.info("注册上下文提供者: name={}, priority={}", provider.getName(), provider.getPriority());
    }

    /**
     * 注销上下文提供者
     */
    public void unregisterContextProvider(String providerName) {
        contextManager.unregisterProvider(providerName);
        log.info("注销上下文提供者: name={}", providerName);
    }

    /**
     * ==================monitor==================
     */
    public ExtensionMonitor.ExtensionMetrics getExtensionMetrics(ExtensionAbility extensionAbility) {
        return extensionMonitor.getMetrics(extensionAbility);
    }

    public Map<String, ExtensionMonitor.ExtensionMetrics> getAllExtensionMetrics() {
        return extensionMonitor.getAllMetrics();
    }

    public void recordInvocation(ExtensionAbility extensionAbility, long duration, boolean success) {
        this.extensionMonitor.recordInvocation(extensionAbility, duration, success);
    }

    public void recordException(ExtensionAbility extensionAbility, Throwable exception) {
        this.extensionMonitor.recordException(extensionAbility, exception);
    }

    public void resetMetrics(ExtensionAbility extensionAbility) {
        this.extensionMonitor.resetMetrics(extensionAbility);
    }

}