package com.flexpoint.core.ext;

import com.flexpoint.core.config.FlexPointConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.flexpoint.core.utils.ExtUtil.getExtType;

/**
 * 默认扩展点注册中心实现
 * 支持扩展点注册、查找
 * 一个code可以对应多个扩展点实现，通过标签区分
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public class DefaultExtAbilityRegistry implements ExtAbilityRegistry {

    private final FlexPointConfig.RegistryConfig registryConfig;
    // 类型 -> 扩展点实例列表
    private final Map<Class<? extends ExtAbility>, List<ExtAbility>> extMap = new ConcurrentHashMap<>();

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
        extMap.computeIfAbsent(extType, k -> new ArrayList<>()).add(instance);
    }

    @Override
    public <T extends ExtAbility> List<T> getAllExtAbility(Class<T> extType) {
        List<ExtAbility> list = extMap.get(extType);
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(extType::cast).collect(Collectors.toList());
    }

    /**
     * 获取注册的扩展点总数
     */
    public int getRegisteredCount() {
        return extMap.values().stream()
            .mapToInt(List::size)
            .sum();
    }

    /**
     * 获取指定类型的扩展点数量
     */
    public <T extends ExtAbility> int getCountByType(Class<T> extType) {
        List<ExtAbility> list = extMap.get(extType);
        return list != null ? list.size() : 0;
    }

    /**
     * 清空所有注册的扩展点
     */
    public synchronized void clear() {
        extMap.clear();
    }

    /**
     * 检查是否有指定类型的扩展点
     */
    public <T extends ExtAbility> boolean hasExt(Class<T> extType) {
        return getCountByType(extType) > 0;
    }
}
