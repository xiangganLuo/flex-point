package com.flexpoint.core.selector;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认选择器链注册表实现
 * @author luoxianggan
 */
@Slf4j
public class DefaultSelectorRegistry implements SelectorRegistry {
    private final Map<String, SelectorChain> chains = new ConcurrentHashMap<>();
    @Override
    public SelectorChain getSelectorChain(String name) {
        return chains.get(name);
    }

    @Override
    public void registerSelectorChain(SelectorChain chain) {
        if (chain != null && chain.getName() != null) {
            chains.put(chain.getName(), chain);
        }
    }

    @Override
    public void unregisterSelectorChain(String chainName) {
        chains.remove(chainName);
    }

    @Override
    public void registerSelector(String chainName, Selector selector) {
        if (chainName == null || selector == null) {
            log.warn("链名字或选择器为空，无法注册");
            return;
        }

        // 获取或创建选择器链
        SelectorChain chain = getSelectorChain(chainName);
        if (chain == null) {
            chain = new SelectorChain(chainName);
            registerSelectorChain(chain);
            log.info("创建新的选择器链: {}", chainName);
        }

        // 添加选择器到链中
        chain.addSelector(selector);
        log.info("选择器[{}] 已添加到链[{}]", selector.getName(), chainName);
    }

    @Override
    public void unregisterSelector(String chainName, String selectorName) {
        if (chainName == null || selectorName == null) {
            log.warn("链名字或选择器名字为空，无法移除");
            return;
        }

        SelectorChain chain = getSelectorChain(chainName);
        if (chain == null) {
            log.warn("未找到选择器链: {}", chainName);
            return;
        }

        chain.removeSelector(selectorName);
        log.info("从链[{}] 中移除选择器[{}]", chainName, selectorName);
    }

}
