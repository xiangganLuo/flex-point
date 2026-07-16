package com.flexpoint.plugin.observability;

import com.flexpoint.core.event.EventBus;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.plugin.observability.alert.AlertStrategy;
import com.flexpoint.plugin.observability.handler.PluginAlertHandler;
import com.flexpoint.plugin.observability.handler.PluginCollectorHandler;
import com.flexpoint.plugin.observability.handler.PluginMetricsHandler;
import com.flexpoint.plugin.observability.metrics.MetricsCollector;
import com.flexpoint.plugin.observability.subscriber.MonitorEventSubscriber;

import java.util.List;

/**
 * 官方内置：可观测性插件（融合 Event 订阅 + Monitor 处理链）。
 * <ul>
 *   <li>向 ExtMonitor 注入默认处理链（Metrics/可选 Collector/Alert）；</li>
 *   <li>订阅实例级 EventBus，将调用与异常事件转发给 ExtMonitor。</li>
 * </ul>
 *
 * @author xiangganluo
 */
public final class ObservabilityPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "core.observability";

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
    public String getId() {
        return PLUGIN_ID;
    }

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
        this.collectorHandler = null;
        this.monitor = null;
    }
}
