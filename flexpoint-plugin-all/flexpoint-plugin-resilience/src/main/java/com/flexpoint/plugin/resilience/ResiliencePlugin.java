package com.flexpoint.plugin.resilience;

import com.flexpoint.core.ext.interceptor.InterceptorRegistry;
import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 官方内置：韧性守护插件（超时 + 熔断）。
 *
 * <p>在 {@code start()} 阶段向 {@link InterceptorRegistry} 注册两个拦截器：</p>
 * <ul>
 *   <li>{@link CircuitBreakerInterceptor}（较外层，order={@value CircuitBreakerInterceptor#DEFAULT_ORDER}）；</li>
 *   <li>{@link TimeoutInterceptor}（较内层，order={@value TimeoutInterceptor#DEFAULT_ORDER}）。</li>
 * </ul>
 *
 * <p>两者之间可放置重试拦截器（order=300），形成
 * “熔断 → 重试 → 超时 → 目标方法” 的组合语义。</p>
 *
 * <p>当 {@code timeoutMs > 0} 且未注入线程池时，插件会创建一个守护线程池，
 * 并在 {@code stop()} 阶段负责关闭。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class ResiliencePlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "resilience.guard";

    private static final AtomicLong THREAD_SEQ = new AtomicLong();

    private final CircuitBreakerInterceptor circuitBreakerInterceptor;
    private final TimeoutInterceptor timeoutInterceptor;
    /** 由本插件创建、需自行关闭的线程池（外部注入时为 null）。 */
    private final ExecutorService ownedExecutor;

    private InterceptorRegistry registry;

    /** 使用默认熔断参数（失败率 0.5、最小 20 次、开路 5000ms），并按 timeoutMs 启用超时。 */
    public ResiliencePlugin(long timeoutMs) {
        this(timeoutMs, 0.5d, 20, 5000L);
    }

    public ResiliencePlugin(long timeoutMs, double failureRateThreshold, int minimumCalls, long openMillis) {
        this(timeoutMs, null, failureRateThreshold, minimumCalls, openMillis);
    }

    /**
     * 完整构造：可注入超时执行线程池（其生命周期由调用方管理，插件不会关闭它）。
     *
     * @param timeoutMs            超时时间（&le;0 表示不启用超时）
     * @param executor            超时执行线程池；为 null 且启用超时时由插件自建守护线程池
     * @param failureRateThreshold 熔断失败率阈值 [0,1]
     * @param minimumCalls        触发熔断评估的最小调用数
     * @param openMillis          OPEN 状态持续时间（毫秒）
     */
    public ResiliencePlugin(long timeoutMs, ExecutorService executor,
                            double failureRateThreshold, int minimumCalls, long openMillis) {
        this.circuitBreakerInterceptor =
                new CircuitBreakerInterceptor(failureRateThreshold, minimumCalls, openMillis);
        if (timeoutMs > 0) {
            if (executor != null) {
                this.ownedExecutor = null;
                this.timeoutInterceptor = new TimeoutInterceptor(timeoutMs, executor);
            } else {
                this.ownedExecutor = defaultExecutor();
                this.timeoutInterceptor = new TimeoutInterceptor(timeoutMs, ownedExecutor);
            }
        } else {
            this.ownedExecutor = null;
            this.timeoutInterceptor = new TimeoutInterceptor(0);
        }
    }

    private static ExecutorService defaultExecutor() {
        return Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "flexpoint-resilience-timeout-" + THREAD_SEQ.incrementAndGet());
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public String getId() {
        return PLUGIN_ID;
    }

    @Override
    public void init(PluginContext context) {
        this.registry = context.interceptorRegistry();
        if (this.registry == null) {
            throw new IllegalStateException("ResiliencePlugin requires InterceptorRegistry from PluginContext");
        }
        log.debug("[{}] init: 获取 InterceptorRegistry", PLUGIN_ID);
    }

    @Override
    public void start() {
        registry.register(circuitBreakerInterceptor);
        registry.register(timeoutInterceptor);
        log.debug("[{}] start: 注册熔断(order={})与超时(order={}, timeoutMs={})拦截器",
                PLUGIN_ID, circuitBreakerInterceptor.order(),
                timeoutInterceptor.order(), timeoutInterceptor.getTimeoutMs());
    }

    @Override
    public void stop() {
        if (registry != null) {
            registry.unregister(circuitBreakerInterceptor);
            registry.unregister(timeoutInterceptor);
            log.debug("[{}] stop: 注销熔断与超时拦截器", PLUGIN_ID);
        }
        if (ownedExecutor != null) {
            ownedExecutor.shutdownNow();
        }
    }

    @Override
    public void destroy() {
        this.registry = null;
    }
}
