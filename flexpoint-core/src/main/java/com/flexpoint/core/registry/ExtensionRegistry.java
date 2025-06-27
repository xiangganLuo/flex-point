package com.flexpoint.core.registry;

import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.registry.metadata.ExtensionMetadata;

import java.util.List;

/**
 * 扩展点注册中心接口
 * 负责扩展点的注册、查找和管理
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ExtensionRegistry {
    
    /**
     * 注册扩展点
     *
     * @param extensionType 扩展点类型
     * @param instance 扩展点实例
     * @param metadata 扩展点元数据
     */
    void register(Class<? extends ExtensionAbility> extensionType, ExtensionAbility instance, ExtensionMetadata metadata);
    
    /**
     * 获取指定类型的所有扩展点
     *
     * @param <T> 扩展点类型
     * @param extensionType 扩展点类型
     * @param context 上下文
     * @return 扩展点列表
     */
    <T extends ExtensionAbility> List<T> getExtensions(Class<T> extensionType);
    
    /**
     * 根据ID获取扩展点
     *
     * @param extensionType 扩展点类型
     * @param extensionId 扩展点ID
     * @param <T> 扩展点类型
     * @return 扩展点实例
     */
    <T extends ExtensionAbility> T getExtensionById(Class<T> extensionType, String extensionId);
    
    /**
     * 获取扩展点元数据
     *
     * @param extensionType 扩展点类型
     * @param extensionId 扩展点ID
     * @return 扩展点元数据
     */
    ExtensionMetadata getExtensionMetadata(Class<? extends ExtensionAbility> extensionType, String extensionId);
    
    /**
     * 注销扩展点
     *
     * @param extensionType 扩展点类型
     * @param extensionId 扩展点ID
     */
    void unregister(Class<? extends ExtensionAbility> extensionType, String extensionId);
    
    /**
     * 检查扩展点是否存在
     *
     * @param extensionType 扩展点类型
     * @param extensionId 扩展点ID
     * @return 是否存在
     */
    boolean exists(Class<? extends ExtensionAbility> extensionType, String extensionId);
    
    /**
     * 获取所有已注册的扩展点类型
     *
     * @return 扩展点类型列表
     */
    List<Class<? extends ExtensionAbility>> getRegisteredTypes();
} 