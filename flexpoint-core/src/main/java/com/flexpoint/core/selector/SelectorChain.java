package com.flexpoint.core.selector;

import com.flexpoint.core.context.Context;
import com.flexpoint.core.extension.ExtensionAbility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 选择器链，按顺序依次调用链上选择器，返回第一个非 null 结果。
 * @author luoxianggan
 */
public class SelectorChain implements Selector {
    private final List<Selector> chain = new ArrayList<>();
    private final String name;

    public SelectorChain(String name) {
        this.name = name;
    }

    public void addSelector(Selector selector) {
        if (selector != null) {
            chain.add(selector);
        }
    }

    public void removeSelector(String selectorName) {
        chain.removeIf(s -> s.getName().equals(selectorName));
    }

    public void clear() {
        chain.clear();
    }

    public List<Selector> getChain() {
        return Collections.unmodifiableList(chain);
    }

    @Override
    public <T extends ExtensionAbility> T select(List<T> candidates, Context context) {
        for (Selector selector : chain) {
            T result = selector.select(candidates, context);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return name;
    }
} 