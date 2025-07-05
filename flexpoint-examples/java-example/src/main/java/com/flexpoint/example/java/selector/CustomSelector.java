package com.flexpoint.example.java.selector;

import com.flexpoint.core.selector.AbstractSelector;
import com.flexpoint.core.selector.SelectionContext;

/**
 * 自定义扩展点选择器示例
 * 演示如何继承AbstractSelector并实现extractContext方法
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class CustomSelector extends AbstractSelector {


    @Override
    protected SelectionContext extractContext() {
        return new SelectionContext("mall", null);
    }

    @Override
    public String getName() {
        return "CustomExtensionSelector";
    }
} 