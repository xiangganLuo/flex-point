package com.flexpoint.core.selector;

import com.flexpoint.core.event.EventDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认选择器注册表实现
 * 管理选择器名称与选择器实例的映射关系
 * @author xiangganluo
 */
@Slf4j
public class DefaultSelectorRegistry implements SelectorRegistry {
    
    private final Map<String, Selector> selectorMap = new ConcurrentHashMap<>();
    private final EventDispatcher eventDispatcher;

    public DefaultSelectorRegistry(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }
    
    @Override
    public void register(Selector selector) {
        if (selector == null) {
            log.warn("选择器为空，无法注册");
            return;
        }
        
        String selectorName = selector.getName();
        if (selectorName == null || selectorName.trim().isEmpty()) {
            log.warn("选择器名称为空，无法注册");
            return;
        }

        // 资源级唯一：同名选择器不可被覆盖
        if (selectorMap.containsKey(selectorName)) {
            throw new IllegalStateException("选择器名称已存在，禁止覆盖: " + selectorName);
        }

        selectorMap.put(selectorName, selector);
        // 发布选择器注册事件
        eventDispatcher.publishSelectorRegistered(selector.getName());
        log.info("注册选择器[{}]", selectorName);
    }
    
    @Override
    public Selector getSelector(String selectorName) {
        return selectorMap.get(selectorName);
    }
    
    @Override
    public void unregister(String selectorName) {
        if (selectorName == null || selectorName.trim().isEmpty()) {
            log.warn("选择器名称为空，无法移除");
            return;
        }
        
        Selector removed = selectorMap.remove(selectorName);
        // 发布选择器注销事件
        eventDispatcher.publishSelectorUnregistered(selectorName);
        if (removed != null) {
            log.info("移除选择器[{}]", selectorName);
        } else {
            log.warn("选择器[{}]未注册", selectorName);
        }
    }
    
    @Override
    public boolean has(String selectorName) {
        return selectorMap.containsKey(selectorName);
    }
}
