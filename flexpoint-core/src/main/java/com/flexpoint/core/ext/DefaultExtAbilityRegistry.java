package com.flexpoint.core.ext;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.event.EventContext;
import com.flexpoint.core.event.EventPublisher;
import com.flexpoint.core.event.EventType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.flexpoint.core.utils.ExtUtil.getExtType;

/**
 * 默认扩展点注册中心实现
 * 支持扩展点注册、查找、事件发布
 * 一个code可以对应多个扩展点实现，通过标签区分
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
@Slf4j
public class DefaultExtAbilityRegistry implements ExtAbilityRegistry {

    private final FlexPointConfig.RegistryConfig registryConfig;

    // 类型 -> 扩展点实例列表
    private final Map<Class<? extends ExtAbility>, List<ExtAbility>> extAbilityMap = new ConcurrentHashMap<>();

    public DefaultExtAbilityRegistry(FlexPointConfig.RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    @Override
    public synchronized void register(ExtAbility instance) {
        if (instance == null) {
            throw new IllegalArgumentException("扩展点实例不能为空");
        }
        
        Class<? extends ExtAbility> extType = getExtType(instance);
        if (extType == null) {
            throw new IllegalArgumentException("扩展点实例必须实现ExtAbility接口: " + instance.getClass().getName());
        }

        // 注册到类型映射 - 允许同一个code的多个实现
        extAbilityMap.computeIfAbsent(extType, k -> new ArrayList<>()).add(instance);
        
        // 发布注册事件
        publishEvent(EventType.EXT_REGISTERED, instance);
        
        log.info("扩展点注册成功: type={}, code={}, class={}", 
            extType.getSimpleName(), instance.getCode(), instance.getClass().getName());
    }
    
    @Override
    public synchronized void unregister(ExtAbility instance) {
        if (instance == null) {
            return;
        }
        
        Class<? extends ExtAbility> extType = getExtType(instance);
        if (extType == null) {
            return;
        }
        
        List<ExtAbility> list = extAbilityMap.get(extType);
        if (list != null && list.remove(instance)) {
            // 发布注销事件
            publishEvent(EventType.EXT_UNREGISTERED, instance);
            
            log.info("扩展点注销成功: type={}, code={}, class={}", 
                extType.getSimpleName(), instance.getCode(), instance.getClass().getName());
        }
    }

    @Override
    public <T extends ExtAbility> List<T> getAllExtAbility(Class<T> extType) {
        List<ExtAbility> list = extAbilityMap.get(extType);
        if (list == null) {
            // 发布未找到事件
            publishEvent(EventType.EXT_NOT_FOUND, null, extType);
            return Collections.emptyList();
        }
        
        // 发布找到事件
        publishEvent(EventType.EXT_FOUND, null, extType);
        
        return list.stream().map(extType::cast).collect(Collectors.toList());
    }
    
    /**
     * 发布事件
     */
    private void publishEvent(EventType eventType, ExtAbility extAbility) {
        publishEvent(eventType, extAbility, null);
    }
    
    /**
     * 发布事件
     */
    private void publishEvent(EventType eventType, ExtAbility extAbility, Class<? extends ExtAbility> extType) {
            EventContext eventContext = EventContext.createExtEvent(eventType, extAbility);
            if (extType != null) {
                eventContext.setExtType(extType);
            }
        EventPublisher.publishEventAsync(eventContext);
    }

    /**
     * 获取注册的扩展点总数
     */
    public int getRegisteredCount() {
        return extAbilityMap.values().stream()
            .mapToInt(List::size)
            .sum();
    }

    /**
     * 获取指定类型的扩展点数量
     */
    public <T extends ExtAbility> int getCountByType(Class<T> extType) {
        List<ExtAbility> list = extAbilityMap.get(extType);
        return list != null ? list.size() : 0;
    }

    /**
     * 清空所有注册的扩展点
     */
    public synchronized void clear() {
        // 发布注销事件
        extAbilityMap.values().stream()
            .flatMap(List::stream)
            .forEach(ext -> publishEvent(EventType.EXT_UNREGISTERED, ext));

        extAbilityMap.clear();
        log.info("清空所有扩展点");
    }

    /**
     * 检查是否有指定类型的扩展点
     */
    public <T extends ExtAbility> boolean hasExt(Class<T> extType) {
        return getCountByType(extType) > 0;
    }
}
