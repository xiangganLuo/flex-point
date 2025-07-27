package com.flexpoint.core.monitor;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 扩展点指标实现
 */
public class ExtMetricsImpl implements ExtMetrics {
    private final AtomicLong totalInvocations = new AtomicLong(0);
    private final AtomicLong successInvocations = new AtomicLong(0);
    private final AtomicLong failureInvocations = new AtomicLong(0);
    private final AtomicLong exceptionCount = new AtomicLong(0);
    private final AtomicLong totalDuration = new AtomicLong(0);
    private final AtomicLong maxResponseTime = new AtomicLong(0);
    private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicReference<Long> lastInvocationTime = new AtomicReference<>(0L);
    private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

    public void recordInvocation(long duration, boolean success) {
        totalInvocations.incrementAndGet();
        if (success) {
            successInvocations.incrementAndGet();
        } else {
            failureInvocations.incrementAndGet();
        }

        totalDuration.addAndGet(duration);

        // 更新最大响应时间
        maxResponseTime.updateAndGet(current -> Math.max(current, duration));

        // 更新最小响应时间
        minResponseTime.updateAndGet(current -> Math.min(current, duration));

        // 更新最后调用时间
        lastInvocationTime.set(System.currentTimeMillis());
    }

    public void recordException() {
        exceptionCount.incrementAndGet();
    }

    @Override
    public long getTotalInvocations() {
        return totalInvocations.get();
    }

    @Override
    public long getSuccessInvocations() {
        return successInvocations.get();
    }

    @Override
    public long getFailureInvocations() {
        return failureInvocations.get();
    }

    @Override
    public double getSuccessRate() {
        long total = totalInvocations.get();
        return total > 0 ? (double) successInvocations.get() / total : 0.0;
    }

    @Override
    public double getAverageResponseTime() {
        long total = totalInvocations.get();
        return total > 0 ? (double) totalDuration.get() / total : 0.0;
    }

    @Override
    public long getMaxResponseTime() {
        return maxResponseTime.get();
    }

    @Override
    public long getMinResponseTime() {
        long min = minResponseTime.get();
        return min == Long.MAX_VALUE ? 0 : min;
    }

    @Override
    public long getExceptionCount() {
        return exceptionCount.get();
    }

    @Override
    public long getLastInvocationTime() {
        return lastInvocationTime.get();
    }

    @Override
    public long getP95ResponseTime() {
        // 简化实现，实际应该使用滑动窗口或采样
        return (long) (getAverageResponseTime() * 1.5);
    }

    @Override
    public long getP99ResponseTime() {
        // 简化实现，实际应该使用滑动窗口或采样
        return (long) (getAverageResponseTime() * 2.0);
    }

    @Override
    public double getQPS() {
        long total = totalInvocations.get();
        long elapsed = System.currentTimeMillis() - startTime.get();
        return elapsed > 0 ? (double) total / (elapsed / 1000.0) : 0.0;
    }
}