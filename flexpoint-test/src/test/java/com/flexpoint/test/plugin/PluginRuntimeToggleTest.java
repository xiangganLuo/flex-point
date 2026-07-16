package com.flexpoint.test.plugin;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.plugin.*;
import com.flexpoint.core.selector.DecisionExplanation;
import com.flexpoint.core.selector.Selector;
import com.flexpoint.core.selector.SelectorRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * 运行期插件启停（enable/disable）用例。
 *
 * <p>验证 disable 反注册能力并置 STOPPED、enable 重新装配并置 STARTED，
 * 以及启停的幂等性。</p>
 *
 * @author xiangganluo
 */
public class PluginRuntimeToggleTest {

    static class ToggleSelectorPlugin extends AbstractPlugin {
        static final String NAME = "toggleSel";
        static final String ID = "test.toggle";
        private final PluginDescriptor descriptor = PluginDescriptor.builder(ID, "1.0.0")
                .capabilities(EnumSet.of(PluginCapability.SELECTOR))
                .build();
        private SelectorRegistry registry;
        private final Selector selector = new Selector() {
            @Override public <T extends ExtAbility> T select(List<T> c) { return c.isEmpty() ? null : c.get(0); }
            @Override public String getName() { return NAME; }
            @Override public <T extends ExtAbility> DecisionExplanation explain(List<T> c) {
                return DecisionExplanation.fromSelection(getName(), c, select(c));
            }
        };
        @Override public PluginDescriptor getDescriptor() { return descriptor; }
        @Override public void init(PluginContext ctx) { this.registry = ctx.selectorRegistry(); }
        @Override public void start() { registry.register(selector); }
        @Override public void stop() { registry.unregister(NAME); }
    }

    private FlexPoint build() {
        List<Plugin> ps = new ArrayList<>();
        ps.add(new ToggleSelectorPlugin());
        return FlexPointBuilder.create().withPlugins(ps).build();
    }

    @Test
    void disable_then_enable_toggles_capability_and_state() {
        FlexPoint fp = build();

        // 初始：STARTED，选择器已注册
        Assertions.assertEquals(PluginState.STARTED, fp.getPluginStates().get(ToggleSelectorPlugin.ID));
        Assertions.assertTrue(fp.hasSelector(ToggleSelectorPlugin.NAME));

        // disable：反注册，状态 STOPPED
        fp.disablePlugin(ToggleSelectorPlugin.ID);
        Assertions.assertEquals(PluginState.STOPPED, fp.getPluginStates().get(ToggleSelectorPlugin.ID));
        Assertions.assertFalse(fp.hasSelector(ToggleSelectorPlugin.NAME));

        // enable：重新注册，状态 STARTED
        fp.enablePlugin(ToggleSelectorPlugin.ID);
        Assertions.assertEquals(PluginState.STARTED, fp.getPluginStates().get(ToggleSelectorPlugin.ID));
        Assertions.assertTrue(fp.hasSelector(ToggleSelectorPlugin.NAME));

        fp.shutdown();
    }

    @Test
    void toggle_is_idempotent() {
        FlexPoint fp = build();

        // 重复 enable（已 STARTED）不报错、保持 STARTED
        Assertions.assertDoesNotThrow(() -> fp.enablePlugin(ToggleSelectorPlugin.ID));
        Assertions.assertEquals(PluginState.STARTED, fp.getPluginStates().get(ToggleSelectorPlugin.ID));

        // 重复 disable（STOPPED 后再 disable）保持 STOPPED
        fp.disablePlugin(ToggleSelectorPlugin.ID);
        Assertions.assertDoesNotThrow(() -> fp.disablePlugin(ToggleSelectorPlugin.ID));
        Assertions.assertEquals(PluginState.STOPPED, fp.getPluginStates().get(ToggleSelectorPlugin.ID));

        fp.shutdown();
    }

    @Test
    void enable_unknown_plugin_is_noop() {
        FlexPoint fp = build();
        Assertions.assertDoesNotThrow(() -> fp.enablePlugin("does.not.exist"));
        Assertions.assertDoesNotThrow(() -> fp.disablePlugin("does.not.exist"));
        fp.shutdown();
    }
}
