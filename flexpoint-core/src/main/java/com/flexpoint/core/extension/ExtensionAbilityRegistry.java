package com.flexpoint.core.extension;

import java.util.List;

/**
 * 扩展点注册中心接口
 * 负责扩展点的注册、查找和管理
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ExtensionAbilityRegistry {
    
    /**
     * 注册扩展点
     *
     * @param instance 扩展点实例
     */
    void register(ExtensionAbility instance);
    
    /**
     * 获取指定类型的所有扩展点
     *
     * @param <T> 扩展点类型
     * @param extensionType 扩展点类型
     * @return 扩展点列表
     */
    <T extends ExtensionAbility> List<T> getAllExtensionAbility(Class<T> extensionType);

    /**
     * 根据ID获取扩展点
     *
     * @param extensionId 扩展点ID
     * @param <T> 扩展点类型
     * @return 扩展点实例
     */
    <T extends ExtensionAbility> T getExtensionById(String extensionId);

    /**
     * 注销扩展点
     *
     * @param extensionId 扩展点ID
     */
    void unregister(String extensionId);

    /**
     * 检查扩展点是否存在
     *
     * @param extensionId 扩展点ID
     * @return 是否存在
     */
    boolean exists(String extensionId);
    
}