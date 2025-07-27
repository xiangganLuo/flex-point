package com.flexpoint.core.monitor;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.ext.ExtAbility;
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
public class AsyncExtMonitor implements ExtMonitor {
    
    private final ExecutorService executor;
    
    private final ExtMonitor delegate;

    /**
     * 使用指定配置创建异步监控器
     */
    public AsyncExtMonitor(ExtMonitor delegate) {
        // 队列满时由调用线程执行
        this.executor = new ThreadPoolExecutor(
            delegate.getConfig().getAsyncCorePoolSize(),
            delegate.getConfig().getAsyncMaxPoolSize(), 
            delegate.getConfig().getAsyncKeepAliveTime(), 
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(delegate.getConfig().getAsyncQueueSize()),
            r -> {
                Thread t = new Thread(r, "flexpoint-async-monitor-" + Thread.currentThread().getId());
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        this.delegate = delegate;
    }
    
    @Override
    public void recordInvocation(ExtAbility extAbility, long duration, boolean success) {
        submitTask(() -> delegate.recordInvocation(extAbility, duration, success));
    }

    @Override
    public void recordException(ExtAbility extAbility, Throwable exception) {
        submitTask(() -> delegate.recordException(extAbility, exception));
    }
    
    @Override
    public ExtMetrics getExtMetrics(ExtAbility extAbility) {
        return delegate.getExtMetrics(extAbility);
    }

    @Override
    public Map<String, ExtMetrics> getAllExtMetrics() {
        return delegate.getAllExtMetrics();
    }

    @Override
    public FlexPointConfig.MonitorConfig getConfig() {
        return delegate.getConfig();
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
    
}