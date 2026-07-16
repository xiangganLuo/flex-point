package com.flexpoint.core.ext.interceptor;

/**
 * 调用链终端：拦截链全部推进完毕后，真正执行的目标调用。
 * <p>由调用管线（代理）提供，通常封装“事件埋点 + 反射调用”。</p>
 *
 * @author xiangganluo
 */
@FunctionalInterface
public interface ExtInvocationTerminal {

    /**
     * 执行最终调用。
     *
     * @return 调用结果
     * @throws Throwable 目标业务异常或框架异常
     */
    Object invoke() throws Throwable;
}
