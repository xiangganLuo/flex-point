package com.flexpoint.core.selector;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 默认扩展点选择器注册表实现
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class DefaultSelectorRegistry implements SelectorRegistry {
    
    private final Map<String, ExtensionSelector> selectors = new ConcurrentHashMap<>();

    public DefaultSelectorRegistry() {
    }
    
    @Override
    public ExtensionSelector getSelector(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        ExtensionSelector selector = selectors.get(name);
        if (selector == null) {
            log.warn("未找到选择器: {}", name);
            return null;
        }
        return selector;
    }
    
    /**
     * 注册选择器
     *
     * @param selector 选择器实例
     */
    @Override
    public void registerSelector(ExtensionSelector selector) {
        if (selector == null || selector.getName() == null) {
            log.warn("选择器注册失败: 策略名称为空");
            return;
        }
        
        selectors.put(selector.getName(), selector);
        log.info("注册选择器: {}", selector.getName());
    }
} 