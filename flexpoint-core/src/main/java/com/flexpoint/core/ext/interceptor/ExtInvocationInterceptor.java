package com.flexpoint.core.ext.interceptor;

/**
 * 扩展点调用拦截器（around 语义）。
 *
 * <p>可在扩展点方法调用前后插入行为（重试、超时、熔断、限流、缓存等），
 * 通过 {@link ExtInvocation#proceed()} 决定是否/何时/几次推进实际调用。</p>
 *
 * <p>由行为增强类插件在 {@code start()} 阶段通过
 * {@code PluginContext.interceptorRegistry().register(...)} 注册。</p>
 *
 * @author xiangganluo
 */
public interface ExtInvocationInterceptor {

    /**
     * 拦截一次调用。
     *
     * @param invocation 可推进的调用上下文
     * @return 调用结果
     * @throws Throwable 处理或被包装的异常
     */
    Object intercept(ExtInvocation invocation) throws Throwable;

    /**
     * 拦截顺序，数值越小越靠外（越先执行）。默认 0。
     */
    default int order() {
        return 0;
    }

    /** 拦截器名称，用于诊断。 */
    default String name() {
        return getClass().getSimpleName();
    }
}
