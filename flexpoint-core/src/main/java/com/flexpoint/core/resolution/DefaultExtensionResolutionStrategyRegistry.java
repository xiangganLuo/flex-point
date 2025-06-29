package com.flexpoint.core.resolution;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 默认扩展点解析策略注册表实现
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class DefaultExtensionResolutionStrategyRegistry implements ExtensionResolutionStrategyRegistry {
    
    private final Map<String, ExtensionResolutionStrategy> strategies = new ConcurrentHashMap<>();

    public DefaultExtensionResolutionStrategyRegistry() {
    }
    
    @Override
    public ExtensionResolutionStrategy getStrategy(String strategyName) {
        if (strategyName == null || strategyName.trim().isEmpty()) {
            return null;
        }
        
        ExtensionResolutionStrategy strategy = strategies.get(strategyName);
        if (strategy == null) {
            log.warn("未找到解析策略: {}", strategyName);
            return null;
        }
        return strategy;
    }
    
    /**
     * 注册解析策略
     *
     * @param strategy 解析策略实例
     */
    @Override
    public void registerStrategy(ExtensionResolutionStrategy strategy) {
        if (strategy == null || strategy.getStrategyName() == null) {
            log.warn("解析策略注册失败: 策略名称为空");
            return;
        }
        
        strategies.put(strategy.getStrategyName(), strategy);
        log.info("注册解析策略: {}", strategy.getStrategyName());
    }
} 