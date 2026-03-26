package com.flexpoint.core.plugin.official.observability;

import com.flexpoint.core.event.EventBus;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.monitor.metrics.MetricsCollector;
import com.flexpoint.core.plugin.official.observability.alert.AlertStrategy;
import com.flexpoint.core.plugin.official.observability.handler.PluginAlertHandler;
import com.flexpoint.core.plugin.official.observability.handler.PluginCollectorHandler;
import com.flexpoint.core.plugin.official.observability.handler.PluginMetricsHandler;
import com.flexpoint.core.plugin.*;
import com.flexpoint.core.plugin.official.monitor.subscriber.MonitorEventSubscriber;

import java.util.EnumSet;
import java.util.List;

/**
 * 官方内置：可观测性插件（融合 Event 订阅 + Monitor 处理链）。
 * - 向 ExtMonitor 注入默认处理链（Metrics/Alert）；
 * - 订阅实例级 EventBus，将调用与异常事件转发给 ExtMonitor。
 */
public final class ObservabilityPlugin implements Plugin {

    public static final String PLUGIN_ID = "core.observability";

    private final PluginDescriptor descriptor = PluginDescriptor.builder(PLUGIN_ID, "1.0.0")
            .capabilities(EnumSet.of(PluginCapability.EVENT, PluginCapability.MONITOR))
            .order(12)
            .critical(false)
            .build();

    private ExtMonitor monitor;
    private PluginMetricsHandler metricsHandler;
    private PluginAlertHandler alertHandler;
    private PluginCollectorHandler collectorHandler;
    private EventBus eventBus;
    private MonitorEventSubscriber subscriber;
    private List<AlertStrategy> alertStrategies;
    private List<MetricsCollector> collectors;

    public ObservabilityPlugin() {}

    public ObservabilityPlugin(List<AlertStrategy> alertStrategies,
                               List<MetricsCollector> collectors) {
        this.alertStrategies = alertStrategies;
        this.collectors = collectors;
    }

    @Override
    public PluginDescriptor getDescriptor() { return descriptor; }

    @Override
    public void init(PluginContext context) {
        this.monitor = context.monitor();
        if (this.monitor == null) {
            throw new IllegalStateException("ObservabilityPlugin requires ExtMonitor from PluginContext");
        }
        this.metricsHandler = new PluginMetricsHandler();
        this.alertHandler = new PluginAlertHandler(alertStrategies);
        this.eventBus = context.eventBus();
        this.subscriber = new MonitorEventSubscriber(monitor);
    }

    @Override
    public void start() {
        monitor.addHandler(metricsHandler);
        if (collectors != null && !collectors.isEmpty()) {
            this.collectorHandler = new PluginCollectorHandler(collectors);
            monitor.addHandler(this.collectorHandler);
        }
        monitor.addHandler(alertHandler);
        eventBus.subscribe(subscriber);
    }

    @Override
    public void stop() {
        if (eventBus != null && subscriber != null) eventBus.unsubscribe(subscriber);
        if (monitor != null) {
            if (metricsHandler != null) monitor.removeHandler(metricsHandler);
            if (collectorHandler != null) monitor.removeHandler(collectorHandler);
            if (alertHandler != null) monitor.removeHandler(alertHandler);
        }
    }

    @Override
    public void destroy() {
        this.subscriber = null;
        this.eventBus = null;
        this.alertHandler = null;
        this.metricsHandler = null;
        this.monitor = null;
    }
}
