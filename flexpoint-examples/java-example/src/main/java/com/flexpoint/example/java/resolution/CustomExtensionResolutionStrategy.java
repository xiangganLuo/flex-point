package com.flexpoint.example.java.resolution;

import com.flexpoint.core.resolution.AbstractExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ResolutionContext;

/**
 * 自定义扩展点解析策略示例
 * 演示如何继承AbstractExtensionResolutionStrategy并实现selectExtension方法
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class CustomExtensionResolutionStrategy extends AbstractExtensionResolutionStrategy {


    @Override
    protected ResolutionContext extractContext() {
        return new ResolutionContext("mall", null);
    }

    @Override
    public String getStrategyName() {
        return "CustomExtensionResolutionStrategy";
    }
} 