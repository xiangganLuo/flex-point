package com.flexpoint.test.plugin;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.common.constants.FlexPointConstants;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.ExtMetrics;
import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.PluginState;
import com.flexpoint.plugin.observability.ObservabilityPlugin;
import com.flexpoint.plugin.selector.code.CodeSelectorPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 官方内置插件装配生效用例（Phase B E2）。
 *
 * <p>验证：ObservabilityPlugin 注入监控处理链并订阅事件（调用后指标累计）；
 * CodeSelectorPlugin 注册选择器；停用观测插件后核心选择能力仍可用（降级）。</p>
 *
 * @author xiangganluo
 */
public class OfficialPluginAssemblyTest {

    @FpSelector(FlexPointConstants.CODE_SELECTOR_NAME)
    interface OrderAbility extends ExtAbility {
        String run();
    }

    static class OrderA implements OrderAbility {
        @Override public String getCode() { return "a"; }
        @Override public String run() { return "A"; }
    }

    private FlexPoint buildWithOfficialPlugins() {
        List<Plugin> ps = new ArrayList<>();
        ps.add(new ObservabilityPlugin());
        ps.add(new CodeSelectorPlugin(() -> "a"));
        return FlexPointBuilder.create().withPlugins(ps).build();
    }

    @Test
    void selector_plugin_registers_selector_and_selection_works() {
        FlexPoint fp = buildWithOfficialPlugins();
        Assertions.assertTrue(fp.hasSelector(FlexPointConstants.CODE_SELECTOR_NAME));

        fp.register(new OrderA());
        OrderAbility found = fp.findAbility(OrderAbility.class);
        Assertions.assertNotNull(found);
        Assertions.assertEquals("A", found.run());

        fp.shutdown();
    }

    @Test
    void observability_plugin_records_metrics_on_invocation() {
        FlexPoint fp = buildWithOfficialPlugins();
        OrderA a = new OrderA();
        fp.register(a);

        OrderAbility found = fp.findAbility(OrderAbility.class);
        found.run();
        found.run();

        ExtMetrics metrics = fp.getExtMetrics(a);
        Assertions.assertNotNull(metrics, "ObservabilityPlugin 应注入 MetricsProvider");
        Assertions.assertEquals(2, metrics.getTotalInvocations());
        Assertions.assertEquals(2, metrics.getSuccessInvocations());

        fp.shutdown();
    }

    @Test
    void disabling_observability_does_not_break_selection() {
        FlexPoint fp = buildWithOfficialPlugins();
        fp.register(new OrderA());

        // 停用观测插件
        fp.disablePlugin(ObservabilityPlugin.PLUGIN_ID);
        Assertions.assertEquals(PluginState.STOPPED, fp.getPluginStates().get(ObservabilityPlugin.PLUGIN_ID));

        // 核心选择能力不受影响（降级）
        OrderAbility found = fp.findAbility(OrderAbility.class);
        Assertions.assertNotNull(found);
        Assertions.assertEquals("A", found.run());

        fp.shutdown();
    }
}
