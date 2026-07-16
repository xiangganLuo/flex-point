package com.flexpoint.plugin.observe.audit;

import com.flexpoint.core.event.EventBus;
import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 官方内置：审计日志插件。
 *
 * <p>订阅实例级 EventBus，将扩展点「选择(EXT_SELECTED/EXT_SELECTION_FAILED)」与
 * 「调用(INVOKE_SUCCESS/INVOKE_FAIL/INVOKE_EXCEPTION)」事件以结构化单行日志输出。</p>
 *
 * <p>构造参数可选：{@code logSelection} 是否记录选择事件、{@code logInvocation} 是否记录
 * 调用事件，默认均开启。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class AuditLogPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "observe.audit";

    private final boolean logSelection;
    private final boolean logInvocation;

    private EventBus eventBus;
    private AuditLogSubscriber subscriber;

    public AuditLogPlugin() {
        this(true, true);
    }

    public AuditLogPlugin(boolean logSelection, boolean logInvocation) {
        this.logSelection = logSelection;
        this.logInvocation = logInvocation;
    }

    @Override
    public String getId() {
        return PLUGIN_ID;
    }

    @Override
    public void init(PluginContext context) {
        this.eventBus = context.eventBus();
        if (this.eventBus == null) {
            throw new IllegalStateException("AuditLogPlugin requires EventBus from PluginContext");
        }
        this.subscriber = new AuditLogSubscriber(logSelection, logInvocation);
        log.debug("[{}] init: logSelection={}, logInvocation={}", PLUGIN_ID, logSelection, logInvocation);
    }

    @Override
    public void start() {
        eventBus.subscribe(subscriber);
        log.debug("[{}] start: 已订阅事件总线", PLUGIN_ID);
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
