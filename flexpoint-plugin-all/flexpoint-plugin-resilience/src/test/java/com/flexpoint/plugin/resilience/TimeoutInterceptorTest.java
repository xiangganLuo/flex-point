package com.flexpoint.plugin.resilience;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.interceptor.ExtInvocation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
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

    /** 可脚本化的 ExtInvocation：可延时/阻塞、可抛异常、可返回值，并记录 proceed 次数。 */
    private static final class ScriptedInvocation implements ExtInvocation {
        private final AtomicInteger proceedCount = new AtomicInteger(0);
        private final long sleepMs;
        private final Throwable throwIt;
        private final Object value;
        private final CountDownLatch gate;

        ScriptedInvocation(long sleepMs, Throwable throwIt, Object value) {
            this(sleepMs, throwIt, value, null);
        }

        /**
         * 阻塞在 {@code gate} 上直到被 {@code countDown()} 释放或线程被中断。
         * 用于「超时」用例：任务是否越界与机器时钟无关，从而消除计时抖动（flaky）。
         */
        ScriptedInvocation(CountDownLatch gate, Object value) {
            this(0L, null, value, gate);
        }

        private ScriptedInvocation(long sleepMs, Throwable throwIt, Object value, CountDownLatch gate) {
            this.sleepMs = sleepMs;
            this.throwIt = throwIt;
            this.value = value;
            this.gate = gate;
        }

        @Override public ExtAbility getTarget() { return null; }
        @Override public Method getMethod() { return null; }
        @Override public Object[] getArgs() { return new Object[0]; }

        @Override
        public Object proceed() throws Throwable {
            proceedCount.incrementAndGet();
            if (gate != null) {
                // 阻塞直至测试显式释放；future.cancel(true) 会中断此处使任务结束
                gate.await();
            } else if (sleepMs > 0) {
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
        // 任务阻塞在 gate 上（而非固定 sleep）：无论机器快慢或主线程 GC/调度停顿，
        // future.get(timeout) 必定超时 —— 消除「墙钟竞速」导致的偶发失败。
        CountDownLatch gate = new CountDownLatch(1);
        ScriptedInvocation inv = new ScriptedInvocation(gate, "late");
        TimeoutInterceptor interceptor = new TimeoutInterceptor(100L);
        try {
            assertThrows(CallTimeoutException.class, () -> interceptor.intercept(inv));
        } finally {
            gate.countDown();       // 释放被阻塞的任务，避免线程悬挂
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
