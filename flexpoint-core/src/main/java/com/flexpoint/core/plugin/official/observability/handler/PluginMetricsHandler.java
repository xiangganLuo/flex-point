package com.flexpoint.core.plugin.official.observability.handler;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.ExtMetrics;
import com.flexpoint.core.monitor.ExtMetricsImpl;
import com.flexpoint.core.monitor.handler.MetricsProvider;
import com.flexpoint.core.monitor.handler.MonitorHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件域：最小指标统计处理器（本地内存）。
 */
public class PluginMetricsHandler implements MonitorHandler, MetricsProvider {
    private final ConcurrentHashMap<String, ExtMetricsImpl> metricsMap = new ConcurrentHashMap<>();

    @Override
    public void handleInvocation(ExtAbility extAbility, long duration, boolean success, ExtMetrics m) {
        ExtMetricsImpl metrics = metricsMap.computeIfAbsent(extAbility.getExtId(), k -> new ExtMetricsImpl());
        metrics.recordInvocation(duration, success);
    }

    @Override
    public void handleException(ExtAbility extAbility, Throwable exception, ExtMetrics m) {
        ExtMetricsImpl metrics = metricsMap.computeIfAbsent(extAbility.getExtId(), k -> new ExtMetricsImpl());
        metrics.recordException();
    }

    @Override
    public ExtMetrics getMetrics(ExtAbility extAbility) {
        return metricsMap.getOrDefault(extAbility.getExtId(), new ExtMetricsImpl());
    }

    @Override
    public Map<String, ExtMetrics> getAllMetrics() {
        return new ConcurrentHashMap<>(metricsMap);
    }
}

