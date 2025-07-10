package com.flexpoint.core.context;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 选择器上下文管理器
 * 协调多个上下文提供者，按优先级获取上下文
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class ContextManager {

    private final List<ContextProvider> providers = new CopyOnWriteArrayList<>();

    /**
     * 注册上下文提供者
     *
     * @param provider 上下文提供者
     */
    public void registerProvider(ContextProvider provider) {
        if (provider != null) {
            providers.add(provider);
            // 按优先级排序
            providers.sort(Comparator.comparingInt(ContextProvider::getPriority));
            log.debug("注册上下文提供者: name={}, priority={}", provider.getName(), provider.getPriority());
        }
    }

    /**
     * 注销上下文提供者
     *
     * @param providerName 提供者名称
     */
    public void unregisterProvider(String providerName) {
        providers.removeIf(provider -> provider.getName().equals(providerName));
        log.debug("注销上下文提供者: name={}", providerName);
    }

    /**
     * 获取选择器上下文
     * 按优先级依次调用提供者，返回第一个非空上下文
     *
     * @param method 调用的方法
     * @param args 方法参数
     * @return 选择器上下文
     */
    public Context getContext(Method method, Object[] args) {
        for (ContextProvider provider : providers) {
            try {
                Context context = provider.getContext(method, args);
                if (context != null) {
                    log.debug("使用上下文提供者: name={}, priority={}", provider.getName(), provider.getPriority());
                    return context;
                }
            } catch (Exception e) {
                log.warn("上下文提供者执行异常: name={}", provider.getName(), e);
            }
        }
        
        // 如果没有提供者或所有提供者都返回null，返回空上下文
        log.debug("未找到有效的上下文提供者，返回空上下文");
        return new Context();
    }

    /**
     * 获取所有注册的提供者
     *
     * @return 提供者列表
     */
    public List<ContextProvider> getProviders() {
        return new ArrayList<>(providers);
    }

    /**
     * 清空所有提供者
     */
    public void clear() {
        providers.clear();
        log.debug("清空所有上下文提供者");
    }
} 