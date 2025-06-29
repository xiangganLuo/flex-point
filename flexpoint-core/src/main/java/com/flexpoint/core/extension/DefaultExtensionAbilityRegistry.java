package com.flexpoint.core.extension;

import com.flexpoint.core.config.FlexPointConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.flexpoint.common.utils.ExtensionUtil.getExtensionId;

/**
 * 默认扩展点注册中心实现
 * 支持扩展点注册、查找、注销、存在性判断
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public class DefaultExtensionAbilityRegistry implements ExtensionAbilityRegistry {

    private final FlexPointConfig.RegistryConfig registryConfig;
    // 类型 -> 扩展点实例列表
    private final Map<Class<? extends ExtensionAbility>, List<ExtensionAbility>> extensionMap = new ConcurrentHashMap<>();

    // 扩展点ID -> 实例
    private final Map<String, ExtensionAbility> instanceMap = new ConcurrentHashMap<>();

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
        
        // 修复：正确传递参数
        String extensionId = getExtensionId(instance.getCode(), instance.version());
        if (!registryConfig.isAllowDuplicateRegistration() && instanceMap.containsKey(extensionId)) {
            throw new IllegalStateException("扩展点已存在，不允许重复注册: " + extensionId);
        }
        
        // 注册到类型映射
        extensionMap.computeIfAbsent(extensionType, k -> new ArrayList<>()).add(instance);
        
        // 注册到实例映射
        instanceMap.put(extensionId, instance);
    }

    @Override
    public <T extends ExtensionAbility> List<T> getAllExtensionAbility(Class<T> extensionType) {
        List<ExtensionAbility> list = extensionMap.get(extensionType);
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(extensionType::cast).collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ExtensionAbility> T getExtensionById(String extensionId) {
        return (T) instanceMap.get(extensionId);
    }

    @Override
    public synchronized void unregister(String extensionId) {
        ExtensionAbility instance = instanceMap.remove(extensionId);
        if (instance != null) {
            Class<? extends ExtensionAbility> extensionType = getExtensionType(instance);
            if (extensionType != null) {
                List<ExtensionAbility> list = extensionMap.get(extensionType);
                if (list != null) {
                    list.remove(instance);
                    if (list.isEmpty()) {
                        extensionMap.remove(extensionType);
                    }
                }
            }
        }
    }

    @Override
    public boolean exists(String extensionId) {
        return instanceMap.containsKey(extensionId);
    }

    private Class<? extends ExtensionAbility> getExtensionType(ExtensionAbility instance) {
        for (Class<?> iface : instance.getClass().getInterfaces()) {
            if (ExtensionAbility.class.isAssignableFrom(iface)) {
                @SuppressWarnings("unchecked")
                Class<? extends ExtensionAbility> type = (Class<? extends ExtensionAbility>) iface;
                return type;
            }
        }
        return null;
    }
}
