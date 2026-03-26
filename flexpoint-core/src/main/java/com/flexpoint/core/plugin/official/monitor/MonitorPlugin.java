package com.flexpoint.core.plugin.official.monitor;

import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.plugin.*;
import com.flexpoint.core.plugin.official.observability.handler.PluginAlertHandler;
import com.flexpoint.core.plugin.official.observability.handler.PluginMetricsHandler;

import java.util.EnumSet;

/**
 * 官方内置：监控插件（最小集）。
 *
 * <p>功能：向现有监控器注入默认的 handler 链：
 * MetricsHandler（本地指标统计） + AlertHandler（按策略告警）。
 * </p>
 */
public final class MonitorPlugin implements Plugin {

    public static final String PLUGIN_ID = "core.monitor.default-chain";

    private final PluginDescriptor descriptor = PluginDescriptor.builder(PLUGIN_ID, "1.0.0")
            .capabilities(EnumSet.of(PluginCapability.MONITOR))
            .order(15)
            .critical(false)
            .build();

    private ExtMonitor monitor;
    private PluginMetricsHandler metricsHandler;
    private PluginAlertHandler alertHandler;

    @Override
    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void init(PluginContext context) {
        this.monitor = context.monitor();
        if (this.monitor == null) {
            throw new IllegalStateException("MonitorPlugin requires ExtMonitor from PluginContext");
        }
        this.metricsHandler = new PluginMetricsHandler();
        this.alertHandler = new PluginAlertHandler();
    }

    @Override
    public void start() {
        monitor.addHandler(metricsHandler);
        monitor.addHandler(alertHandler);
    }

    @Override
    public void stop() {
        if (monitor != null) {
            monitor.removeHandler(metricsHandler);
            monitor.removeHandler(alertHandler);
        }
    }

    @Override
    public void destroy() {
        this.metricsHandler = null;
        this.alertHandler = null;
        this.monitor = null;
    }
}
