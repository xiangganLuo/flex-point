package com.flexpoint.example.java.resolution;

import com.flexpoint.core.resolution.AbstractExtensionResolutionStrategy;

import java.util.Map;

/**
 * 自定义扩展点解析策略示例
 * 演示如何继承AbstractExtensionResolutionStrategy并实现extractCode方法
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class CustomExtensionResolutionStrategy extends AbstractExtensionResolutionStrategy {

    @Override
    protected String extractCode(Map<String, Object> context) {
        // 示例：从上下文中提取appCode字段作为code
        if (context != null && context.containsKey("appCode")) {
            return (String) context.get("appCode");
        }
        
        // 示例：从上下文中提取tenantId字段作为code
        if (context != null && context.containsKey("tenantId")) {
            return (String) context.get("tenantId");
        }
        
        // 示例：从上下文中提取businessType字段作为code
        if (context != null && context.containsKey("businessType")) {
            return (String) context.get("businessType");
        }
        
        return null;
    }
    
    @Override
    public String getStrategyName() {
        return "CustomExtensionResolutionStrategy";
    }
} 