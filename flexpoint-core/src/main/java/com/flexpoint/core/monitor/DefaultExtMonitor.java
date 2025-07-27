package com.flexpoint.core.monitor;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.handler.MetricsProvider;
import com.flexpoint.core.monitor.handler.MonitorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 默认扩展点监控实现（全链式）
 *
 * @author xiangganluo
 * @version 2.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultExtMonitor implements ExtMonitor {

    private final FlexPointConfig.MonitorConfig config;
    private final List<MonitorHandler> handlerChain = new CopyOnWriteArrayList<>();

    @Override
    public void recordInvocation(ExtAbility extAbility, long duration, boolean success) {
        if (!config.isEnabled()) return;
        for (MonitorHandler handler : handlerChain) {
            try {
                handler.handleInvocation(extAbility, duration, success, getExtMetrics(extAbility));
            } catch (Exception e) {
                log.warn("MonitorHandler.handleInvocation异常: {}", e.getMessage());
            }
        }
    }

    @Override
    public void recordException(ExtAbility extAbility, Throwable exception) {
        if (!config.isEnabled()) return;
        for (MonitorHandler handler : handlerChain) {
            try {
                handler.handleException(extAbility, exception, getExtMetrics(extAbility));
            } catch (Exception e) {
                log.warn("MonitorHandler.handleException异常: {}", e.getMessage());
            }
        }
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
        return config;
    }

    /**
     * 添加责任链节点
     */
    public void addHandler(MonitorHandler handler) {
        if (handler != null) handlerChain.add(handler);
    }

    /**
     * 移除责任链节点
     */
    public void removeHandler(MonitorHandler handler) {
        handlerChain.remove(handler);
    }

    /**
     * 清空责任链
     */
    public void clearHandlers() {
        handlerChain.clear();
    }

} 