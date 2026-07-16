package com.flexpoint.plugin.observability.metrics;

import com.flexpoint.core.monitor.ExtMetrics;
import com.flexpoint.plugin.observability.enums.CollectorType;

/**
 * 监控数据采集器扩展接口
 * @author xiangganluo
 */
public interface MetricsCollector {

    /**
     * 采集扩展点指标
     * @param ext 扩展点ID
     * @param metrics 指标数据
     * @param collectorType 采集器类型
     */
    void collect(String ext, ExtMetrics metrics, CollectorType collectorType);
}
