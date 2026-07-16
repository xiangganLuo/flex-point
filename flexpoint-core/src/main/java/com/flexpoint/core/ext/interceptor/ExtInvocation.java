package com.flexpoint.core.ext.interceptor;

import com.flexpoint.core.ext.ExtAbility;

import java.lang.reflect.Method;

/**
 * 一次扩展点方法调用的可推进上下文（around 拦截链节点）。
 *
 * <p>拦截器通过 {@link #proceed()} 推进到链上的下一个拦截器，直至最终真正的方法调用。
 * {@code proceed()} 可被同一拦截器多次调用（每次都会重新执行其后的链与终端），
 * 以支持重试等语义。</p>
 *
 * @author xiangganluo
 */
public interface ExtInvocation {

    /** 被调用的原始扩展点实例。 */
    ExtAbility getTarget();

    /** 被调用的方法。 */
    Method getMethod();

    /** 调用参数（可能为 null）。 */
    Object[] getArgs();

    /**
     * 推进执行链上其后的拦截器与最终方法调用，返回其结果。
     *
     * @return 调用结果
     * @throws Throwable 链路中抛出的任意异常
     */
    Object proceed() throws Throwable;
}
