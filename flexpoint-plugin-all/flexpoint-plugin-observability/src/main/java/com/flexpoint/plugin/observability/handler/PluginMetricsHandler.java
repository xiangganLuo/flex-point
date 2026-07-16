package com.flexpoint.plugin.observability.handler;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.ExtMetrics;
import com.flexpoint.core.monitor.ExtMetricsImpl;
import com.flexpoint.core.monitor.handler.MetricsProvider;
import com.flexpoint.core.monitor.handler.MonitorHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件域：最小指标统计处理器（本地内存）。
 *
 * @author xiangganluo
 */
@Slf4j
public class PluginMetricsHandler implements MonitorHandler, MetricsProvider {
    private final ConcurrentHashMap<String, ExtMetricsImpl> metricsMap = new ConcurrentHashMap<>();

    @Override
    public void handleInvocation(ExtAbility extAbility, long duration, boolean success, ExtMetrics m) {
        ExtMetricsImpl metrics = metricsMap.computeIfAbsent(extAbility.getExtId(), k -> new ExtMetricsImpl());
        metrics.recordInvocation(duration, success);
        if (log.isDebugEnabled()) {
            log.debug("指标累计: extId={}, total={}, success={}", extAbility.getExtId(),
                    metrics.getTotalInvocations(), metrics.getSuccessInvocations());
        }
    }

    @Override
    public void handleException(ExtAbility extAbility, Throwable exception, ExtMetrics m) {
        ExtMetricsImpl metrics = metricsMap.computeIfAbsent(extAbility.getExtId(), k -> new ExtMetricsImpl());
        metrics.recordException();
        if (log.isDebugEnabled()) {
            log.debug("异常计数: extId={}, exceptionCount={}", extAbility.getExtId(), metrics.getExceptionCount());
        }
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
