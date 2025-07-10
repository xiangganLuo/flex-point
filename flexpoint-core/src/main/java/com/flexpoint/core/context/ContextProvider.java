package com.flexpoint.core.context;

import java.lang.reflect.Method;

/**
 * 选择器上下文提供者接口
 * 支持多种上下文传递方式，实现高内聚低耦合的上下文获取机制
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ContextProvider {

    /**
     * 获取选择器上下文
     * 
     * @param method 调用的方法
     * @param args 方法参数
     * @return 选择器上下文
     */
    Context getContext(Method method, Object[] args);

    /**
     * 获取提供者名称
     * 
     * @return 提供者名称
     */
    String getName();

    /**
     * 获取提供者优先级，数字越小优先级越高
     * 
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }
} 