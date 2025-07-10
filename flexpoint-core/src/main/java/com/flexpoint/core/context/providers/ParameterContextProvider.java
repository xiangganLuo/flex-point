package com.flexpoint.core.context.providers;

import com.flexpoint.common.annotations.FpContextParam;
import com.flexpoint.core.context.ContextProvider;
import com.flexpoint.core.context.Context;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于方法参数的上下文提供者
 * 支持从方法参数中提取上下文信息，适用于参数驱动的场景
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class ParameterContextProvider implements ContextProvider {

    @Override
    public Context getContext(Method method, Object[] args) {
        if (method == null || args == null) {
            return null;
        }

        Map<String, Object> attributes = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        
        for (int i = 0; i < parameters.length && i < args.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];
            // 处理 @ContextParam 注解
            FpContextParam fpContextParam = parameter.getAnnotation(FpContextParam.class);
            if (fpContextParam != null) {
                String contextKey = fpContextParam.value().isEmpty() ? parameter.getName() : fpContextParam.value();
                if (arg != null) {
                    attributes.put(contextKey, arg);
                    log.debug("从 @ContextParam 注解提取 {}: {}", contextKey, arg);
                } else if (fpContextParam.required()) {
                    log.warn("必需的 @ContextParam 参数为 null: method={}, parameter={}, key={}", method.getName(), parameter.getName(), contextKey);
                }
            }
        }

        if (!attributes.isEmpty()) {
            Context context = new Context(attributes);
            log.debug("从方法参数提取上下文: method={}, attributes={}", method.getName(), attributes);
            return context;
        }
        
        return null;
    }

    @Override
    public String getName() {
        return "parameterProvider";
    }

    @Override
    public int getPriority() {
        return 50;
    }
} 