package com.flexpoint.core.monitor.handler;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.ExtMetrics;

import java.util.Map;

/**
 * 指标提供者接口
 * 供指标节点实现，暴露指标查询能力
 * @author luoxianggan
 */
public interface MetricsProvider {
    ExtMetrics getMetrics(ExtAbility extAbility);
    Map<String, ExtMetrics> getAllMetrics();
} 