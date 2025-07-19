package com.flexpoint.core.monitor;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.extension.ExtensionAbility;
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

        // 队列满时由调用线程执行
        this.executor = new ThreadPoolExecutor(
            config.getAsyncCorePoolSize(),
            config.getAsyncMaxPoolSize(), 
            config.getAsyncKeepAliveTime(), 
            TimeUnit.SECONDS, 
            workQueue,
            r -> {
                Thread t = new Thread(r, "flexpoint-async-monitor-" + Thread.currentThread().getId());
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        this.delegate = new DefaultExtensionMonitor(config);
        
        log.info("创建异步监控器: corePoolSize={}, maxPoolSize={}, keepAliveTime={}s, queueSize={}", 
                config.getAsyncCorePoolSize(), config.getAsyncMaxPoolSize(), 
                config.getAsyncKeepAliveTime(), queueSize);
    }
    
    @Override
    public void recordInvocation(ExtensionAbility extensionAbility, long duration, boolean success) {
        submitTask(() -> delegate.recordInvocation(extensionAbility, duration, success));
    }
    
    @Override
    public void recordInvocation(ExtensionAbility extensionAbility, long duration, boolean success, Map<String, Object> context) {
        submitTask(() -> delegate.recordInvocation(extensionAbility, duration, success, context));
    }
    
    @Override
    public void recordException(ExtensionAbility extensionAbility, Throwable exception) {
        submitTask(() -> delegate.recordException(extensionAbility, exception));
    }
    
    @Override
    public void recordException(ExtensionAbility extensionAbility, Throwable exception, Map<String, Object> context) {
        submitTask(() -> delegate.recordException(extensionAbility, exception, context));
    }
    
    @Override
    public ExtensionMetrics getMetrics(ExtensionAbility extensionAbility) {
        return delegate.getMetrics(extensionAbility);
    }
    
    @Override
    public Map<String, ExtensionMetrics> getAllMetrics() {
        return delegate.getAllMetrics();
    }
    
    @Override
    public void resetMetrics(ExtensionAbility extensionAbility) {
        submitTask(() -> delegate.resetMetrics(extensionAbility));
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
     * 获取线程池状态
     */
    public ThreadPoolStatus getThreadPoolStatus() {
        if (executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
            return new ThreadPoolStatus(
                    tpe.getCorePoolSize(),
                tpe.getMaximumPoolSize(),
                tpe.getActiveCount(),
                tpe.getPoolSize(),
                tpe.getTaskCount(),
                tpe.getCompletedTaskCount()
            );
        }
        return null;
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
    
    /**
     * 线程池状态
     */
    public static class ThreadPoolStatus {
        private final int corePoolSize;
        private final int maxPoolSize;
        private final int activeCount;
        private final int poolSize;
        private final long taskCount;
        private final long completedTaskCount;

        public ThreadPoolStatus(int corePoolSize, int maxPoolSize, int activeCount,
                               int poolSize, long taskCount, long completedTaskCount) {
            this.corePoolSize = corePoolSize;
            this.maxPoolSize = maxPoolSize;
            this.activeCount = activeCount;
            this.poolSize = poolSize;
            this.taskCount = taskCount;
            this.completedTaskCount = completedTaskCount;
        }

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public int getActiveCount() {
            return activeCount;
        }

        public int getPoolSize() {
            return poolSize;
        }

        public long getTaskCount() {
            return taskCount;
        }

        public long getCompletedTaskCount() {
            return completedTaskCount;
        }

        /**
         * 获取线程池利用率
         */
        public double getUtilization() {
            return maxPoolSize > 0 ? (double) activeCount / maxPoolSize : 0.0;
        }

        /**
         * 获取任务完成率
         */
        public double getCompletionRate() {
            return taskCount > 0 ? (double) completedTaskCount / taskCount : 0.0;
        }

        /**
         * 是否接近满负荷
         */
        public boolean isNearCapacity() {
            return getUtilization() > 0.8; // 超过80%认为接近满负荷
        }
    }
}