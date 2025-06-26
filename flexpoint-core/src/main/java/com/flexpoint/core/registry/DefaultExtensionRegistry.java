package com.flexpoint.core.registry;

import com.flexpoint.common.ExtensionAbility;
import com.flexpoint.core.metadata.ExtensionMetadata;
import com.flexpoint.common.utils.ExtensionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 默认扩展点注册中心实现
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@SuppressWarnings("unchecked")
public class DefaultExtensionRegistry implements ExtensionRegistry {
    
    // 扩展点实例存储: Class -> List<ExtensionAbility>
    private final Map<Class<? extends ExtensionAbility>, List<ExtensionAbility>> extensionInstances = new ConcurrentHashMap<>();
    
    // 扩展点元数据存储: Class -> Map<extensionId, ExtensionMetadata>
    private final Map<Class<? extends ExtensionAbility>, Map<String, ExtensionMetadata>> extensionMetadata = new ConcurrentHashMap<>();
    
    // 扩展点ID映射: Class -> Map<extensionId, ExtensionAbility>
    private final Map<Class<? extends ExtensionAbility>, Map<String, ExtensionAbility>> extensionIdMap = new ConcurrentHashMap<>();
    
    @Override
    public void register(Class<? extends ExtensionAbility> extensionType, ExtensionAbility instance, ExtensionMetadata metadata) {
        if (extensionType == null || instance == null) {
            log.warn("扩展点注册失败：类型或实例为空");
            return;
        }
        
        String extensionId = metadata != null ? metadata.getExtensionId() : instance.getClass().getSimpleName();
        
        // 注册扩展点实例
        extensionInstances.computeIfAbsent(extensionType, k -> new ArrayList<>()).add(instance);
        
        // 注册扩展点元数据
        extensionMetadata.computeIfAbsent(extensionType, k -> new HashMap<>()).put(extensionId, metadata);
        
        // 注册扩展点ID映射
        extensionIdMap.computeIfAbsent(extensionType, k -> new HashMap<>()).put(extensionId, instance);
        
        log.info("扩展点注册成功: type={}, id={}, class={}", 
                extensionType.getSimpleName(), extensionId, instance.getClass().getName());
    }
    
    @Override
    public <T extends ExtensionAbility> List<T> getExtensions(Class<T> extensionType, Map<String, Object> context) {
        List<ExtensionAbility> instances = extensionInstances.get(extensionType);
        if (instances == null) {
            return Collections.emptyList();
        }
        
        // 按优先级排序
        return instances.stream()
                .map(instance -> (T) instance)
                .sorted((a, b) -> {
                    String idA = ExtensionUtil.getExtensionId(a.getClass(), context);
                    String idB = ExtensionUtil.getExtensionId(b.getClass(), context);
                    ExtensionMetadata metaA = getExtensionMetadata(extensionType, idA);
                    ExtensionMetadata metaB = getExtensionMetadata(extensionType, idB);
                    int priorityA = metaA != null ? metaA.getPriority() : Integer.MAX_VALUE;
                    int priorityB = metaB != null ? metaB.getPriority() : Integer.MAX_VALUE;
                    return Integer.compare(priorityA, priorityB);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public <T extends ExtensionAbility> T getExtensionById(Class<T> extensionType, String extensionId) {
        Map<String, ExtensionAbility> idMap = extensionIdMap.get(extensionType);
        if (idMap == null) {
            return null;
        }
        return (T) idMap.get(extensionId);
    }
    
    @Override
    public ExtensionMetadata getExtensionMetadata(Class<? extends ExtensionAbility> extensionType, String extensionId) {
        Map<String, ExtensionMetadata> metadataMap = extensionMetadata.get(extensionType);
        if (metadataMap == null) {
            return null;
        }
        return metadataMap.get(extensionId);
    }
    
    @Override
    public void unregister(Class<? extends ExtensionAbility> extensionType, String extensionId) {
        // 移除实例
        List<ExtensionAbility> instances = extensionInstances.get(extensionType);
        if (instances != null) {
            instances.removeIf(instance -> ExtensionUtil.getExtensionId(instance.getClass(), null).equals(extensionId));
        }
        
        // 移除元数据
        Map<String, ExtensionMetadata> metadataMap = extensionMetadata.get(extensionType);
        if (metadataMap != null) {
            metadataMap.remove(extensionId);
        }
        
        // 移除ID映射
        Map<String, ExtensionAbility> idMap = extensionIdMap.get(extensionType);
        if (idMap != null) {
            idMap.remove(extensionId);
        }
        
        log.info("扩展点注销成功: type={}, id={}", extensionType.getSimpleName(), extensionId);
    }
    
    @Override
    public boolean exists(Class<? extends ExtensionAbility> extensionType, String extensionId) {
        Map<String, ExtensionAbility> idMap = extensionIdMap.get(extensionType);
        return idMap != null && idMap.containsKey(extensionId);
    }
    
    @Override
    public List<Class<? extends ExtensionAbility>> getRegisteredTypes() {
        return new ArrayList<>(extensionInstances.keySet());
    }
} 