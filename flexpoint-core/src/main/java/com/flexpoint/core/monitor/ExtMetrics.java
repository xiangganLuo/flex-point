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
     * 获取P95响应时间（毫秒）
     */
    long getP95ResponseTime();

    /**
     * 获取P99响应时间（毫秒）
     */
    long getP99ResponseTime();

    /**
     * 获取QPS（每秒查询数）
     */
    double getQPS();
}