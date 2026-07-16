package com.flexpoint.test.plugin;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.event.DefaultEventBus;
import com.flexpoint.core.event.EventBus;
import com.flexpoint.core.event.EventDispatcher;
import com.flexpoint.core.ext.DefaultExtAbilityRegistry;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.monitor.MonitorFactory;
import com.flexpoint.core.plugin.*;
import com.flexpoint.core.plugin.manage.DefaultPluginManager;
import com.flexpoint.core.selector.DefaultSelectorRegistry;
import com.flexpoint.core.selector.SelectorRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

/**
 * 插件上下文能力边界用例（覆盖 Phase B 任务 A2）。
 *
 * <p>验证 {@link PluginContext} 向插件暴露的受控能力（registry/selector/event/monitor/config）
 * 与内核装配的实例一致，且插件可通过上下文注册自身能力。</p>
 *
 * @author xiangganluo
 */
public class PluginContextBoundaryTest {

    static class CapturingPlugin extends AbstractPlugin {
        PluginContext captured;
        private final PluginDescriptor descriptor = PluginDescriptor.builder("test.capture", "1.0.0")
                .capabilities(EnumSet.of(PluginCapability.OTHER))
                .build();
        @Override public PluginDescriptor getDescriptor() { return descriptor; }
        @Override public void init(PluginContext ctx) { this.captured = ctx; }
    }

    @Test
    void context_exposes_wired_kernel_capabilities() {
        FlexPointConfig config = FlexPointConfig.defaultConfig();
        EventDispatcher dispatcher = new EventDispatcher(new DefaultEventBus());
        ExtAbilityRegistry registry = new DefaultExtAbilityRegistry(config.getRegistry(), dispatcher);
        SelectorRegistry selectorRegistry = new DefaultSelectorRegistry(dispatcher);
        ExtMonitor monitor = MonitorFactory.createDefaultMonitor(config.getMonitor());
        EventBus eventBus = dispatcher.getEventBus();

        DefaultPluginManager pm = new DefaultPluginManager(registry, selectorRegistry, eventBus, monitor, config);
        CapturingPlugin plugin = new CapturingPlugin();
        pm.register(plugin);
        pm.resolve();
        pm.installAll();

        PluginContext ctx = plugin.captured;
        Assertions.assertNotNull(ctx, "init 阶段应收到受控上下文");
        Assertions.assertSame(registry, ctx.extRegistry());
        Assertions.assertSame(selectorRegistry, ctx.selectorRegistry());
        Assertions.assertSame(eventBus, ctx.eventBus());
        Assertions.assertSame(monitor, ctx.monitor());
        Assertions.assertSame(config, ctx.config());
    }
}
