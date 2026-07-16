package com.flexpoint.plugin.resilience.retry;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.interceptor.ExtInvocation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link RetryInterceptor} 单元测试。
 *
 * @author xiangganluo
 */
class RetryInterceptorTest {

    /** 可脚本化的 ExtInvocation 测试替身：记录 proceed 调用次数，并按脚本抛异常/返回值。 */
    private static final class ScriptedInvocation implements ExtInvocation {
        private final AtomicInteger proceedCount = new AtomicInteger(0);
        /** 前 failTimes 次抛出 failWith，其后返回 successValue。 */
        private final int failTimes;
        private final Throwable failWith;
        private final Object successValue;

        ScriptedInvocation(int failTimes, Throwable failWith, Object successValue) {
            this.failTimes = failTimes;
            this.failWith = failWith;
            this.successValue = successValue;
        }

        @Override
        public ExtAbility getTarget() {
            return null;
        }

        @Override
        public Method getMethod() {
            return null;
        }

        @Override
        public Object[] getArgs() {
            return new Object[0];
        }

        @Override
        public Object proceed() throws Throwable {
            int n = proceedCount.incrementAndGet();
            if (n <= failTimes) {
                throw failWith;
            }
            return successValue;
        }

        int proceedCount() {
            return proceedCount.get();
        }
    }

    @Test
    void succeedsOnThirdAttemptAfterTwoFailures() throws Throwable {
        ScriptedInvocation inv = new ScriptedInvocation(2, new IllegalStateException("boom"), "OK");
        RetryInterceptor interceptor = new RetryInterceptor(3, 0L);

        Object result = interceptor.intercept(inv);

        assertEquals("OK", result);
        assertEquals(3, inv.proceedCount(), "首次 + 两次重试 = 共 3 次 proceed");
    }

    @Test
    void throwsLastExceptionWhenAttemptsExhausted() {
        RuntimeException failure = new RuntimeException("always fail");
        ScriptedInvocation inv = new ScriptedInvocation(Integer.MAX_VALUE, failure, "never");
        RetryInterceptor interceptor = new RetryInterceptor(3, 0L);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> interceptor.intercept(inv));

        assertSame(failure, thrown, "最终应抛出最后一次的业务异常");
        assertEquals(3, inv.proceedCount(), "达到 maxAttempts 后停止重试");
    }

    @Test
    void doesNotRetryWhenRetryPredicateRejects() {
        IllegalArgumentException nonRetryable = new IllegalArgumentException("do not retry");
        ScriptedInvocation inv = new ScriptedInvocation(Integer.MAX_VALUE, nonRetryable, "never");
        // 仅对 IllegalStateException 重试；IllegalArgumentException 直接透出
        Predicate<Throwable> retryOn = t -> t instanceof IllegalStateException;
        RetryInterceptor interceptor = new RetryInterceptor(3, 0L, retryOn);

        assertThrows(IllegalArgumentException.class, () -> interceptor.intercept(inv));
        assertEquals(1, inv.proceedCount(), "不可重试异常仅调用一次");
    }

    @Test
    void neverRetriesError() {
        OutOfMemoryError error = new OutOfMemoryError("fatal");
        ScriptedInvocation inv = new ScriptedInvocation(Integer.MAX_VALUE, error, "never");
        RetryInterceptor interceptor = new RetryInterceptor(5, 0L);

        assertThrows(OutOfMemoryError.class, () -> interceptor.intercept(inv));
        assertEquals(1, inv.proceedCount(), "Error 不重试，仅调用一次");
    }

    @Test
    void singleAttemptWhenMaxAttemptsIsOne() {
        RuntimeException failure = new RuntimeException("boom");
        ScriptedInvocation inv = new ScriptedInvocation(Integer.MAX_VALUE, failure, "never");
        RetryInterceptor interceptor = new RetryInterceptor(1, 0L);

        assertThrows(RuntimeException.class, () -> interceptor.intercept(inv));
        assertEquals(1, inv.proceedCount(), "maxAttempts=1 表示不重试");
    }

    @Test
    void defaultOrderIsThreeHundred() {
        assertEquals(300, new RetryInterceptor().order());
    }
}
