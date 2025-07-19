package com.flexpoint.core.selector;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认选择器注册表实现
 * 管理选择器名称与选择器实例的映射关系
 * @author luoxianggan
 */
@Slf4j
public class DefaultSelectorRegistry implements SelectorRegistry {
    
    private final Map<String, Selector> selectorMap = new ConcurrentHashMap<>();
    
    @Override
    public void registerSelector(Selector selector) {
        if (selector == null) {
            log.warn("选择器为空，无法注册");
            return;
        }
        
        String selectorName = selector.getName();
        if (selectorName == null || selectorName.trim().isEmpty()) {
            log.warn("选择器名称为空，无法注册");
            return;
        }
        
        selectorMap.put(selectorName, selector);
        log.info("注册选择器[{}]", selectorName);
    }
    
    @Override
    public Selector getSelector(String selectorName) {
        return selectorMap.get(selectorName);
    }
    
    @Override
    public void unregisterSelector(String selectorName) {
        if (selectorName == null || selectorName.trim().isEmpty()) {
            log.warn("选择器名称为空，无法移除");
            return;
        }
        
        Selector removed = selectorMap.remove(selectorName);
        if (removed != null) {
            log.info("移除选择器[{}]", selectorName);
        } else {
            log.warn("选择器[{}]未注册", selectorName);
        }
    }
    
    @Override
    public boolean hasSelector(String selectorName) {
        return selectorMap.containsKey(selectorName);
    }
}
