package com.flexpoint.core.monitor;

import com.flexpoint.core.config.FlexPointConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 异步扩展点监控实现
 * 提供高性能的异步监控处理，减少对业务逻辑的影响
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class AsyncExtensionMonitor implements ExtensionMonitor {
    
    private final ExtensionMonitor delegate;
    private final ExecutorService executor;
    private final BlockingQueue<Runnable> workQueue;
    private final int queueSize;
    
    /**
     * 使用默认配置创建异步监控器
     */
    public AsyncExtensionMonitor() {
        this(new FlexPointConfig.MonitorConfig());
    }
    
    /**
     * 使用指定配置创建异步监控器
     */
    public AsyncExtensionMonitor(FlexPointConfig.MonitorConfig config) {
        this.queueSize = config.getAsyncQueueSize();
        this.workQueue = new LinkedBlockingQueue<>(queueSize);
        this.executor = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS, workQueue,
            r -> {
                Thread t = new Thread(r, "flexpoint-async-monitor");
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        this.delegate = new DefaultExtensionMonitor(config);
        
        log.info("创建异步监控器: queueSize={}", queueSize);
    }
    
    @Override
    public void recordInvocation(String extensionId, long duration, boolean success) {
        submitTask(() -> delegate.recordInvocation(extensionId, duration, success));
    }
    
    @Override
    public void recordInvocation(String extensionId, long duration, boolean success, Map<String, Object> context) {
        submitTask(() -> delegate.recordInvocation(extensionId, duration, success, context));
    }
    
    @Override
    public void recordException(String extensionId, Throwable exception) {
        submitTask(() -> delegate.recordException(extensionId, exception));
    }
    
    @Override
    public void recordException(String extensionId, Throwable exception, Map<String, Object> context) {
        submitTask(() -> delegate.recordException(extensionId, exception, context));
    }
    
    @Override
    public ExtensionMetrics getMetrics(String extensionId) {
        return delegate.getMetrics(extensionId);
    }
    
    @Override
    public Map<String, ExtensionMetrics> getAllMetrics() {
        return delegate.getAllMetrics();
    }
    
    @Override
    public void resetMetrics(String extensionId) {
        submitTask(() -> delegate.resetMetrics(extensionId));
    }
    
    @Override
    public MonitorStatus getStatus() {
        return new AsyncMonitorStatus(delegate.getStatus());
    }
    
    /**
     * 提交异步任务
     */
    private void submitTask(Runnable task) {
        try {
            if (!executor.isShutdown()) {
                executor.submit(task);
            }
        } catch (RejectedExecutionException e) {
            log.warn("监控任务队列已满，使用调用线程执行: {}", e.getMessage());
            task.run();
        } catch (Exception e) {
            log.error("提交监控任务失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 关闭异步监控器
     */
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("异步监控器已关闭");
        }
    }
    
    /**
     * 获取队列状态
     */
    public QueueStatus getQueueStatus() {
        return new QueueStatus(workQueue.size(), queueSize);
    }
    
    /**
     * 异步监控器状态
     */
    private static class AsyncMonitorStatus implements MonitorStatus {
        private final MonitorStatus delegate;
        
        AsyncMonitorStatus(MonitorStatus delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public boolean isEnabled() {
            return delegate.isEnabled();
        }
        
        @Override
        public boolean isAsyncSupported() {
            return true;
        }
        
        @Override
        public boolean isPersistenceSupported() {
            return delegate.isPersistenceSupported();
        }
        
        @Override
        public String getStorageType() {
            return delegate.getStorageType();
        }
        
        @Override
        public int getListenerCount() {
            return delegate.getListenerCount();
        }
    }
    
    /**
     * 队列状态
     */
    public static class QueueStatus {
        private final int currentSize;
        private final int maxSize;
        
        public QueueStatus(int currentSize, int maxSize) {
            this.currentSize = currentSize;
            this.maxSize = maxSize;
        }
        
        public int getCurrentSize() {
            return currentSize;
        }
        
        public int getMaxSize() {
            return maxSize;
        }
        
        public double getUtilization() {
            return maxSize > 0 ? (double) currentSize / maxSize : 0.0;
        }
        
        public boolean isFull() {
            return currentSize >= maxSize;
        }
    }
} 