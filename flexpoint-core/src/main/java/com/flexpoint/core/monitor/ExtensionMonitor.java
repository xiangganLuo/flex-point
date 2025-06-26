package com.flexpoint.core.monitor;

/**
 * 扩展点监控接口
 * 提供扩展点调用统计、性能监控和异常监控
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ExtensionMonitor {
    
    /**
     * 记录扩展点调用
     *
     * @param extensionId 扩展点ID
     * @param duration 调用耗时（毫秒）
     * @param success 是否成功
     */
    void recordInvocation(String extensionId, long duration, boolean success);
    
    /**
     * 记录扩展点异常
     *
     * @param extensionId 扩展点ID
     * @param exception 异常信息
     */
    void recordException(String extensionId, Throwable exception);
    
    /**
     * 获取扩展点指标
     *
     * @param extensionId 扩展点ID
     * @return 扩展点指标
     */
    ExtensionMetrics getMetrics(String extensionId);
    
    /**
     * 获取所有扩展点指标
     *
     * @return 所有扩展点指标
     */
    java.util.Map<String, ExtensionMetrics> getAllMetrics();
    
    /**
     * 重置扩展点指标
     *
     * @param extensionId 扩展点ID
     */
    void resetMetrics(String extensionId);
    
    /**
     * 扩展点指标
     */
    interface ExtensionMetrics {
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
    }
} 