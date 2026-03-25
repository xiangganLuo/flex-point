package com.flexpoint.core.plugin.manage;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.event.EventBus;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.selector.SelectorRegistry;

/**
 * 默认的受控上下文实现。
 *
 * <p>将内核能力以只读/受控方式暴露给插件。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
final class DefaultPluginContext implements PluginContext {
    private final ExtAbilityRegistry extRegistry;
    private final SelectorRegistry selectorRegistry;
    private final EventBus eventBus;
    private final ExtMonitor monitor;
    private final FlexPointConfig config;

    DefaultPluginContext(ExtAbilityRegistry extRegistry,
                         SelectorRegistry selectorRegistry,
                         EventBus eventBus,
                         ExtMonitor monitor,
                         FlexPointConfig config) {
        this.extRegistry = extRegistry;
        this.selectorRegistry = selectorRegistry;
        this.eventBus = eventBus;
        this.monitor = monitor;
        this.config = config;
    }

    @Override public ExtAbilityRegistry extRegistry() { return extRegistry; }
    @Override public SelectorRegistry selectorRegistry() { return selectorRegistry; }
    @Override public EventBus eventBus() { return eventBus; }
    @Override public ExtMonitor monitor() { return monitor; }
    @Override public FlexPointConfig config() { return config; }
}
