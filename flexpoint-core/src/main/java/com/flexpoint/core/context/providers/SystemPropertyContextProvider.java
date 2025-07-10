package com.flexpoint.core.context.providers;

import com.flexpoint.core.context.Context;
import com.flexpoint.core.context.ContextProvider;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于系统属性的上下文提供者
 * 支持从系统属性、环境变量等获取上下文信息，适用于配置驱动的场景
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class SystemPropertyContextProvider implements ContextProvider {

    private final Map<String, String> propertyMappings = new HashMap<>();

    public SystemPropertyContextProvider() {
        propertyMappings.put("env", "flexpoint.context.env");
        propertyMappings.put("region", "flexpoint.context.region");
    }

    /**
     * 添加属性映射
     *
     * @param contextKey 上下文键
     * @param propertyKey 系统属性键
     */
    public void addPropertyMapping(String contextKey, String propertyKey) {
        propertyMappings.put(contextKey, propertyKey);
    }

    @Override
    public Context getContext(Method method, Object[] args) {
        Map<String, Object> attributes = new HashMap<>();
        
        // 从系统属性获取
        for (Map.Entry<String, String> entry : propertyMappings.entrySet()) {
            String contextKey = entry.getKey();
            String propertyKey = entry.getValue();
            
            String value = System.getProperty(propertyKey);
            if (value != null && !value.trim().isEmpty()) {
                attributes.put(contextKey, value);
                continue;
            }
            
            // 尝试从环境变量获取
            value = System.getenv(propertyKey.replace('.', '_').toUpperCase());
            if (value != null && !value.trim().isEmpty()) {
                attributes.put(contextKey, value);
            }
        }

        // 添加一些默认的系统信息
        if (!attributes.containsKey("env")) {
            String env = System.getProperty("spring.profiles.active", System.getProperty("flexpoint.env", "dev"));
            attributes.put("env", env);
        }

        if (!attributes.containsKey("region")) {
            String region = System.getProperty("flexpoint.region", "default");
            attributes.put("region", region);
        }

        Context context = new Context(attributes);
        log.debug("从系统属性提取上下文: attributes={}", attributes);
        return context;

    }

    @Override
    public String getName() {
        return "systemPropertyProvider";
    }

    @Override
    public int getPriority() {
        return 100;
    }
} 