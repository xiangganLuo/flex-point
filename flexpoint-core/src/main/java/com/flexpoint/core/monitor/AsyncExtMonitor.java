package com.flexpoint.core.monitor;

import com.flexpoint.core.config.FlexPointConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 异步扩展点监控实现
 * 继承抽象链式监控基类，提供异步执行能力
 *
 * @author xiangganluo
 * @version 2.0.0
 */
@Slf4j
public class AsyncExtMonitor extends AbstractChainExtMonitor {
    
    private final ExecutorService executor;
    private final FlexPointConfig.MonitorConfig config;

    /**
     * 使用指定配置创建异步监控器
     */
    public AsyncExtMonitor(FlexPointConfig.MonitorConfig config) {
        this.config = config;
        // 队列满时由调用线程执行
        this.executor = new ThreadPoolExecutor(
            config.getAsyncCorePoolSize(),
            config.getAsyncMaxPoolSize(), 
            config.getAsyncKeepAliveTime(), 
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(config.getAsyncQueueSize()),
            r -> {
                Thread t = new Thread(r, "flexpoint-async-monitor-" + Thread.currentThread().getId());
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
    
    @Override
    protected FlexPointConfig.MonitorConfig getMonitorConfig() {
        return config;
    }

    @Override
    protected void executeMonitorTask(Runnable task) {
        submitTask(task);
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