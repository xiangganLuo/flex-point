package com.flexpoint.core.resolution;

/**
 * 扩展点解析策略注册表接口
 * 负责管理不同的扩展点解析策略，支持策略的注册、存储和获取
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ExtensionResolutionStrategyRegistry {
    
    /**
     * 获取解析策略
     *
     * @param strategyName 策略名称
     * @return 解析策略实例
     */
    ExtensionResolutionStrategy getStrategy(String strategyName);

    /**
     * 注册解析策略
     * @param strategy 解析策略实例
     */
    void registerStrategy(ExtensionResolutionStrategy strategy);
} 