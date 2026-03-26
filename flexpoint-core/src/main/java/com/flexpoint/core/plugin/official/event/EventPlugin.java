package com.flexpoint.core.plugin.official.event;

import com.flexpoint.core.event.EventBus;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.plugin.*;
import com.flexpoint.core.plugin.official.monitor.subscriber.MonitorEventSubscriber;

import java.util.EnumSet;

/**
 * 官方内置：事件插件（最小示例）。
 *
 * <p>当前示例将 {@link MonitorEventSubscriber} 订阅到实例级 {@link EventBus}，
 * 将调用/异常事件转发到 {@link ExtMonitor}。后续可以替换为其他“玩法”。</p>
 */
public final class EventPlugin implements Plugin {

    public static final String PLUGIN_ID = "core.event.monitor-subscriber";

    private final PluginDescriptor descriptor = PluginDescriptor.builder(PLUGIN_ID, "1.0.1")
            .capabilities(EnumSet.of(PluginCapability.EVENT))
            .order(20)
            .critical(false)
            .build();

    private EventBus eventBus;
    private ExtMonitor monitor;
    private MonitorEventSubscriber subscriber;
    private com.flexpoint.core.plugin.official.event.subscriber.AuditEventSubscriber audit;

    @Override
    public PluginDescriptor getDescriptor() { return descriptor; }

    @Override
    public void init(PluginContext context) {
        this.eventBus = context.eventBus();
        this.monitor = context.monitor();
        this.subscriber = new MonitorEventSubscriber(monitor);
        this.audit = new com.flexpoint.core.plugin.official.event.subscriber.AuditEventSubscriber();
    }

    @Override
    public void start() { eventBus.subscribe(subscriber); eventBus.subscribe(audit); }

    @Override
    public void stop() { if (eventBus != null) { if (subscriber != null) eventBus.unsubscribe(subscriber); if (audit != null) eventBus.unsubscribe(audit);} }

    @Override
    public void destroy() { this.audit = null; this.subscriber = null; this.monitor = null; this.eventBus = null; }
}
