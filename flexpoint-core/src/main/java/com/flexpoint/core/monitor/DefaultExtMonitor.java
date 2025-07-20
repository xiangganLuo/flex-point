package com.flexpoint.core.monitor;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.utils.ExtUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 默认扩展点监控实现
 * 支持配置控制监控行为
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class DefaultExtMonitor implements ExtMonitor {
    
    private final ConcurrentHashMap<String, ExtMetricsImpl> metricsMap = new ConcurrentHashMap<>();
    private final ExecutorService asyncExecutor;
    
    private MonitorPipeline pipeline;
    public void setMonitorPipeline(MonitorPipeline pipeline) {
        this.pipeline = pipeline;
    }
    
    private final FlexPointConfig.MonitorConfig config;
    
    /**
     * 使用默认配置创建监控器
     */
    public DefaultExtMonitor() {
        this(new FlexPointConfig.MonitorConfig());
    }
    
    /**
     * 使用指定配置创建监控器
     */
    public DefaultExtMonitor(FlexPointConfig.MonitorConfig config) {
        this.config = config;
        this.asyncExecutor = config.isAsyncEnabled() ? 
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "flexpoint-monitor-async");
                t.setDaemon(true);
                return t;
            }) : null;
            
        if (config.isEnabled()) {
            log.info("创建监控器: logInvocation={}, isLogSelection={}, logExceptionDetails={}, performanceStatsEnabled={}, asyncEnabled={}",
                    config.isLogInvocation(), config.isLogSelection(),
                    config.isLogExceptionDetails(), config.isPerformanceStatsEnabled(), config.isAsyncEnabled());
        } else {
            log.info("监控器已禁用");
        }
    }

    @Override
    public void recordInvocation(ExtAbility extAbility, long duration, boolean success) {
        if (!config.isEnabled()) {
            return;
        }
        
        if (config.isAsyncEnabled() && asyncExecutor != null) {
            asyncExecutor.submit(() -> doRecordInvocation(extAbility, duration, success));
        } else {
            doRecordInvocation(extAbility, duration, success);
        }
    }
    
    private void doRecordInvocation(ExtAbility extAbility, long duration, boolean success) {
        String ext = ExtUtil.getExtId(extAbility);
        ExtMetricsImpl metrics = null;
        if (config.isPerformanceStatsEnabled()) {
            metrics = metricsMap.computeIfAbsent(ext, k -> new ExtMetricsImpl());
            metrics.recordInvocation(duration, success);
        }

        if (config.isLogInvocation()) {
            log.debug("扩展点调用记录: id={}, duration={}ms, success={}",
                    ext, duration, success);
        }
        
        // 埋点：统一交给MonitorPipeline
        if (pipeline != null) {
            pipeline.afterInvoke(extAbility, duration, success, metrics);
        }
    }
    
    @Override
    public void recordException(ExtAbility extAbility, Throwable exception) {
        if (!config.isEnabled()) {
            return;
        }

        if (config.isAsyncEnabled() && asyncExecutor != null) {
            asyncExecutor.submit(() -> doRecordException(extAbility, exception));
        } else {
            doRecordException(extAbility, exception);
        }
    }
    
    private void doRecordException(ExtAbility extAbility, Throwable exception) {
        String ext = ExtUtil.getExtId(extAbility);
        ExtMetricsImpl metrics = null;
        if (config.isPerformanceStatsEnabled()) {
            metrics = metricsMap.computeIfAbsent(ext, k -> new ExtMetricsImpl());
            metrics.recordException();
        }
        
        if (config.isLogExceptionDetails()) {
            log.warn("扩展点异常记录: id={}, exception={}",
                    ext, exception.getMessage(), exception);
        } else {
            log.warn("扩展点异常记录: id={}, exception={}",
                    ext, exception.getMessage());
        }
        
        // 埋点：统一交给MonitorPipeline
        if (pipeline != null) {
            pipeline.onException(extAbility, exception, metrics);
        }
    }
    
    @Override
    public ExtMetrics getMetrics(ExtAbility extAbility) {
        if (!config.isEnabled() || !config.isPerformanceStatsEnabled()) {
            return new ExtMetricsImpl();
        }
        String ext = ExtUtil.getExtId(extAbility);
        return metricsMap.getOrDefault(ext, new ExtMetricsImpl());
    }
    
    @Override
    public Map<String, ExtMetrics> getAllMetrics() {
        if (!config.isEnabled() || !config.isPerformanceStatsEnabled()) {
            return new ConcurrentHashMap<>();
        }
        return new ConcurrentHashMap<>(metricsMap);
    }
    
    @Override
    public void resetMetrics(ExtAbility extAbility) {
        if (!config.isEnabled()) {
            return;
        }
        
        String ext = ExtUtil.getExtId(extAbility);
        metricsMap.remove(ext);
        log.info("扩展点指标已重置: id={}", ext);
        
        // 通知监听器
        // 埋点：统一交给MonitorPipeline
        if (pipeline != null) {
            pipeline.onMetricsReset(extAbility);
        }
    }
    
    @Override
    public MonitorStatus getStatus() {
        return new MonitorStatusImpl();
    }

    /**
     * 关闭监控器
     */
    public void shutdown() {
        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.shutdown();
            log.info("监控器异步执行器已关闭");
        }
    }
    
    /**
     * 扩展点指标实现
     */
    private static class ExtMetricsImpl implements ExtMetrics {
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

    /**
     * 监控器状态实现
     */
    private class MonitorStatusImpl implements MonitorStatus {
        @Override
        public boolean isEnabled() {
            return config.isEnabled();
        }
        
        @Override
        public boolean isAsyncSupported() {
            return config.isAsyncEnabled();
        }
        
        @Override
        public boolean isPersistenceSupported() {
            return false;
        }
        
        @Override
        public String getStorageType() {
            return "MEMORY";
        }
        
        @Override
        public int getListenerCount() {
            return 0; // No listeners to count
        }
    }
} 