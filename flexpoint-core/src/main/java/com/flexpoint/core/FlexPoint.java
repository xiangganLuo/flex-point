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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * 根据扩展点类型和code查找匹配的扩展点列表
     *
     * @param extType 扩展点类型
     * @param code 业务标识
     * @param <T> 扩展点类型
     * @return 匹配的扩展点列表
     */
    public <T extends ExtAbility> List<T> findAbilitiesByCode(Class<T> extType, String code) {
        List<T> exts = extAbilityRegistry.getAllExtAbility(extType);
        if (exts.isEmpty()) {
            return Collections.emptyList();
        }

        return exts.stream()
                .filter(ext -> code.equals(ext.getCode()))
                .collect(Collectors.toList());
    }
    
    /**
     * 根据扩展点类型和code查找匹配的第一个扩展点
     *
     * @param extType 扩展点类型
     * @param code 业务标识
     * @param <T> 扩展点类型
     * @return 匹配的扩展点，如果没有找到则返回null
     */
    public <T extends ExtAbility> T findAbilityByCode(Class<T> extType, String code) {
        List<T> abilities = findAbilitiesByCode(extType, code);
        return abilities.isEmpty() ? null : abilities.get(0);
    }

    /**
     * 根据扩展点类型、code和多个标签查找匹配的扩展点列表
     * 所有标签都必须匹配才会返回
     * 标签以键值对的形式传入，必须成对出现，如：tagKey1, tagValue1, tagKey2, tagValue2
     *
     * @param extType 扩展点类型
     * @param code 业务标识
     * @param tagsKeyValue 标签键值对，必须成对出现
     * @param <T> 扩展点类型
     * @return 匹配的扩展点列表
     * @throws IllegalArgumentException 如果标签键值对不成对出现
     */
    public <T extends ExtAbility> List<T> findAbilitiesByCodeAndTags(Class<T> extType, String code, Object... tagsKeyValue) {
        if (tagsKeyValue == null || tagsKeyValue.length == 0) {
            return findAbilitiesByCode(extType, code);
        }
        
        if (tagsKeyValue.length % 2 != 0) {
            throw new IllegalArgumentException("标签键值对必须成对出现");
        }
        
        Map<String, Object> tags = new HashMap<>();
        for (int i = 0; i < tagsKeyValue.length; i += 2) {
            if (!(tagsKeyValue[i] instanceof String)) {
                throw new IllegalArgumentException("标签键必须是字符串类型");
            }
            tags.put((String) tagsKeyValue[i], tagsKeyValue[i + 1]);
        }
        
        return findAbilitiesByCodeAndTags(extType, code, tags);
    }
    
    /**
     * 根据扩展点类型、code和多个标签查找匹配的第一个扩展点
     * 所有标签都必须匹配才会返回
     * 标签以键值对的形式传入，必须成对出现，如：tagKey1, tagValue1, tagKey2, tagValue2
     *
     * @param extType 扩展点类型
     * @param code 业务标识
     * @param tagsKeyValue 标签键值对，必须成对出现
     * @param <T> 扩展点类型
     * @return 匹配的扩展点，如果没有找到则返回null
     * @throws IllegalArgumentException 如果标签键值对不成对出现
     */
    public <T extends ExtAbility> T findAbilityByCodeAndTags(Class<T> extType, String code, Object... tagsKeyValue) {
        List<T> abilities = findAbilitiesByCodeAndTags(extType, code, tagsKeyValue);
        return abilities.isEmpty() ? null : abilities.get(0);
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