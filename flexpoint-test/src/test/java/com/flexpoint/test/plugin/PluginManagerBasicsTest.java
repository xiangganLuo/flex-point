package com.flexpoint.test.plugin;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.event.DefaultEventBus;
import com.flexpoint.core.event.EventDispatcher;
import com.flexpoint.core.ext.DefaultExtAbilityRegistry;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.monitor.MonitorFactory;
import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.plugin.PluginState;
import com.flexpoint.core.plugin.exception.PluginException;
import com.flexpoint.core.plugin.manage.DefaultPluginManager;
import com.flexpoint.core.selector.DefaultSelectorRegistry;
import com.flexpoint.core.selector.SelectionResult;
import com.flexpoint.core.selector.Selector;
import com.flexpoint.core.selector.SelectorRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 插件管理器基础语义用例（极简模型）：
 * - pluginId 唯一；
 * - 资源级唯一（选择器同名禁止覆盖 → 第二个插件降级 FAILED）；
 * - 任意插件失败统一降级，不阻断其它插件。
 *
 * @author xiangganluo
 */
public class PluginManagerBasicsTest {

    private DefaultPluginManager newManager() {
        FlexPointConfig config = FlexPointConfig.defaultConfig();
        EventDispatcher dispatcher = new EventDispatcher(new DefaultEventBus());
        ExtAbilityRegistry registry = new DefaultExtAbilityRegistry(config.getRegistry(), dispatcher);
        SelectorRegistry selectorRegistry = new DefaultSelectorRegistry(dispatcher);
        ExtMonitor monitor = MonitorFactory.createDefaultMonitor(config.getMonitor());
        return new DefaultPluginManager(registry, selectorRegistry, dispatcher.getEventBus(), monitor, config);
    }

    static class NoopPlugin extends AbstractPlugin {
        private final String id;
        NoopPlugin(String id) { this.id = id; }
        @Override public String getId() { return id; }
    }

    /** 注册固定名称选择器的插件。 */
    static class NamedSelectorPlugin extends AbstractPlugin {
        private final String id;
        private final String selectorName;
        private SelectorRegistry registry;
        NamedSelectorPlugin(String id, String selectorName) { this.id = id; this.selectorName = selectorName; }
        @Override public String getId() { return id; }
        @Override public void init(PluginContext ctx) { this.registry = ctx.selectorRegistry(); }
        @Override public void start() {
            registry.register(new Selector() {
                @Override public String getName() { return selectorName; }
                @Override public <T extends ExtAbility> SelectionResult<T> select(List<T> c) {
                    return SelectionResult.of(getName(), c, null);
                }
            });
        }
    }

    static class FailingPlugin extends AbstractPlugin {
        private final String id;
        FailingPlugin(String id) { this.id = id; }
        @Override public String getId() { return id; }
        @Override public void start() { throw new IllegalStateException("boom"); }
    }

    @Test
    void duplicate_plugin_id_throws_on_register() {
        DefaultPluginManager pm = newManager();
        pm.register(new NoopPlugin("dup"));
        Assertions.assertThrows(PluginException.class, () -> pm.register(new NoopPlugin("dup")));
    }

    @Test
    void empty_plugin_id_is_rejected() {
        DefaultPluginManager pm = newManager();
        Assertions.assertThrows(PluginException.class, () -> pm.register(new NoopPlugin("  ")));
    }

    @Test
    void duplicate_selector_name_degrades_second_plugin() {
        DefaultPluginManager pm = newManager();
        pm.register(new NamedSelectorPlugin("sel.a", "sameName"));
        pm.register(new NamedSelectorPlugin("sel.b", "sameName"));
        pm.installAll();

        Assertions.assertEquals(PluginState.STARTED, pm.getPluginStates().get("sel.a"));
        Assertions.assertEquals(PluginState.FAILED, pm.getPluginStates().get("sel.b"),
                "同名选择器的第二个插件应装配失败并被标记 FAILED");
        Assertions.assertTrue(pm.getLoadReport().getErrors().containsKey("sel.b"));
    }

    @Test
    void any_failure_is_degraded_and_does_not_block_others() {
        DefaultPluginManager pm = newManager();
        pm.register(new FailingPlugin("bad"));
        pm.register(new NamedSelectorPlugin("sel.ok", "okName"));
        pm.installAll();

        Assertions.assertEquals(PluginState.FAILED, pm.getPluginStates().get("bad"));
        Assertions.assertEquals(PluginState.STARTED, pm.getPluginStates().get("sel.ok"),
                "任意插件失败不应阻断后续插件装配");
    }

    @Test
    void install_order_equals_registration_order() {
        DefaultPluginManager pm = newManager();
        pm.register(new NoopPlugin("p1"));
        pm.register(new NoopPlugin("p2"));
        pm.register(new NoopPlugin("p3"));
        pm.installAll();

        Assertions.assertEquals(java.util.Arrays.asList("p1", "p2", "p3"),
                pm.getLoadReport().getOrderedPluginIds());
    }
}
