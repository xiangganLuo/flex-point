package com.flexpoint.core.monitor.handler;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.ExtMetrics;
import com.flexpoint.core.monitor.enums.CollectorType;
import com.flexpoint.core.monitor.metrics.MetricsCollector;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 采集器分发节点
 * 负责调用所有 MetricsCollector 进行采集
 * @author luoxianggan
 */
@Slf4j
public class CollectorHandler implements MonitorHandler {
    private final List<MetricsCollector> collectors;

    public CollectorHandler(List<MetricsCollector> collectors) {
        this.collectors = collectors;
    }

    @Override
    public void handleInvocation(ExtAbility extAbility, long duration, boolean success, ExtMetrics metrics) {
        if (metrics == null) {
            log.warn("handleInvocation received null metrics, skipping collection.");
            return;
        }

        for (MetricsCollector collector : collectors) {
            try {
                collector.collect(extAbility.getExtId(), metrics, CollectorType.REALTIME);
            } catch (Exception e) {
                log.warn("采集器采集异常", e);
            }
        }
    }

    @Override
    public void handleException(ExtAbility extAbility, Throwable exception, ExtMetrics metrics) {
        handleInvocation(extAbility, 0L, false, metrics);
    }
}
