package com.flexpoint.core.monitor;

/**
 * 不可变的空指标实现：所有值均为 0/0.0。
 *
 * <p>无 {@code MetricsProvider} 时由 {@link ExtMetrics#empty()} 返回，
 * 保证纯内核下 {@code getExtMetrics(...)} 永不返回 null。</p>
 *
 * @author xiangganluo
 */
final class EmptyExtMetrics implements ExtMetrics {

    static final EmptyExtMetrics INSTANCE = new EmptyExtMetrics();

    private EmptyExtMetrics() {
    }

    @Override
    public long getTotalInvocations() {
        return 0;
    }

    @Override
    public long getSuccessInvocations() {
        return 0;
    }

    @Override
    public long getFailureInvocations() {
        return 0;
    }

    @Override
    public double getSuccessRate() {
        return 0.0;
    }

    @Override
    public double getAverageResponseTime() {
        return 0.0;
    }

    @Override
    public long getMaxResponseTime() {
        return 0;
    }

    @Override
    public long getMinResponseTime() {
        return 0;
    }

    @Override
    public long getExceptionCount() {
        return 0;
    }

    @Override
    public long getLastInvocationTime() {
        return 0;
    }

    @Override
    public double getQPS() {
        return 0.0;
    }
}
