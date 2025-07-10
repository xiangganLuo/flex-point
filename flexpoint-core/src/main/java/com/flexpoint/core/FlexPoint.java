package com.flexpoint.core;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.context.ContextProvider;
import com.flexpoint.core.context.ContextManager;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.extension.ExtensionAbilityRegistry;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.context.Context;
import com.flexpoint.core.selector.Selector;
import com.flexpoint.core.selector.SelectorChain;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.flexpoint.common.constants.FlexPointConstants.DEFAULT_SELECTOR_CHAIN_NAME;

/**
 * 扩展点管理器
 * 只负责扩展点注册、查找、监控，选择器链由 SelectorRegistry 管理
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
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType, String selectorChainName, Context context) {
        String extensionId = null;
        try {
            SelectorChain chain = selectorRegistry.getSelectorChain(selectorChainName);
            if (chain == null) {
                throw new IllegalArgumentException("未找到指定名称的 SelectorChain: " + selectorChainName);
            }
            List<T> extensions = extensionAbilityRegistry.getAllExtensionAbility(extensionType);
            if (extensions.isEmpty()) {
                log.warn("未找到扩展点实现: type={}", extensionType.getSimpleName());
                return null;
            }
            T selected = chain.select(extensions, context);
            if (selected == null) {
                log.warn("SelectorChain 未找到匹配的扩展点: type={}, chain={}", extensionType.getSimpleName(), selectorChainName);
                return null;
            }
            extensionId = selected.getCode() + ":" + selected.version();
            log.debug("成功获取扩展点: type={}, id={}, class={}", extensionType.getSimpleName(), extensionId, selected.getClass().getName());
            return selected;
        } catch (Exception e) {
            log.error("获取扩展点失败: type={}, id={}", extensionType.getSimpleName(), extensionId, e);
            throw e;
        }
    }

    public <T extends ExtensionAbility> Optional<T> findAbilityOpt(Class<T> extensionType, String selectorChainName, Context context) {
        return Optional.ofNullable(findAbility(extensionType, selectorChainName, context));
    }

    /**
     * 查找扩展点（使用默认选择器链）
     * 从扩展点接口的 @FpSelector 注解中获取选择器链名称
     */
    public <T extends ExtensionAbility> T findAbility(Class<T> extensionType) {
        FpSelector selectorAnno = extensionType.getAnnotation(FpSelector.class);
        if (selectorAnno == null || selectorAnno.value().isEmpty()) {
            throw new IllegalArgumentException("扩展点接口 " + extensionType.getName() + " 缺少 @FpSelector 注解或未指定选择器链名称");
        }
        return findAbility(extensionType, selectorAnno.value(), new Context());
    }

    /**
     * 查找扩展点（使用默认选择器链）- Optional版本
     */
    public <T extends ExtensionAbility> Optional<T> findAbilityOpt(Class<T> extensionType) {
        return Optional.ofNullable(findAbility(extensionType));
    }

    public <T extends ExtensionAbility> T findAbilityById(String extensionId) {
        return extensionAbilityRegistry.getExtensionById(extensionId);
    }

    public <T extends ExtensionAbility> List<T> getAllExtensions(Class<T> extensionType) {
        return extensionAbilityRegistry.getAllExtensionAbility(extensionType);
    }

    public int getExtensionCount() {
        return extensionAbilityRegistry.getAllExtensionAbility(ExtensionAbility.class).size();
    }

    public boolean exists(String extensionId) {
        return extensionAbilityRegistry.exists(extensionId);
    }

    public void register(ExtensionAbility extension) {
        extensionAbilityRegistry.register(extension);
        log.info("注册扩展点: code={}, version={}, class={}", extension.getCode(), extension.version(), extension.getClass().getName());
    }

    public void unregister(String extensionId) {
        extensionAbilityRegistry.unregister(extensionId);
        log.info("注销扩展点: id={}", extensionId);
    }

    /**
     * ==================selector==================
     */
    public void registerSelector(Selector selector) {
        this.registerSelector(DEFAULT_SELECTOR_CHAIN_NAME, selector);
    }

    public void registerSelector(String chainName, Selector selector) {
        selectorRegistry.registerSelector(chainName, selector);
        log.info("注册选择器: name={}", selector.getName());
    }

    /**
     * 注销选择器
     */
    public void unregisterSelector(String chainName, String selectorName) {
        selectorRegistry.unregisterSelector(chainName, selectorName);
        log.info("注销选择器: name={}", selectorName);
    }

    /**
     * 注册选择器链
     */
    public void registerSelectorChain(com.flexpoint.core.selector.SelectorChain chain) {
        selectorRegistry.registerSelectorChain(chain);
        log.info("注册选择器链: name={}", chain.getName());
    }

    /**
     * 注销选择器链
     */
    public void unregisterSelectorChain(String chainName) {
        selectorRegistry.unregisterSelectorChain(chainName);
        log.info("注销选择器链: name={}", chainName);
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
    public ExtensionMonitor.ExtensionMetrics getExtensionMetrics(String extensionId) {
        return extensionMonitor.getMetrics(extensionId);
    }

    public Map<String, ExtensionMonitor.ExtensionMetrics> getAllExtensionMetrics() {
        return extensionMonitor.getAllMetrics();
    }

    public void recordInvocation(String extensionId, long duration, boolean success) {
        this.extensionMonitor.recordInvocation(extensionId, duration, success);
    }

    public void recordException(String extensionId, Throwable exception) {
        this.extensionMonitor.recordException(extensionId, exception);
    }

    public void resetMetrics(String extensionId) {
        this.extensionMonitor.resetMetrics(extensionId);
    }
}