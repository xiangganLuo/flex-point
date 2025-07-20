package com.flexpoint.core.ext;

import java.util.List;

/**
 * 扩展点注册中心接口
 * 负责扩展点的注册、查找和管理
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ExtAbilityRegistry {
    
    /**
     * 注册扩展点
     *
     * @param instance 扩展点实例
     */
    void register(ExtAbility instance);
    
    /**
     * 获取指定类型的所有扩展点
     *
     * @param <T> 扩展点类型
     * @param extType 扩展点类型
     * @return 扩展点列表
     */
    <T extends ExtAbility> List<T> getAllExtAbility(Class<T> extType);

}