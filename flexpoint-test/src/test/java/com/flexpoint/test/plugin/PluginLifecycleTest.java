package com.flexpoint.test.plugin;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.event.DefaultEventBus;
import com.flexpoint.core.event.EventDispatcher;
import com.flexpoint.core.ext.DefaultExtAbilityRegistry;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.monitor.MonitorFactory;
import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.plugin.PluginState;
import com.flexpoint.core.plugin.manage.DefaultPluginManager;
import com.flexpoint.core.selector.DefaultSelectorRegistry;
import com.flexpoint.core.selector.SelectorRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 插件生命周期状态机与编排顺序用例。
 *
 * <p>覆盖：CREATED -> INITIALIZED -> STARTED -> STOPPED -> DESTROYED 的状态流转，
 * 以及安装按注册顺序、停止按逆序的编排约束。</p>
 *
 * @author xiangganluo
 */
public class PluginLifecycleTest {

    private DefaultPluginManager newManager() {
        FlexPointConfig config = FlexPointConfig.defaultConfig();
        EventDispatcher dispatcher = new EventDispatcher(new DefaultEventBus());
        ExtAbilityRegistry registry = new DefaultExtAbilityRegistry(config.getRegistry(), dispatcher);
        SelectorRegistry selectorRegistry = new DefaultSelectorRegistry(dispatcher);
        ExtMonitor monitor = MonitorFactory.createDefaultMonitor(config.getMonitor());
        return new DefaultPluginManager(registry, selectorRegistry, dispatcher.getEventBus(), monitor, config);
    }

    /** 记录自身生命周期事件到共享列表。 */
    static class RecordingPlugin extends AbstractPlugin {
        private final String id;
        private final List<String> trace;
        RecordingPlugin(String id, List<String> trace) { this.id = id; this.trace = trace; }
        @Override public String getId() { return id; }
        @Override public void init(PluginContext ctx) { trace.add(id + ":init"); }
        @Override public void start() { trace.add(id + ":start"); }
        @Override public void stop() { trace.add(id + ":stop"); }
        @Override public void destroy() { trace.add(id + ":destroy"); }
    }

    @Test
    void state_transitions_from_created_to_started_then_destroyed() {
        DefaultPluginManager pm = newManager();
        List<String> trace = new ArrayList<>();
        pm.register(new RecordingPlugin("p", trace));

        // 注册后即为 CREATED
        Assertions.assertEquals(PluginState.CREATED, pm.getPluginStates().get("p"));

        pm.installAll();
        Assertions.assertEquals(PluginState.STARTED, pm.getPluginStates().get("p"));

        pm.stopAll();
        Assertions.assertEquals(PluginState.DESTROYED, pm.getPluginStates().get("p"));

        Assertions.assertEquals(Arrays.asList("p:init", "p:start", "p:stop", "p:destroy"), trace);
    }

    @Test
    void install_registration_order_and_stop_reverse_order() {
        DefaultPluginManager pm = newManager();
        List<String> trace = new ArrayList<>();
        // 注册顺序 a -> b -> c
        pm.register(new RecordingPlugin("a", trace));
        pm.register(new RecordingPlugin("b", trace));
        pm.register(new RecordingPlugin("c", trace));
        pm.installAll();
        pm.stopAll();

        // 安装顺序 a,b,c；停止顺序 c,b,a
        Assertions.assertEquals(Arrays.asList(
                "a:init", "a:start",
                "b:init", "b:start",
                "c:init", "c:start",
                "c:stop", "c:destroy",
                "b:stop", "b:destroy",
                "a:stop", "a:destroy"
        ), trace);
    }
}
