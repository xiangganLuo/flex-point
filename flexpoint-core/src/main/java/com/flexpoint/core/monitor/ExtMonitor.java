package com.flexpoint.core.monitor;

import com.flexpoint.core.ext.ExtAbility;

import java.util.Map;

/**
 * 扩展点监控接口
 * 提供扩展点调用统计、性能监控和异常监控
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ExtMonitor {
    
    /**
     * 记录扩展点调用
     *
     * @param extAbility 扩展点能力实例
     * @param duration 调用耗时（毫秒）
     * @param success 是否成功
     */
    void recordInvocation(ExtAbility extAbility, long duration, boolean success);

    /**
     * 记录扩展点异常
     *
     * @param extAbility 扩展点能力实例
     * @param exception 异常信息
     */
    void recordException(ExtAbility extAbility, Throwable exception);
    
    /**
     * 获取扩展点指标
     *
     * @param extAbility 扩展点能力实例
     * @return 扩展点指标
     */
    ExtMetrics getMetrics(ExtAbility extAbility);
    
    /**
     * 获取所有扩展点指标
     *
     * @return 所有扩展点指标，key为扩展点标识符
     */
    Map<String, ExtMetrics> getAllMetrics();
    
    /**
     * 重置扩展点指标
     *
     * @param extAbility 扩展点能力实例
     */
    void resetMetrics(ExtAbility extAbility);
    
    /**
     * 获取监控器状态
     *
     * @return 监控器状态
     */
    MonitorStatus getStatus();
    
    /**
     * 扩展点指标
     */
    interface ExtMetrics {
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
    
    /**
     * 监控器状态
     */
    interface MonitorStatus {
        /**
         * 是否启用
         */
        boolean isEnabled();
        
        /**
         * 是否支持异步
         */
        boolean isAsyncSupported();
        
        /**
         * 是否支持持久化
         */
        boolean isPersistenceSupported();
        
        /**
         * 获取存储类型
         */
        String getStorageType();
        
        /**
         * 获取监听器数量
         */
        int getListenerCount();
    }

} 