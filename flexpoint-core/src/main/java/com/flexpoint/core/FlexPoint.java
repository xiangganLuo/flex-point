package com.flexpoint.core;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.common.exception.SelectorNotFoundException;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.monitor.ExtMonitor;
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
    private final ExtAbilityRegistry extAbilityRegistry;
    @Getter
    private final ExtMonitor extMonitor;
    @Getter
    private final SelectorRegistry selectorRegistry;

    @Getter
    private final FlexPointConfig flexPointConfig;

    public FlexPoint(ExtAbilityRegistry extAbilityRegistry,
                     ExtMonitor extMonitor,
                     SelectorRegistry selectorRegistry,
                     FlexPointConfig flexPointConfig
    ) {
        this.extAbilityRegistry = extAbilityRegistry;
        this.extMonitor = extMonitor;
        this.selectorRegistry = selectorRegistry;
        this.flexPointConfig = flexPointConfig;
    }

    /**
     * ==================ext==================
     */
    
    /**
     * 查找扩展点
     */
    public <T extends ExtAbility> T findAbility(Class<T> extType) {
        try {
            // 从扩展点接口的@FpSelector注解获取选择器名称
            FpSelector selectorAnno = extType.getAnnotation(FpSelector.class);
            if (selectorAnno == null) {
                log.warn("扩展点类型[{}]缺少@FpSelector注解", extType.getSimpleName());
                return null;
            }
            
            String selectorName = selectorAnno.value();
            Selector selector = selectorRegistry.getSelector(selectorName);
            if (selector == null) {
                log.warn("未找到名称为[{}]的选择器", selectorName);
                throw new SelectorNotFoundException(selectorName, extType.getSimpleName());
            }
            
            List<T> exts = extAbilityRegistry.getAllExtAbility(extType);
            if (exts.isEmpty()) {
                log.warn("未找到扩展点实现: type={}", extType.getSimpleName());
                return null;
            }

            T selected = selector.select(exts);
            if (selected == null) {
                log.warn("选择器[{}]未找到匹配的扩展点: type={}", selectorName, extType.getSimpleName());
                return null;
            }
            
            log.debug("成功获取扩展点: type={}, code={}, selector={}, class={}",
                    extType.getSimpleName(), selected.getCode(), selectorName, selected.getClass().getName());
            return selected;
        } catch (Exception e) {
            log.error("获取扩展点失败: type={}", extType.getSimpleName(), e);
            throw e;
        }
    }

    /**
     * 查找扩展点- Optional版本
     */
    public <T extends ExtAbility> Optional<T> findAbilityOpt(Class<T> extType) {
        return Optional.ofNullable(findAbility(extType));
    }

    public <T extends ExtAbility> List<T> getAllExt(Class<T> extType) {
        return extAbilityRegistry.getAllExtAbility(extType);
    }

    public int getExtCount() {
        return extAbilityRegistry.getAllExtAbility(ExtAbility.class).size();
    }

    public void register(ExtAbility ext) {
        extAbilityRegistry.register(ext);
        log.info("注册扩展点: code={}, tags={}, class={}", ext.getCode(), ext.getTags(), ext.getClass().getName());
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
     * ==================monitor==================
     */
    public ExtMonitor.ExtMetrics getExtMetrics(ExtAbility extAbility) {
        return extMonitor.getMetrics(extAbility);
    }

    public Map<String, ExtMonitor.ExtMetrics> getAllExtMetrics() {
        return extMonitor.getAllMetrics();
    }

    public void recordInvocation(ExtAbility extAbility, long duration, boolean success) {
        this.extMonitor.recordInvocation(extAbility, duration, success);
    }

    public void recordException(ExtAbility extAbility, Throwable exception) {
        this.extMonitor.recordException(extAbility, exception);
    }

    public void resetMetrics(ExtAbility extAbility) {
        this.extMonitor.resetMetrics(extAbility);
    }

}