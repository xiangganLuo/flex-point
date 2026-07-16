package com.flexpoint.plugin.resilience.retry;

import com.flexpoint.core.ext.interceptor.ExtInvocation;
import com.flexpoint.core.ext.interceptor.ExtInvocationInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;

/**
 * 重试拦截器（around 语义）。
 *
 * <p>循环调用 {@link ExtInvocation#proceed()}，捕获异常后按剩余次数与退避时间重试，
 * 直至成功或次数耗尽（最终抛出最后一次异常）。{@code proceed()} 支持多次调用，
 * 每次都会重新执行其后的拦截链与真正的方法调用。</p>
 *
 * <p>约定：{@link Error} 视为不可恢复错误，绝不重试，立即向外透出。</p>
 *
 * <p>顺序：默认 {@value #DEFAULT_ORDER}。数值越小越外层，重试应相对靠内层，
 * 从而“包裹每一次实际调用”；与超时（更内层）、熔断（更外层）配合形成
 * “熔断 → 重试 → 超时 → 目标方法”的常见组合。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class RetryInterceptor implements ExtInvocationInterceptor {

    /** 默认拦截顺序，重试相对靠内层。 */
    public static final int DEFAULT_ORDER = 300;

    /** 默认最大尝试次数（含首次）。 */
    public static final int DEFAULT_MAX_ATTEMPTS = 3;

    /** 默认所有 {@link Exception} 均可重试（但不含 {@link Error}）。 */
    public static final Predicate<Throwable> RETRY_ALL_EXCEPTIONS = t -> t instanceof Exception;

    private final int maxAttempts;
    private final long backoffMs;
    private final Predicate<Throwable> retryOn;
    private final int order;

    /** 使用全部默认值：尝试 3 次、无退避、重试所有 {@link Exception}。 */
    public RetryInterceptor() {
        this(DEFAULT_MAX_ATTEMPTS, 0L, RETRY_ALL_EXCEPTIONS, DEFAULT_ORDER);
    }

    public RetryInterceptor(int maxAttempts, long backoffMs) {
        this(maxAttempts, backoffMs, RETRY_ALL_EXCEPTIONS, DEFAULT_ORDER);
    }

    public RetryInterceptor(int maxAttempts, long backoffMs, Predicate<Throwable> retryOn) {
        this(maxAttempts, backoffMs, retryOn, DEFAULT_ORDER);
    }

    public RetryInterceptor(int maxAttempts, long backoffMs, Predicate<Throwable> retryOn, int order) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1, but was " + maxAttempts);
        }
        if (backoffMs < 0) {
            throw new IllegalArgumentException("backoffMs must be >= 0, but was " + backoffMs);
        }
        this.maxAttempts = maxAttempts;
        this.backoffMs = backoffMs;
        this.retryOn = retryOn != null ? retryOn : RETRY_ALL_EXCEPTIONS;
        this.order = order;
    }

    @Override
    public Object intercept(ExtInvocation invocation) throws Throwable {
        Throwable last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return invocation.proceed();
            } catch (Throwable t) {
                // Error 不可恢复，绝不重试
                if (t instanceof Error) {
                    throw t;
                }
                last = t;
                boolean retryable = retryOn.test(t);
                if (!retryable || attempt >= maxAttempts) {
                    throw t;
                }
                if (log.isDebugEnabled()) {
                    log.debug("[{}] 第{}次调用失败，准备重试(剩余{}次): {}",
                            name(), attempt, maxAttempts - attempt, t.toString());
                }
                if (backoffMs > 0) {
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        // 被中断则放弃重试，抛出最后一次业务异常
                        throw t;
                    }
                }
            }
        }
        // 理论上不可达（循环内已 return 或 throw）
        throw last;
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public String name() {
        return "RetryInterceptor";
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getBackoffMs() {
        return backoffMs;
    }
}
