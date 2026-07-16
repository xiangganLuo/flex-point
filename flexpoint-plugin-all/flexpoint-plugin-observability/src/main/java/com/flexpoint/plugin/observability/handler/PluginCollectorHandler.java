package com.flexpoint.plugin.observability.handler;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.ExtMetrics;
import com.flexpoint.core.monitor.handler.MonitorHandler;
import com.flexpoint.plugin.observability.enums.CollectorType;
import com.flexpoint.plugin.observability.metrics.MetricsCollector;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * 插件域：采集器分发处理器。
 *
 * @author xiangganluo
 */
@Slf4j
public class PluginCollectorHandler implements MonitorHandler {
    private final List<MetricsCollector> collectors;

    public PluginCollectorHandler(List<MetricsCollector> collectors) {
        this.collectors = collectors == null ? Collections.emptyList() : collectors;
    }

    @Override
    public void handleInvocation(ExtAbility extAbility, long duration, boolean success, ExtMetrics metrics) {
        if (metrics == null) {
            log.warn("PluginCollectorHandler received null metrics, skip");
            return;
        }
        for (MetricsCollector collector : collectors) {
            try {
                collector.collect(extAbility.getExtId(), metrics, CollectorType.REALTIME);
            } catch (Exception e) {
                log.warn("collector collect error", e);
            }
        }
    }

    @Override
    public void handleException(ExtAbility extAbility, Throwable exception, ExtMetrics metrics) {
        handleInvocation(extAbility, 0L, false, metrics);
    }
}
