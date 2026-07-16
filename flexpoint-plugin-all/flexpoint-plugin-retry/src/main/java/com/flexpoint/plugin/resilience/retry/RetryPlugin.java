package com.flexpoint.plugin.resilience.retry;

import com.flexpoint.core.ext.interceptor.InterceptorRegistry;
import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;

/**
 * 官方内置：重试插件。
 *
 * <p>在 {@code start()} 阶段向 {@link InterceptorRegistry} 注册 {@link RetryInterceptor}，
 * 为扩展点方法调用提供失败自动重试能力；{@code stop()} 阶段注销。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class RetryPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "resilience.retry";

    private final RetryInterceptor interceptor;
    private InterceptorRegistry registry;

    /** 使用默认重试策略（3 次、无退避、重试所有异常）。 */
    public RetryPlugin() {
        this(new RetryInterceptor());
    }

    public RetryPlugin(int maxAttempts, long backoffMs) {
        this(new RetryInterceptor(maxAttempts, backoffMs));
    }

    public RetryPlugin(int maxAttempts, long backoffMs, Predicate<Throwable> retryOn) {
        this(new RetryInterceptor(maxAttempts, backoffMs, retryOn));
    }

    public RetryPlugin(RetryInterceptor interceptor) {
        if (interceptor == null) {
            throw new IllegalArgumentException("RetryInterceptor must not be null");
        }
        this.interceptor = interceptor;
    }

    @Override
    public String getId() {
        return PLUGIN_ID;
    }

    @Override
    public void init(PluginContext context) {
        this.registry = context.interceptorRegistry();
        if (this.registry == null) {
            throw new IllegalStateException("RetryPlugin requires InterceptorRegistry from PluginContext");
        }
        log.debug("[{}] init: 获取 InterceptorRegistry", PLUGIN_ID);
    }

    @Override
    public void start() {
        registry.register(interceptor);
        log.debug("[{}] start: 注册重试拦截器 order={} maxAttempts={} backoffMs={}",
                PLUGIN_ID, interceptor.order(), interceptor.getMaxAttempts(), interceptor.getBackoffMs());
    }

    @Override
    public void stop() {
        if (registry != null) {
            registry.unregister(interceptor);
            log.debug("[{}] stop: 注销重试拦截器", PLUGIN_ID);
        }
    }

    @Override
    public void destroy() {
        this.registry = null;
    }
}
