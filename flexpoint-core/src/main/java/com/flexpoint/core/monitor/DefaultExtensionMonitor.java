package com.flexpoint.core.monitor;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 默认扩展点监控实现
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class DefaultExtensionMonitor implements ExtensionMonitor {
    
    private final ConcurrentHashMap<String, ExtensionMetricsImpl> metricsMap = new ConcurrentHashMap<>();
    
    @Override
    public void recordInvocation(String extensionId, long duration, boolean success) {
        ExtensionMetricsImpl metrics = metricsMap.computeIfAbsent(extensionId, k -> new ExtensionMetricsImpl());
        metrics.recordInvocation(duration, success);
        
        log.debug("扩展点调用记录: id={}, duration={}ms, success={}", extensionId, duration, success);
    }
    
    @Override
    public void recordException(String extensionId, Throwable exception) {
        ExtensionMetricsImpl metrics = metricsMap.computeIfAbsent(extensionId, k -> new ExtensionMetricsImpl());
        metrics.recordException();
        
        log.warn("扩展点异常记录: id={}, exception={}", extensionId, exception.getMessage());
    }
    
    @Override
    public ExtensionMetrics getMetrics(String extensionId) {
        return metricsMap.getOrDefault(extensionId, new ExtensionMetricsImpl());
    }
    
    @Override
    public java.util.Map<String, ExtensionMetrics> getAllMetrics() {
        return new ConcurrentHashMap<>(metricsMap);
    }
    
    @Override
    public void resetMetrics(String extensionId) {
        metricsMap.remove(extensionId);
        log.info("扩展点指标已重置: id={}", extensionId);
    }
    
    /**
     * 扩展点指标实现
     */
    private static class ExtensionMetricsImpl implements ExtensionMetrics {
        private final AtomicLong totalInvocations = new AtomicLong(0);
        private final AtomicLong successInvocations = new AtomicLong(0);
        private final AtomicLong failureInvocations = new AtomicLong(0);
        private final AtomicLong exceptionCount = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private final AtomicLong maxResponseTime = new AtomicLong(0);
        private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicReference<Long> lastInvocationTime = new AtomicReference<>(0L);
        
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
    }
} 