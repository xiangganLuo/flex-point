package com.flexpoint.plugin.resilience;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.interceptor.ExtInvocation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link TimeoutInterceptor} 单元测试。
 *
 * @author xiangganluo
 */
class TimeoutInterceptorTest {

    /** 可脚本化的 ExtInvocation：可延时、可抛异常、可返回值，并记录 proceed 次数。 */
    private static final class ScriptedInvocation implements ExtInvocation {
        private final AtomicInteger proceedCount = new AtomicInteger(0);
        private final long sleepMs;
        private final Throwable throwIt;
        private final Object value;

        ScriptedInvocation(long sleepMs, Throwable throwIt, Object value) {
            this.sleepMs = sleepMs;
            this.throwIt = throwIt;
            this.value = value;
        }

        @Override public ExtAbility getTarget() { return null; }
        @Override public Method getMethod() { return null; }
        @Override public Object[] getArgs() { return new Object[0]; }

        @Override
        public Object proceed() throws Throwable {
            proceedCount.incrementAndGet();
            if (sleepMs > 0) {
                Thread.sleep(sleepMs);
            }
            if (throwIt != null) {
                throw throwIt;
            }
            return value;
        }

        int proceedCount() { return proceedCount.get(); }
    }

    @Test
    void throwsCallTimeoutWhenProceedExceedsTimeout() {
        ScriptedInvocation inv = new ScriptedInvocation(500L, null, "late");
        TimeoutInterceptor interceptor = new TimeoutInterceptor(100L);
        try {
            assertThrows(CallTimeoutException.class, () -> interceptor.intercept(inv));
        } finally {
            interceptor.shutdown();
        }
    }

    @Test
    void returnsValueWhenWithinTimeout() throws Throwable {
        ScriptedInvocation inv = new ScriptedInvocation(0L, null, "OK");
        TimeoutInterceptor interceptor = new TimeoutInterceptor(1000L);
        try {
            assertEquals("OK", interceptor.intercept(inv));
        } finally {
            interceptor.shutdown();
        }
    }

    @Test
    void propagatesBusinessExceptionUnwrapped() {
        IllegalStateException business = new IllegalStateException("business boom");
        ScriptedInvocation inv = new ScriptedInvocation(0L, business, null);
        TimeoutInterceptor interceptor = new TimeoutInterceptor(1000L);
        try {
            IllegalStateException thrown =
                    assertThrows(IllegalStateException.class, () -> interceptor.intercept(inv));
            assertSame(business, thrown, "业务异常应经 ExecutionException 解包后原样透出");
        } finally {
            interceptor.shutdown();
        }
    }

    @Test
    void runsSynchronouslyWhenTimeoutDisabled() throws Throwable {
        ScriptedInvocation inv = new ScriptedInvocation(0L, null, "sync");
        // timeoutMs <= 0 → 直接同步 proceed，不创建线程池
        TimeoutInterceptor interceptor = new TimeoutInterceptor(0L);
        assertEquals("sync", interceptor.intercept(inv));
        assertEquals(1, inv.proceedCount());
    }

    @Test
    void defaultOrderIsFourHundred() {
        assertEquals(400, new TimeoutInterceptor(0L).order());
    }
}
