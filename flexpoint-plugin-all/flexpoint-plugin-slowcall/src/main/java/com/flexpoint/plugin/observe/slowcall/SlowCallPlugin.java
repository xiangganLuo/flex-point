package com.flexpoint.plugin.observe.slowcall;

import com.flexpoint.core.event.EventBus;
import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 官方内置：慢调用告警插件。
 *
 * <p>订阅实例级 EventBus 的 INVOKE_SUCCESS/INVOKE_FAIL 事件，当调用耗时超过
 * {@code thresholdMs} 时输出 warn 告警日志（含 extId/method/duration/阈值），
 * 并回调可选的 {@link SlowCallListener} 以便扩展。</p>
 *
 * <p>构造参数：{@code thresholdMs}（默认 200 毫秒）、可选 {@link SlowCallListener}。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class SlowCallPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "observe.slowcall";

    /** 默认慢调用阈值（毫秒）。 */
    public static final long DEFAULT_THRESHOLD_MS = 200L;

    private final long thresholdMs;
    private final SlowCallListener listener;

    private EventBus eventBus;
    private SlowCallSubscriber subscriber;

    public SlowCallPlugin() {
        this(DEFAULT_THRESHOLD_MS, null);
    }

    public SlowCallPlugin(long thresholdMs) {
        this(thresholdMs, null);
    }

    public SlowCallPlugin(long thresholdMs, SlowCallListener listener) {
        this.thresholdMs = thresholdMs;
        this.listener = listener;
    }

    @Override
    public String getId() {
        return PLUGIN_ID;
    }

    @Override
    public void init(PluginContext context) {
        this.eventBus = context.eventBus();
        if (this.eventBus == null) {
            throw new IllegalStateException("SlowCallPlugin requires EventBus from PluginContext");
        }
        this.subscriber = new SlowCallSubscriber(thresholdMs, listener);
        log.debug("[{}] init: thresholdMs={}, listener={}", PLUGIN_ID, thresholdMs, listener != null);
    }

    @Override
    public void start() {
        eventBus.subscribe(subscriber);
        log.debug("[{}] start: 已订阅事件总线，阈值={}ms", PLUGIN_ID, thresholdMs);
    }

    @Override
    public void stop() {
        if (eventBus != null && subscriber != null) {
            eventBus.unsubscribe(subscriber);
        }
        log.debug("[{}] stop: 已反订阅事件总线", PLUGIN_ID);
    }

    @Override
    public void destroy() {
        this.subscriber = null;
        this.eventBus = null;
    }
}
