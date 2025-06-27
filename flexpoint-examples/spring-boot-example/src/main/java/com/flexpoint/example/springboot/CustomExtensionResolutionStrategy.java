package com.flexpoint.example.springboot;

import com.flexpoint.core.resolution.AbstractExtensionResolutionStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 自定义扩展点解析策略示例
 * 演示Spring Boot环境下的自动注册
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Component
public class CustomExtensionResolutionStrategy extends AbstractExtensionResolutionStrategy {

    @Override
    protected String extractCode(Map<String, Object> context) {
        // 示例：从上下文中提取appCode字段作为code
        if (context != null && context.containsKey("appCode")) {
            return (String) context.get("appCode");
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