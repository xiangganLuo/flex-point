package com.flexpoint.core.selector;

/**
 * 选择器链注册表接口
 * @author luoxianggan
 */
public interface SelectorRegistry {
    /**
     * 获取选择器链
     * @param name 链名
     * @return 选择器链
     */
    SelectorChain getSelectorChain(String name);

    /**
     * 注册选择器链
     * @param chain 选择器链
     */
    void registerSelectorChain(SelectorChain chain);
    /**
     * 移除选择器链
     * @param chainName 链名字
     */
    void unregisterSelectorChain(String chainName);
    /**
     * 动态注册选择器到指定链
     *
     * @param chainName 链名字
     * @param selector 选择器
     */
    void registerSelector(String chainName, Selector selector);
    /**
     * 从指定链中移除选择器
     *
     * @param chainName 链名字
     * @param selectorName 选择器名字
     */
    void unregisterSelector(String chainName, String selectorName);
} 