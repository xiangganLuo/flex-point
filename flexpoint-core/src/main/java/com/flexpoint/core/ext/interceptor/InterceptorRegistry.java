package com.flexpoint.core.ext.interceptor;

import java.util.List;

/**
 * 调用拦截器注册表。
 *
 * <p>行为增强类插件通过 {@code PluginContext.interceptorRegistry()} 注册拦截器；
 * 调用管线按 {@link ExtInvocationInterceptor#order()} 升序（越小越外层）编排。</p>
 *
 * @author xiangganluo
 */
public interface InterceptorRegistry {

    /** 注册一个拦截器 */
    void register(ExtInvocationInterceptor interceptor);

    /** 注销一个拦截器 */
    void unregister(ExtInvocationInterceptor interceptor);

    /** 获取按 order 升序排序的拦截器快照 */
    List<ExtInvocationInterceptor> getInterceptors();
}
