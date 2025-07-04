package com.flexpoint.core.monitor.metrics;

import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.monitor.enums.CollectorType;

import java.util.Map;

/**
 * 监控数据采集器扩展接口
 * @author luoxianggan
 */
public interface MetricsCollector {

    /**
     * 采集扩展点指标
     * @param extensionId 扩展点ID
     * @param metrics 指标数据
     * @param context 业务上下文
     * @param collectorType 采集器类型
     */
    void collect(String extensionId, ExtensionMonitor.ExtensionMetrics metrics, Map<String, Object> context, CollectorType collectorType);
} 