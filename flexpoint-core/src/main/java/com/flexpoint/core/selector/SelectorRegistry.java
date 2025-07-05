package com.flexpoint.core.selector;

/**
 * 扩展点选择器注册表接口
 * 负责管理不同的扩展点选择器，支持选择器的注册和获取
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface SelectorRegistry {
    
    /**
     * 获取选择器
     *
     * @param name 策略名称
     * @return 选择器实例
     */
    ExtensionSelector getSelector(String name);

    /**
     * 注册选择器
     * @param strategy 选择器实例
     */
    void registerSelector(ExtensionSelector selector);
} 