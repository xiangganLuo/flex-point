package com.flexpoint.core.monitor;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.handler.MetricsProvider;
import com.flexpoint.core.monitor.handler.MonitorHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 抽象链式扩展点监控基类
 * 提供链式调用能力的通用实现，可以被同步和异步监控器复用
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public abstract class AbstractChainExtMonitor implements ExtMonitor {

    private final List<MonitorHandler> handlerChain = new CopyOnWriteArrayList<>();

    /**
     * 获取监控配置
     */
    protected abstract FlexPointConfig.MonitorConfig getMonitorConfig();

    /**
     * 执行监控任务
     * 子类可以重写此方法来实现同步或异步执行
     */
    protected abstract void executeMonitorTask(Runnable task);

    @Override
    public void recordInvocation(ExtAbility extAbility, long duration, boolean success) {
        if (!getMonitorConfig().isEnabled()) return;
        
        executeMonitorTask(() -> {
            for (MonitorHandler handler : handlerChain) {
                try {
                    handler.handleInvocation(extAbility, duration, success, getExtMetrics(extAbility));
                } catch (Exception e) {
                    log.warn("MonitorHandler.handleInvocation异常: {}", e.getMessage());
                }
            }
        });
    }

    @Override
    public void recordException(ExtAbility extAbility, Throwable exception) {
        if (!getMonitorConfig().isEnabled()) return;
        
        executeMonitorTask(() -> {
            for (MonitorHandler handler : handlerChain) {
                try {
                    handler.handleException(extAbility, exception, getExtMetrics(extAbility));
                } catch (Exception e) {
                    log.warn("MonitorHandler.handleException异常: {}", e.getMessage());
                }
            }
        });
    }

    @Override
    public ExtMetrics getExtMetrics(ExtAbility extAbility) {
        for (MonitorHandler handler : handlerChain) {
            if (handler instanceof MetricsProvider) {
                return ((MetricsProvider) handler).getMetrics(extAbility);
            }
        }
        return null;
    }

    @Override
    public Map<String, ExtMetrics> getAllExtMetrics() {
        for (MonitorHandler handler : handlerChain) {
            if (handler instanceof MetricsProvider) {
                return ((MetricsProvider) handler).getAllMetrics();
            }
        }
        return null;
    }

    @Override
    public FlexPointConfig.MonitorConfig getConfig() {
        return getMonitorConfig();
    }

    @Override
    public void addHandler(MonitorHandler handler) {
        if (handler != null) {
            handlerChain.add(handler);
        }
    }

    @Override
    public void removeHandler(MonitorHandler handler) {
        handlerChain.remove(handler);
    }

    @Override
    public void clearHandlers() {
        handlerChain.clear();
    }

    /**
     * 获取处理器链
     */
    protected List<MonitorHandler> getHandlerChain() {
        return handlerChain;
    }
} 