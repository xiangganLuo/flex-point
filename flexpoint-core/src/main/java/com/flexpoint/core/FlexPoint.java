package com.flexpoint.core;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.common.constants.FlexPointConstants;
import com.flexpoint.common.exception.SelectorNotFoundException;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.event.EventPublisher;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.ext.proxy.EventPublisherInvocationHandler;
import com.flexpoint.core.monitor.ExtMetrics;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.selector.Selector;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
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
     * TODO 需要对原始的ExtAbility进行代理并然后进行埋点
     */
    public <T extends ExtAbility> T findAbility(Class<T> extType) {
        String typeName = extType.getSimpleName();

        // 从扩展点接口的@FpSelector注解获取选择器名称
        FpSelector selectorAnno = extType.getAnnotation(FpSelector.class);
        if (selectorAnno == null) {
            log.warn("扩展点类型[{}]缺少@FpSelector注解", typeName);
            return null;
        }

        String selectorName = selectorAnno.value();

        // 发布选择器查找事件
        EventPublisher.publishSelectorFound(selectorName);

        Selector selector = selectorRegistry.getSelector(selectorName);
        if (selector == null) {
            log.warn("未找到名称为[{}]的选择器", selectorName);
            // 发布选择器未找到事件
            EventPublisher.publishSelectorNotFound(selectorName);
            throw new SelectorNotFoundException(selectorName, typeName);
        }

        List<T> exts = extAbilityRegistry.getAllExtAbility(extType);
        if (exts.isEmpty()) {
            log.warn("未找到扩展点实现: type={}", typeName);
            // 发布扩展点未找到事件
            EventPublisher.publishExtNotFound(extType);
            return null;
        }

        T ability = selector.select(exts);
        if (ability == null) {
            String errorMsg = "选择器未找到匹配的扩展点";
            log.warn("选择器[{}]未找到匹配的扩展点: type={}", selectorName, typeName);
            // 发布扩展点选择失败事件
            EventPublisher.publishExtSelectionFailed(extType, selectorName, errorMsg);
            return null;
        }

        // 发布扩展点选择事件
        EventPublisher.publishExtSelected(ability, selectorName);

        if (log.isDebugEnabled()) {
            log.debug("成功获取扩展点: type={}, code={}, selector={}, class={}",
                    typeName, ability.getCode(), selectorName, ability.getClass().getName());
        }

        return getProxy(extType, ability);
    }

    /**
     * 根据扩展点类型和code查找匹配的扩展点列表
     *
     * @param extType 扩展点类型
     * @param code 业务标识
     * @param <T> 扩展点类型
     * @return 匹配的扩展点列表
     */
    public <T extends ExtAbility> List<T> findAbilitiysByCode(Class<T> extType, String code) {
        List<T> exts = extAbilityRegistry.getAllExtAbility(extType);
        if (exts.isEmpty()) {
            // 发布扩展点未找到事件
            EventPublisher.publishExtNotFound(extType);
            return null;
        }
        List<T> matched = exts.stream()
                .filter(ext -> code.equals(ext.getCode()))
                .filter(ext -> code.equals(ext.getCode()))
                .map(ext -> getProxy(extType, ext))
                .collect(Collectors.toList());
        
        // 发布扩展点选择失败事件
        EventPublisher.publishExtSelectionFailed(extType, FlexPointConstants.CODE_SELECTOR_NAME, "未找到匹配的扩展点");
        return matched;
    }

    /**
     * 根据扩展点类型、code查找匹配的扩展点
     *
     * @param extType 扩展点类型
     * @param code 业务标识
     * @param <T> 扩展点类型
     * @return 匹配的扩展点
     */
    public <T extends ExtAbility> T findAbilityByCode(Class<T> extType, String code) {
        List<T> matched = findAbilitiysByCode(extType, code);
        return matched.isEmpty() ? null : matched.get(0);
    }

    /**
     * 根据扩展点类型、code和标签查找匹配的扩展点列表
     *
     * @param extType 扩展点类型
     * @param code 业务标识
     * @param tagsKeyValue 标签键值对
     * @param <T> 扩展点类型
     * @return 匹配的扩展点列表
     */
    public <T extends ExtAbility> List<T> findAbilitysByCodeAndTags(Class<T> extType, String code, Object... tagsKeyValue) {
        List<T> exts = extAbilityRegistry.getAllExtAbility(extType);
        if (exts.isEmpty()) {
            // 发布扩展点未找到事件
            EventPublisher.publishExtNotFound(extType);
            return Collections.emptyList();
        }

        // 构建标签映射
        Map<String, Object> tagMap = new HashMap<>();
        for (int i = 0; i < tagsKeyValue.length; i += 2) {
            if (i + 1 < tagsKeyValue.length) {
                tagMap.put(tagsKeyValue[i].toString(), tagsKeyValue[i + 1]);
            }
        }

        List<T> matched = exts.stream()
                .filter(ext -> code.equals(ext.getCode()))
                .filter(ext -> {
                    // 标签匹配逻辑
                    return tagMap.entrySet().stream()
                            .allMatch(entry -> {
                                Object extValue = ext.getTags().get(entry.getKey());
                                return entry.getValue().equals(extValue);
                            });
                })
                .map(ext -> getProxy(extType, ext))
                .collect(Collectors.toList());

        if (!matched.isEmpty()) {
            // 发布扩展点选择事件
            matched.forEach(ext -> EventPublisher.publishExtSelected(ext, FlexPointConstants.CODE_TAGS_SELECTOR_NAME));
        } else {
            // 发布扩展点选择失败事件
            EventPublisher.publishExtSelectionFailed(extType, FlexPointConstants.CODE_TAGS_SELECTOR_NAME, "未找到匹配的扩展点");
        }
        return matched;
    }

    /**
     * 根据扩展点类型、code和标签查找匹配的扩展点
     *
     * @param extType 扩展点类型
     * @param code 业务标识
     * @param tagsKeyValue 标签键值对
     * @param <T> 扩展点类型
     * @return 匹配的扩展点
     */
    public <T extends ExtAbility> T findAbilityByCodeAndTags(Class<T> extType, String code, Object... tagsKeyValue) {
        List<T> matched = findAbilitysByCodeAndTags(extType, code, tagsKeyValue);
        return matched.isEmpty() ? null : matched.get(0);
    }

    /**
     * 获取指定类型的所有扩展点
     *
     * @param extType 扩展点类型
     * @param <T> 扩展点类型
     * @return 扩展点列表
     */
    public <T extends ExtAbility> List<T> getAllExt(Class<T> extType) {
        return extAbilityRegistry.getAllExtAbility(extType);
    }

    /**
     * 获取注册的扩展点总数
     */
    public int getExtCount() {
        return extAbilityRegistry.getAllExtAbility(ExtAbility.class).size();
    }

    /**
     * 注册扩展点
     */
    public void register(ExtAbility ext) {
        extAbilityRegistry.register(ext);
    }
    
    /**
     * 注销扩展点
     */
    public void unregister(ExtAbility ext) {
        extAbilityRegistry.unregister(ext);
    }

    /**
     * ==================selector==================
     */
    
    /**
     * 注册选择器
     */
    public void registerSelector(Selector selector) {
        selectorRegistry.register(selector);
    }

    /**
     * 注销选择器
     */
    public void unregisterSelector(String selectorName) {
        selectorRegistry.unregister(selectorName);
    }

    /**
     * 检查是否有指定名称的选择器
     */
    public boolean hasSelector(String selectorName) {
        return selectorRegistry.has(selectorName);
    }

    /**
     * ==================monitor==================
     */
    
    /**
     * 获取扩展点调用统计
     */
    public ExtMetrics getExtMetrics(ExtAbility extAbility) {
        return extMonitor.getExtMetrics(extAbility);
    }

    /**
     * 获取所有扩展点调用统计
     */
    public Map<String, ExtMetrics> getAllExtMetrics() {
        return extMonitor.getAllExtMetrics();
    }

    private <T extends ExtAbility> T getProxy(Class<T> extType, T ability) {
        @SuppressWarnings("unchecked")
        T proxyInstance = (T) Proxy.newProxyInstance(
                extType.getClassLoader(),
                new Class[]{extType},
                new EventPublisherInvocationHandler(ability)
        );
        return proxyInstance;
    }

}