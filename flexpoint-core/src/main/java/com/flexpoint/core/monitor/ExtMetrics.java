package com.flexpoint.core.monitor;

/**
 * 扩展点指标
 */
public interface ExtMetrics {
    /**
     * 获取总调用次数
     */
    long getTotalInvocations();

    /**
     * 获取成功调用次数
     */
    long getSuccessInvocations();

    /**
     * 获取失败调用次数
     */
    long getFailureInvocations();

    /**
     * 获取成功率
     */
    double getSuccessRate();

    /**
     * 获取平均响应时间（毫秒）
     */
    double getAverageResponseTime();

    /**
     * 获取最大响应时间（毫秒）
     */
    long getMaxResponseTime();

    /**
     * 获取最小响应时间（毫秒）
     */
    long getMinResponseTime();

    /**
     * 获取异常次数
     */
    long getExceptionCount();

    /**
     * 获取最后调用时间
     */
    long getLastInvocationTime();

    /**
     * 获取QPS（每秒查询数）
     */
    double getQPS();

    /**
     * 返回不可变的空指标（各值均为 0）。
     * <p>用于纯内核（未装配 observability 插件、无 {@code MetricsProvider}）场景，
     * 避免调用方拿到 null 而 NPE。</p>
     */
    static ExtMetrics empty() {
        return EmptyExtMetrics.INSTANCE;
    }
}