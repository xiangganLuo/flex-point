package com.flexpoint.core.selector;

import java.util.Set;

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
    void register(Selector selector);
    
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
    void unregister(String selectorName);
    
    /**
     * 检查指定名称的选择器是否已注册
     * @param selectorName 选择器名称
     * @return 是否已注册
     */
    boolean has(String selectorName);

    /**
     * 获取所有已注册选择器的名称快照（用于诊断/加载报告）。
     * @return 选择器名称集合（不可变快照）
     */
    Set<String> getSelectorNames();

    /**
     * 已注册选择器数量。
     * @return 数量
     */
    int size();
} 