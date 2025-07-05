package com.flexpoint.core.selector;

import com.flexpoint.core.extension.ExtensionAbility;

import java.util.List;

/**
 * 扩展点选择器接口
 * 支持基于元数据的扩展点选择逻辑
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ExtensionSelector {
    
    /**
     * 解析扩展点
     * 从扩展点候选者列表中选择合适的扩展点实现
     *
     * @param extensions 扩展点候选者列表（包含实例和元数据）
     * @param <T> 扩展点类型
     * @return 扩展点实例
     */
    <T extends ExtensionAbility> T resolve(List<T> extensions);
    
    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    String getName();
} 