package com.flexpoint.test.plugin;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.plugin.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 验证 FlexPoint 持有 PluginManager 后的生命周期治理：
 * - build 后可观测插件状态与加载报告；
 * - shutdown 时逆序停止并销毁插件。
 *
 * @author xiangganluo
 */
public class FlexPointPluginLifecycleTest {

    static class TracePlugin extends AbstractPlugin {
        boolean started;
        boolean stopped;
        boolean destroyed;
        @Override public String getId() { return "test.trace"; }
        @Override public void start() { started = true; }
        @Override public void stop() { stopped = true; }
        @Override public void destroy() { destroyed = true; }
    }

    @Test
    void shutdown_stops_and_destroys_plugins_and_report_is_observable() {
        TracePlugin plugin = new TracePlugin();
        List<Plugin> plugins = new ArrayList<>();
        plugins.add(plugin);

        FlexPoint fp = FlexPointBuilder.create().withPlugins(plugins).build();

        // 装配后：STARTED，且加载报告可观测
        Assertions.assertTrue(plugin.started);
        Assertions.assertEquals(PluginState.STARTED, fp.getPluginStates().get("test.trace"));
        Assertions.assertNotNull(fp.getPluginLoadReport());
        Assertions.assertTrue(fp.getPluginLoadReport().getOrderedPluginIds().contains("test.trace"));

        fp.shutdown();

        // 关闭后：stop + destroy 均被调用，状态为 DESTROYED
        Assertions.assertTrue(plugin.stopped, "shutdown 应调用插件 stop()");
        Assertions.assertTrue(plugin.destroyed, "shutdown 应调用插件 destroy()");
        Assertions.assertEquals(PluginState.DESTROYED, fp.getPluginStates().get("test.trace"));
    }

    @Test
    void pure_kernel_without_plugins_has_empty_states() {
        FlexPoint fp = FlexPointBuilder.create().build();
        Assertions.assertTrue(fp.getPluginStates().isEmpty());
        Assertions.assertNull(fp.getPluginLoadReport());
        // 纯内核 shutdown 不应抛异常
        Assertions.assertDoesNotThrow(fp::shutdown);
    }
}
