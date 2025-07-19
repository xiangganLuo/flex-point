package com.flexpoint.core.selector;

/**
 * 选择器注册表接口
 * 管理选择器名称与选择器实例的映射关系
 * @author xiangganluo
 */
public interface SelectorRegistry {
    
    /**
     * 注册选择器
     * @param selector 选择器实例
     */
    void registerSelector(Selector selector);
    
    /**
     * 根据名称获取选择器
     * @param selectorName 选择器名称
     * @return 选择器实例，如果未注册则返回null
     */
    Selector getSelector(String selectorName);
    
    /**
     * 移除指定名称的选择器
     * @param selectorName 选择器名称
     */
    void unregisterSelector(String selectorName);
    
    /**
     * 检查指定名称的选择器是否已注册
     * @param selectorName 选择器名称
     * @return 是否已注册
     */
    boolean hasSelector(String selectorName);
} 