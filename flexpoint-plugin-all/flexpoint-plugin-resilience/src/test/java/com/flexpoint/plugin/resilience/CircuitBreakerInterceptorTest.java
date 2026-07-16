package com.flexpoint.plugin.resilience;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.interceptor.ExtInvocation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link CircuitBreakerInterceptor} 单元测试。
 *
 * <p>通过注入确定性时钟消除时间相关的 flaky，无需 {@code Thread.sleep}。</p>
 *
 * @author xiangganluo
 */
class CircuitBreakerInterceptorTest {

    /** 可控 ExtInvocation：按 {@link #fail} 决定抛异常或返回值，并记录 proceed 次数。 */
    private static final class ControllableInvocation implements ExtInvocation {
        private final AtomicInteger proceedCount = new AtomicInteger(0);
        private volatile boolean fail;
        private final RuntimeException error = new RuntimeException("call failed");

        ControllableInvocation(boolean fail) {
            this.fail = fail;
        }

        void setFail(boolean fail) {
            this.fail = fail;
        }

        @Override public ExtAbility getTarget() { return null; }
        @Override public Method getMethod() { return null; }
        @Override public Object[] getArgs() { return new Object[0]; }

        @Override
        public Object proceed() {
            proceedCount.incrementAndGet();
            if (fail) {
                throw error;
            }
            return "OK";
        }

        int proceedCount() { return proceedCount.get(); }
    }

    private static final double THRESHOLD = 0.5d;
    private static final int MIN_CALLS = 4;
    private static final long OPEN_MILLIS = 1000L;

    private CircuitBreakerInterceptor newBreaker(AtomicLong clock) {
        return new CircuitBreakerInterceptor(THRESHOLD, MIN_CALLS, OPEN_MILLIS,
                CircuitBreakerInterceptor.DEFAULT_ORDER, clock::get);
    }

    private void driveFailures(CircuitBreakerInterceptor breaker, ControllableInvocation inv, int times) {
        for (int i = 0; i < times; i++) {
            assertThrows(RuntimeException.class, () -> breaker.intercept(inv));
        }
    }

    @Test
    void opensAfterFailureRateExceededAndFastFails() {
        AtomicLong clock = new AtomicLong(0L);
        CircuitBreakerInterceptor breaker = newBreaker(clock);
        ControllableInvocation inv = new ControllableInvocation(true);

        // MIN_CALLS 次连续失败 → 失败率 1.0 ≥ 0.5 → OPEN
        driveFailures(breaker, inv, MIN_CALLS);
        assertEquals(CircuitBreakerInterceptor.State.OPEN, breaker.getState());
        assertEquals(MIN_CALLS, inv.proceedCount());

        // OPEN 期间快速失败：抛 CircuitOpenException 且不再推进 proceed
        assertThrows(CircuitOpenException.class, () -> breaker.intercept(inv));
        assertEquals(MIN_CALLS, inv.proceedCount(), "OPEN 状态下 proceed 不应再被调用");
    }

    @Test
    void halfOpenProbeSuccessClosesCircuit() throws Throwable {
        AtomicLong clock = new AtomicLong(0L);
        CircuitBreakerInterceptor breaker = newBreaker(clock);
        ControllableInvocation inv = new ControllableInvocation(true);

        driveFailures(breaker, inv, MIN_CALLS);
        assertEquals(CircuitBreakerInterceptor.State.OPEN, breaker.getState());

        // 经过 openMillis → 允许一次 HALF_OPEN 探测；令其成功
        clock.set(OPEN_MILLIS);
        inv.setFail(false);
        Object result = breaker.intercept(inv);

        assertEquals("OK", result);
        assertEquals(CircuitBreakerInterceptor.State.CLOSED, breaker.getState(), "探测成功应回到 CLOSED");
        assertEquals(MIN_CALLS + 1, inv.proceedCount(), "探测放行了一次 proceed");
    }

    @Test
    void halfOpenProbeFailureReopensCircuit() {
        AtomicLong clock = new AtomicLong(0L);
        CircuitBreakerInterceptor breaker = newBreaker(clock);
        ControllableInvocation inv = new ControllableInvocation(true);

        driveFailures(breaker, inv, MIN_CALLS);
        assertEquals(CircuitBreakerInterceptor.State.OPEN, breaker.getState());

        // 经过 openMillis → HALF_OPEN 探测，但探测仍失败 → 重新 OPEN
        clock.set(OPEN_MILLIS);
        assertThrows(RuntimeException.class, () -> breaker.intercept(inv));
        assertEquals(CircuitBreakerInterceptor.State.OPEN, breaker.getState(), "探测失败应重新 OPEN");

        // 未过新的 openMillis（时钟未推进）→ 再次快速失败
        int before = inv.proceedCount();
        assertThrows(CircuitOpenException.class, () -> breaker.intercept(inv));
        assertEquals(before, inv.proceedCount(), "重新 OPEN 后应快速失败");
    }

    @Test
    void staysClosedBelowFailureRateThreshold() {
        AtomicLong clock = new AtomicLong(0L);
        CircuitBreakerInterceptor breaker = newBreaker(clock);
        // 2 失败 + 2 成功 = 失败率 0.5 恰达阈值 → 会 OPEN；改为 1 失败 + 3 成功验证保持 CLOSED
        ControllableInvocation failing = new ControllableInvocation(true);
        ControllableInvocation ok = new ControllableInvocation(false);

        assertThrows(RuntimeException.class, () -> breaker.intercept(failing)); // 1 fail
        for (int i = 0; i < 3; i++) {
            try {
                breaker.intercept(ok);
            } catch (Throwable t) {
                throw new AssertionError(t);
            }
        }
        // calls=4, failures=1, rate=0.25 < 0.5 → 保持 CLOSED
        assertEquals(CircuitBreakerInterceptor.State.CLOSED, breaker.getState());
    }

    @Test
    void defaultOrderIsOneHundred() {
        assertEquals(100, new CircuitBreakerInterceptor(0.5d, 20, 5000L).order());
    }
}
