package com.flexpoint.core.monitor;

import com.flexpoint.core.config.FlexPointConfig;
import lombok.RequiredArgsConstructor;

/**
 * 默认扩展点监控实现（同步链式）
 * 继承抽象链式监控基类，提供同步执行能力
 *
 * @author xiangganluo
 * @version 2.3.0
 */
@RequiredArgsConstructor
public class DefaultExtMonitor extends AbstractChainExtMonitor {

    private final FlexPointConfig.MonitorConfig config;

    @Override
    protected FlexPointConfig.MonitorConfig getMonitorConfig() {
        return config;
    }

    @Override
    protected void executeMonitorTask(Runnable task) {
        // 同步执行监控任务
        task.run();
    }
}