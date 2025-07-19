package com.flexpoint.core.extension;

import com.flexpoint.core.config.FlexPointConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 默认扩展点注册中心实现
 * 支持扩展点注册、查找
 * 一个code可以对应多个扩展点实现，通过标签区分
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public class DefaultExtensionAbilityRegistry implements ExtensionAbilityRegistry {

    private final FlexPointConfig.RegistryConfig registryConfig;
    // 类型 -> 扩展点实例列表
    private final Map<Class<? extends ExtensionAbility>, List<ExtensionAbility>> extensionMap = new ConcurrentHashMap<>();

    public DefaultExtensionAbilityRegistry(FlexPointConfig.RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    @Override
    public synchronized void register(ExtensionAbility instance) {
        if (instance == null) {
            throw new IllegalArgumentException("扩展点实例不能为空");
        }
        
        Class<? extends ExtensionAbility> extensionType = getExtensionType(instance);
        if (extensionType == null) {
            throw new IllegalArgumentException("扩展点实例必须实现ExtensionAbility接口: " + instance.getClass().getName());
        }
        
        // 注册到类型映射 - 允许同一个code的多个实现
        extensionMap.computeIfAbsent(extensionType, k -> new ArrayList<>()).add(instance);
    }

    @Override
    public <T extends ExtensionAbility> List<T> getAllExtensionAbility(Class<T> extensionType) {
        List<ExtensionAbility> list = extensionMap.get(extensionType);
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(extensionType::cast).collect(Collectors.toList());
    }

    /**
     * 获取扩展点类型
     * 查找实例实现的第一个ExtensionAbility接口
     */
    @SuppressWarnings("unchecked")
    private Class<? extends ExtensionAbility> getExtensionType(ExtensionAbility instance) {
        Class<?> clazz = instance.getClass();
        
        // 检查直接实现的接口
        for (Class<?> iface : clazz.getInterfaces()) {
            if (ExtensionAbility.class.isAssignableFrom(iface) && iface != ExtensionAbility.class) {
                return (Class<? extends ExtensionAbility>) iface;
            }
        }
        
        // 检查父类的接口
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            for (Class<?> iface : superClass.getInterfaces()) {
                if (ExtensionAbility.class.isAssignableFrom(iface) && iface != ExtensionAbility.class) {
                    return (Class<? extends ExtensionAbility>) iface;
                }
            }
            superClass = superClass.getSuperclass();
        }
        
        // 如果没找到具体的业务接口，返回ExtensionAbility
        return ExtensionAbility.class;
    }

    /**
     * 获取注册的扩展点总数
     */
    public int getRegisteredCount() {
        return extensionMap.values().stream()
            .mapToInt(List::size)
            .sum();
    }

    /**
     * 获取指定类型的扩展点数量
     */
    public <T extends ExtensionAbility> int getCountByType(Class<T> extensionType) {
        List<ExtensionAbility> list = extensionMap.get(extensionType);
        return list != null ? list.size() : 0;
    }

    /**
     * 清空所有注册的扩展点
     */
    public synchronized void clear() {
        extensionMap.clear();
    }

    /**
     * 检查是否有指定类型的扩展点
     */
    public <T extends ExtensionAbility> boolean hasExtension(Class<T> extensionType) {
        return getCountByType(extensionType) > 0;
    }
}
