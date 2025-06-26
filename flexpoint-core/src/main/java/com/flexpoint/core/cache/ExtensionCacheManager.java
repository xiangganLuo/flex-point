package com.flexpoint.core.cache;

import com.flexpoint.common.ExtensionAbility;

import java.util.concurrent.TimeUnit;

/**
 * 扩展点缓存管理器接口
 * 提供扩展点实例的缓存功能
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ExtensionCacheManager {
    
    /**
     * 获取缓存的扩展点
     *
     * @param extensionType 扩展点类型
     * @param key 缓存键
     * @param <T> 扩展点类型
     * @return 缓存的扩展点
     */
    <T extends ExtensionAbility> T getCachedExtension(Class<T> extensionType, String key);
    
    /**
     * 缓存扩展点
     *
     * @param extensionType 扩展点类型
     * @param key 缓存键
     * @param extension 扩展点实例
     * @param <T> 扩展点类型
     */
    <T extends ExtensionAbility> void cacheExtension(Class<T> extensionType, String key, T extension);
    
    /**
     * 缓存扩展点（带过期时间）
     *
     * @param extensionType 扩展点类型
     * @param key 缓存键
     * @param extension 扩展点实例
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @param <T> 扩展点类型
     */
    <T extends ExtensionAbility> void cacheExtension(Class<T> extensionType, String key, T extension,
                                                     long expireTime, TimeUnit timeUnit);
    
    /**
     * 使缓存失效
     *
     * @param extensionType 扩展点类型
     */
    void invalidateCache(Class<? extends ExtensionAbility> extensionType);
    
    /**
     * 使指定键的缓存失效
     *
     * @param extensionType 扩展点类型
     * @param key 缓存键
     */
    void invalidateCache(Class<? extends ExtensionAbility> extensionType, String key);
    
    /**
     * 清空所有缓存
     */
    void clearAllCache();
    
    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    CacheStatistics getCacheStatistics();
    
    /**
     * 缓存统计信息
     */
    interface CacheStatistics {
        long getHitCount();
        long getMissCount();
        double getHitRate();
        long getCacheSize();
    }
} 