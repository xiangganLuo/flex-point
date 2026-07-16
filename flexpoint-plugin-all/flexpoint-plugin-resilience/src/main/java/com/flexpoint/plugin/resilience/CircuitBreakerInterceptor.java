package com.flexpoint.plugin.resilience;

import com.flexpoint.core.ext.interceptor.ExtInvocation;
import com.flexpoint.core.ext.interceptor.ExtInvocationInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/**
 * 熔断拦截器（around 语义），实现经典三态状态机：
 *
 * <ul>
 *   <li><b>CLOSED</b>：正常放行；以 {@code minimumCalls} 为窗口累计成功/失败，
 *       达到窗口且失败率 &ge; {@code failureRateThreshold} 时切换到 OPEN。</li>
 *   <li><b>OPEN</b>：快速失败（抛 {@link CircuitOpenException}，不推进 {@code proceed()}）；
 *       经过 {@code openMillis} 后进入 HALF_OPEN 放行一次探测。</li>
 *   <li><b>HALF_OPEN</b>：仅放行一个探测请求，成功则回到 CLOSED，失败则重新 OPEN。</li>
 * </ul>
 *
 * <p>使用 {@code AtomicXxx} 保证并发安全：快速失败判定走无锁的原子读/CAS，
 * 结果记录（窗口累计与状态迁移）在轻量锁内完成以保证复合操作的原子性。</p>
 *
 * <p>顺序：默认 {@value #DEFAULT_ORDER}，相对靠外层，从而在重试/超时之外“看到”整体调用结果，
 * OPEN 时可在重试发生前直接快速失败。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class CircuitBreakerInterceptor implements ExtInvocationInterceptor {

    /** 默认拦截顺序，熔断相对靠外层。 */
    public static final int DEFAULT_ORDER = 100;

    /** 熔断状态。 */
    public enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    private final double failureRateThreshold;
    private final int minimumCalls;
    private final long openMillis;
    private final int order;
    private final LongSupplier clock;

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicLong openedAt = new AtomicLong(0L);
    private final AtomicInteger windowCalls = new AtomicInteger(0);
    private final AtomicInteger windowFailures = new AtomicInteger(0);
    /** HALF_OPEN 期间是否已有探测在途，保证只放行一个探测。 */
    private final AtomicBoolean probeInFlight = new AtomicBoolean(false);

    private final Object transitionLock = new Object();

    public CircuitBreakerInterceptor(double failureRateThreshold, int minimumCalls, long openMillis) {
        this(failureRateThreshold, minimumCalls, openMillis, DEFAULT_ORDER, System::currentTimeMillis);
    }

    /** 供测试注入确定性时钟。 */
    CircuitBreakerInterceptor(double failureRateThreshold, int minimumCalls, long openMillis,
                              int order, LongSupplier clock) {
        if (failureRateThreshold < 0 || failureRateThreshold > 1) {
            throw new IllegalArgumentException("failureRateThreshold must be in [0,1], but was " + failureRateThreshold);
        }
        if (minimumCalls < 1) {
            throw new IllegalArgumentException("minimumCalls must be >= 1, but was " + minimumCalls);
        }
        if (openMillis < 0) {
            throw new IllegalArgumentException("openMillis must be >= 0, but was " + openMillis);
        }
        this.failureRateThreshold = failureRateThreshold;
        this.minimumCalls = minimumCalls;
        this.openMillis = openMillis;
        this.order = order;
        this.clock = clock;
    }

    @Override
    public Object intercept(ExtInvocation invocation) throws Throwable {
        if (!tryAcquirePermission()) {
            throw new CircuitOpenException("circuit breaker is OPEN, call rejected");
        }
        try {
            Object result = invocation.proceed();
            onSuccess();
            return result;
        } catch (Throwable t) {
            onFailure();
            throw t;
        }
    }

    /** 无锁的准入判定（快速失败在此完成）。 */
    private boolean tryAcquirePermission() {
        State s = state.get();
        switch (s) {
            case CLOSED:
                return true;
            case OPEN:
                if (clock.getAsLong() - openedAt.get() >= openMillis) {
                    if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                        // 迁移成功，尝试抢占唯一探测名额
                        return probeInFlight.compareAndSet(false, true);
                    }
                    // 已被其它线程迁移，尝试抢占探测名额
                    return state.get() == State.HALF_OPEN && probeInFlight.compareAndSet(false, true);
                }
                return false;
            case HALF_OPEN:
                return probeInFlight.compareAndSet(false, true);
            default:
                return false;
        }
    }

    private void onSuccess() {
        synchronized (transitionLock) {
            if (state.get() == State.HALF_OPEN) {
                // 探测成功 → 恢复
                close();
            } else if (state.get() == State.CLOSED) {
                record(false);
            }
        }
    }

    private void onFailure() {
        synchronized (transitionLock) {
            if (state.get() == State.HALF_OPEN) {
                // 探测失败 → 重新打开
                open();
            } else if (state.get() == State.CLOSED) {
                record(true);
            }
        }
    }

    /** 记录一次 CLOSED 状态下的调用结果并按窗口评估是否熔断。调用方需持有 transitionLock。 */
    private void record(boolean failure) {
        int calls = windowCalls.incrementAndGet();
        int failures = failure ? windowFailures.incrementAndGet() : windowFailures.get();
        if (calls >= minimumCalls) {
            double rate = (double) failures / calls;
            if (rate >= failureRateThreshold) {
                open();
            } else {
                // 滚动窗口：达到窗口且未触发熔断，重置累计
                windowCalls.set(0);
                windowFailures.set(0);
            }
        }
    }

    private void open() {
        state.set(State.OPEN);
        openedAt.set(clock.getAsLong());
        windowCalls.set(0);
        windowFailures.set(0);
        probeInFlight.set(false);
        log.debug("[CircuitBreakerInterceptor] 状态迁移 -> OPEN");
    }

    private void close() {
        state.set(State.CLOSED);
        windowCalls.set(0);
        windowFailures.set(0);
        probeInFlight.set(false);
        log.debug("[CircuitBreakerInterceptor] 状态迁移 -> CLOSED");
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public String name() {
        return "CircuitBreakerInterceptor";
    }

    /** 当前状态（用于诊断/测试）。 */
    public State getState() {
        return state.get();
    }
}
