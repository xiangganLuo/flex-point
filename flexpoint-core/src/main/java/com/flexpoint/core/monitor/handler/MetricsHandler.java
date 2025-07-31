package com.flexpoint.core.monitor.handler;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.ExtMetrics;
import com.flexpoint.core.monitor.ExtMetricsImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地内存统计节点
 * 负责本地统计扩展点调用和异常，并提供指标查询能力
 * @author luoxianggan
 */
public class MetricsHandler implements MonitorHandler, MetricsProvider {

    /**
     * key: 扩展点ID value: 统计对象
     */
    private final ConcurrentHashMap<String, ExtMetricsImpl> metricsMap = new ConcurrentHashMap<>();

    @Override
    public void handleInvocation(ExtAbility extAbility, long duration, boolean success, ExtMetrics m) {
        ExtMetricsImpl metrics = metricsMap.computeIfAbsent(extAbility.getExtId(), k -> new ExtMetricsImpl());
        metrics.recordInvocation(duration, success);
    }

    @Override
    public void handleException(ExtAbility extAbility, Throwable exception, ExtMetrics m) {
        String ext = extAbility.getClass().getName();
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