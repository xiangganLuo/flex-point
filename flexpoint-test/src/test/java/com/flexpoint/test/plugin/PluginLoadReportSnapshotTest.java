package com.flexpoint.test.plugin;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.plugin.PluginLoadReport;
import com.flexpoint.core.plugin.PluginState;
import com.flexpoint.core.selector.SelectionResult;
import com.flexpoint.core.selector.Selector;
import com.flexpoint.core.selector.SelectorRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 修复 5 回归：getLoadReport 返回不可变快照，与内部可变状态解耦。
 *
 * @author xiangganluo
 */
public class PluginLoadReportSnapshotTest {

    static class SimplePlugin extends AbstractPlugin {
        static final String ID = "test.snapshot";
        static final String NAME = "snapshotSel";
        private SelectorRegistry registry;
        private final Selector selector = new Selector() {
            @Override public String getName() { return NAME; }
            @Override public <T extends ExtAbility> SelectionResult<T> select(List<T> c) {
                return SelectionResult.of(getName(), c, c.isEmpty() ? null : c.get(0));
            }
        };
        @Override public String getId() { return ID; }
        @Override public void init(PluginContext ctx) { this.registry = ctx.selectorRegistry(); }
        @Override public void start() { registry.register(selector); }
        @Override public void stop() { registry.unregister(NAME); }
    }

    @Test
    void snapshot_is_immutable() {
        PluginLoadReport report = new PluginLoadReport();
        report.addOrdered("a");
        report.setState("a", PluginState.STARTED);

        PluginLoadReport snap = report.snapshot();
        Assertions.assertEquals(PluginState.STARTED, snap.getStates().get("a"));

        // 快照的返回集合不可变
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> snap.getStates().put("x", PluginState.FAILED));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> snap.getOrderedPluginIds().add("x"));
    }

    @Test
    void snapshot_is_decoupled_from_later_mutations() {
        PluginLoadReport report = new PluginLoadReport();
        report.addOrdered("a");
        report.setState("a", PluginState.STARTED);

        PluginLoadReport snap = report.snapshot();

        // 快照后再改动原始 report，不应影响已取到的快照
        report.setState("a", PluginState.FAILED);
        report.addOrdered("b");

        Assertions.assertEquals(PluginState.STARTED, snap.getStates().get("a"));
        Assertions.assertFalse(snap.getOrderedPluginIds().contains("b"));
    }

    @Test
    void flexpoint_getPluginLoadReport_returns_decoupled_snapshot() {
        List<Plugin> ps = new ArrayList<>();
        ps.add(new SimplePlugin());
        FlexPoint fp = FlexPointBuilder.create().withPlugins(ps).build();

        PluginLoadReport before = fp.getPluginLoadReport();
        Assertions.assertEquals(PluginState.STARTED, before.getStates().get(SimplePlugin.ID));

        // 运行期停用后，先前取得的报告快照不应发生变化
        fp.disablePlugin(SimplePlugin.ID);
        Assertions.assertEquals(PluginState.STARTED, before.getStates().get(SimplePlugin.ID),
                "先前取得的报告应为独立快照，不随后续状态变化");

        PluginLoadReport after = fp.getPluginLoadReport();
        Assertions.assertEquals(PluginState.STOPPED, after.getStates().get(SimplePlugin.ID));

        fp.shutdown();
    }
}
