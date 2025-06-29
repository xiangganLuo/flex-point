package com.flexpoint.core.registry;

import com.flexpoint.common.utils.ExtensionUtil;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.registry.metadata.ExtensionMetadata;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 默认扩展点注册中心实现
 * 支持配置控制注册行为
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@SuppressWarnings("unchecked")
public class FlexPointExtensionAbilityRegistry implements ExtensionAbilityRegistry {
    
    // 扩展点实例存储: Class -> List<ExtensionAbility>
    private final Map<Class<? extends ExtensionAbility>, List<ExtensionAbility>> extensionInstances = new ConcurrentHashMap<>();
    
    // 扩展点元数据存储: Class -> Map<extensionId, ExtensionMetadata>
    private final Map<Class<? extends ExtensionAbility>, Map<String, ExtensionMetadata>> extensionMetadata = new ConcurrentHashMap<>();
    
    // 扩展点ID映射: Class -> Map<extensionId, ExtensionAbility>
    private final Map<Class<? extends ExtensionAbility>, Map<String, ExtensionAbility>> extensionIdMap = new ConcurrentHashMap<>();
    
    // 配置
    private final FlexPointConfig.RegistryConfig config;
    
    /**
     * 使用默认配置创建注册中心
     */
    public FlexPointExtensionAbilityRegistry() {
        this(new FlexPointConfig.RegistryConfig());
    }
    
    /**
     * 使用指定配置创建注册中心
     */
    public FlexPointExtensionAbilityRegistry(FlexPointConfig.RegistryConfig config) {
        this.config = config;
        if (config.isEnabled()) {
            log.info("创建注册中心: allowDuplicateRegistration={}", config.isAllowDuplicateRegistration());
        } else {
            log.info("注册中心已禁用");
        }
    }
    
    @Override
    public void register(Class<? extends ExtensionAbility> extensionType, ExtensionAbility instance, ExtensionMetadata metadata) {
        if (!config.isEnabled()) {
            return;
        }
        
        if (extensionType == null || instance == null) {
            log.warn("扩展点注册失败：类型或实例为空");
            return;
        }
        
        String extensionId = metadata != null ? metadata.getExtensionId() : ExtensionUtil.getExtensionId(extensionType, instance.getCode());
        
        // 检查是否允许重复注册
        if (!config.isAllowDuplicateRegistration() && exists(extensionType, extensionId)) {
            log.warn("扩展点已存在，不允许重复注册: type={}, id={}", extensionType.getSimpleName(), extensionId);
            return;
        }
        
        // 注册扩展点实例
        extensionInstances.computeIfAbsent(extensionType, k -> new ArrayList<>()).add(instance);
        
        // 注册扩展点元数据
        extensionMetadata.computeIfAbsent(extensionType, k -> new HashMap<>()).put(extensionId, metadata);
        
        // 注册扩展点ID映射
        extensionIdMap.computeIfAbsent(extensionType, k -> new HashMap<>()).put(extensionId, instance);
        
        log.info("扩展点注册成功: type={}, id={}", extensionType.getSimpleName(), extensionId);
    }
    
    @Override
    public <T extends ExtensionAbility> List<T> getExtensions(Class<T> extensionType) {
        if (!config.isEnabled()) {
            return Collections.emptyList();
        }
        
        List<ExtensionAbility> instances = extensionInstances.get(extensionType);
        if (instances == null) {
            return Collections.emptyList();
        }
        
        // 按优先级排序
        return instances.stream()
                .map(instance -> (T) instance)
                .sorted((a, b) -> {
                    String idA = ExtensionUtil.getExtensionId(a.getClass(), a.getCode());
                    String idB = ExtensionUtil.getExtensionId(b.getClass(), b.getCode());
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
        if (!config.isEnabled()) {
            return null;
        }
        
        Map<String, ExtensionAbility> idMap = extensionIdMap.get(extensionType);
        if (idMap == null) {
            return null;
        }
        return (T) idMap.get(extensionId);
    }
    
    @Override
    public ExtensionMetadata getExtensionMetadata(Class<? extends ExtensionAbility> extensionType, String extensionId) {
        if (!config.isEnabled()) {
            return null;
        }
        
        Map<String, ExtensionMetadata> metadataMap = extensionMetadata.get(extensionType);
        if (metadataMap == null) {
            return null;
        }
        return metadataMap.get(extensionId);
    }
    
    @Override
    public void unregister(Class<? extends ExtensionAbility> extensionType, String extensionId) {
        if (!config.isEnabled()) {
            return;
        }
        
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
        if (!config.isEnabled()) {
            return false;
        }
        
        Map<String, ExtensionAbility> idMap = extensionIdMap.get(extensionType);
        return idMap != null && idMap.containsKey(extensionId);
    }
    
    @Override
    public List<Class<? extends ExtensionAbility>> getRegisteredTypes() {
        if (!config.isEnabled()) {
            return Collections.emptyList();
        }
        
        return new ArrayList<>(extensionInstances.keySet());
    }
} 