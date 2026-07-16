package com.flexpoint.plugin.resilience;

import com.flexpoint.core.ext.interceptor.ExtInvocation;
import com.flexpoint.core.ext.interceptor.ExtInvocationInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 超时拦截器（around 语义）。
 *
 * <p>将 {@link ExtInvocation#proceed()} 提交到 {@link ExecutorService} 异步执行，
 * 并以 {@code future.get(timeoutMs)} 等待结果；超时则 {@code cancel} 任务并抛出
 * {@link CallTimeoutException}。业务异常经 {@link ExecutionException} 解包后透出。</p>
 *
 * <p>{@code timeoutMs <= 0} 表示不启用超时，此时直接同步 {@code proceed()}。</p>
 *
 * <p>顺序：默认 {@value #DEFAULT_ORDER}，相对靠内层，从而“对每一次实际尝试生效”
 * （位于重试之内）。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class TimeoutInterceptor implements ExtInvocationInterceptor {

    /** 默认拦截顺序，超时相对靠内层（对每次尝试生效）。 */
    public static final int DEFAULT_ORDER = 400;

    private static final AtomicLong THREAD_SEQ = new AtomicLong();

    private final long timeoutMs;
    private final ExecutorService executor;
    private final boolean ownsExecutor;
    private final int order;

    /**
     * 使用内建的守护线程池（由本拦截器负责关闭）。
     * 当 {@code timeoutMs <= 0} 时不创建线程池。
     */
    public TimeoutInterceptor(long timeoutMs) {
        this(timeoutMs, timeoutMs > 0 ? defaultExecutor() : null, timeoutMs > 0, DEFAULT_ORDER);
    }

    /**
     * 使用外部注入的线程池（生命周期由调用方管理，本拦截器不会关闭它）。
     */
    public TimeoutInterceptor(long timeoutMs, ExecutorService executor) {
        this(timeoutMs, executor, false, DEFAULT_ORDER);
    }

    private TimeoutInterceptor(long timeoutMs, ExecutorService executor, boolean ownsExecutor, int order) {
        if (timeoutMs > 0 && executor == null) {
            throw new IllegalArgumentException("executor must not be null when timeoutMs > 0");
        }
        this.timeoutMs = timeoutMs;
        this.executor = executor;
        this.ownsExecutor = ownsExecutor;
        this.order = order;
    }

    private static ExecutorService defaultExecutor() {
        return Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "flexpoint-timeout-" + THREAD_SEQ.incrementAndGet());
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public Object intercept(ExtInvocation invocation) throws Throwable {
        if (timeoutMs <= 0) {
            return invocation.proceed();
        }
        Callable<Object> task = () -> {
            try {
                return invocation.proceed();
            } catch (Exception e) {
                throw e;
            } catch (Throwable t) {
                // Callable 只能抛 Exception，包装 Error/Throwable 以便解包后原样透出
                throw new WrappedThrowable(t);
            }
        };
        Future<Object> future = executor.submit(task);
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException te) {
            future.cancel(true);
            throw new CallTimeoutException("call timed out after " + timeoutMs + "ms", te);
        } catch (ExecutionException ee) {
            throw unwrap(ee);
        } catch (InterruptedException ie) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw ie;
        }
    }

    private static Throwable unwrap(ExecutionException ee) {
        Throwable cause = ee.getCause();
        if (cause instanceof WrappedThrowable) {
            return cause.getCause();
        }
        return cause != null ? cause : ee;
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public String name() {
        return "TimeoutInterceptor";
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    /** 关闭内建线程池（仅当由本拦截器创建时生效）。 */
    public void shutdown() {
        if (ownsExecutor && executor != null) {
            executor.shutdownNow();
        }
    }

    /** 内部包装类型，用于跨线程搬运非 {@link Exception} 的 {@link Throwable}。 */
    private static final class WrappedThrowable extends Exception {
        private static final long serialVersionUID = 1L;

        WrappedThrowable(Throwable cause) {
            super(cause);
        }
    }
}
