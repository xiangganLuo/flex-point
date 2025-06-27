package com.flexpoint.core.resolution;

import com.flexpoint.core.extension.ExtensionAbility;

import java.util.List;
import java.util.Map;

/**
 * 扩展点解析策略接口
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ExtensionResolutionStrategy {
    
    /**
     * 解析扩展点
     *
     * @param extensions 扩展点列表
     * @param context 上下文信息
     * @param <T> 扩展点类型
     * @return 解析后的扩展点
     */
    <T extends ExtensionAbility> T resolve(List<T> extensions, Map<String, Object> context);
    
    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    String getStrategyName();
} 