package com.flexpoint.core.context.providers;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.flexpoint.core.context.Context;
import com.flexpoint.core.context.ContextProvider;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于ThreadLocal的上下文提供者
 * 支持线程级别的上下文传递，适用于Web请求、异步任务等场景
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class ThreadLocalContextProvider implements ContextProvider {

    private static final ThreadLocal<Context> CONTEXT_HOLDER = new TransmittableThreadLocal<>();
    private static final Map<String, ThreadLocal<Object>> ATTRIBUTE_HOLDERS = new ConcurrentHashMap<>();

    @Override
    public Context getContext(Method method, Object[] args) {
        Context context = CONTEXT_HOLDER.get();
        if (context != null) {
            log.debug("从ThreadLocal获取上下文: attributes={}", context.getAttributes());
            return context;
        }
        return null;
    }

    @Override
    public String getName() {
        return "threadLocalProvider";
    }

    @Override
    public int getPriority() {
        return 10;
    }

    /**
     * 设置当前线程的上下文
     *
     * @param context 选择器上下文
     */
    public static void setContext(Context context) {
        CONTEXT_HOLDER.set(context);
        log.debug("设置ThreadLocal上下文: attributes={}", context != null ? context.getAttributes() : null);
    }

    /**
     * 获取当前线程的上下文
     *
     * @return 选择器上下文
     */
    public static Context getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前线程的上下文
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
        log.debug("清除ThreadLocal上下文");
    }

    /**
     * 设置当前线程的特定属性
     *
     * @param key 属性键
     * @param value 属性值
     */
    public static void setAttribute(String key, Object value) {
        ThreadLocal<Object> holder = ATTRIBUTE_HOLDERS.computeIfAbsent(key, k -> new ThreadLocal<>());
        holder.set(value);
        log.debug("设置ThreadLocal属性: key={}, value={}", key, value);
    }

    /**
     * 获取当前线程的特定属性
     *
     * @param key 属性键
     * @return 属性值
     */
    public static Object getAttribute(String key) {
        ThreadLocal<Object> holder = ATTRIBUTE_HOLDERS.get(key);
        return holder != null ? holder.get() : null;
    }

    /**
     * 清除当前线程的特定属性
     *
     * @param key 属性键
     */
    public static void clearAttribute(String key) {
        ThreadLocal<Object> holder = ATTRIBUTE_HOLDERS.get(key);
        if (holder != null) {
            holder.remove();
            log.debug("清除ThreadLocal属性: key={}", key);
        }
    }

    /**
     * 清除当前线程的所有属性
     */
    public static void clearAllAttributes() {
        ATTRIBUTE_HOLDERS.values().forEach(ThreadLocal::remove);
        log.debug("清除所有ThreadLocal属性");
    }
} 