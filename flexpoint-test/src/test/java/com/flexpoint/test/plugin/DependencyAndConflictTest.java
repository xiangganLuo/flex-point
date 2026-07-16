package com.flexpoint.test.plugin;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.event.DefaultEventBus;
import com.flexpoint.core.event.EventDispatcher;
import com.flexpoint.core.ext.DefaultExtAbilityRegistry;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.monitor.MonitorFactory;
import com.flexpoint.core.plugin.*;
import com.flexpoint.core.plugin.exception.PluginDependencyException;
import com.flexpoint.core.plugin.exception.PluginException;
import com.flexpoint.core.plugin.manage.DefaultPluginManager;
import com.flexpoint.core.selector.DefaultSelectorRegistry;
import com.flexpoint.core.selector.Selector;
import com.flexpoint.core.selector.SelectorRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * 依赖解析 / 顺序 / 冲突（资源级唯一）用例。
 *
 * <p>覆盖 Phase B 任务 P2：依赖顺序稳定性、缺失依赖、循环依赖、重复 ID、
 * 资源级唯一（选择器同名禁止覆盖）、关键/非关键插件失败差异化处理。</p>
 *
 * @author xiangganluo
 */
public class DependencyAndConflictTest {

    // =============== 测试脚手架 ===============

    private DefaultPluginManager newManager() {
        FlexPointConfig config = FlexPointConfig.defaultConfig();
        EventDispatcher dispatcher = new EventDispatcher(new DefaultEventBus());
        ExtAbilityRegistry registry = new DefaultExtAbilityRegistry(config.getRegistry(), dispatcher);
        SelectorRegistry selectorRegistry = new DefaultSelectorRegistry(dispatcher);
        ExtMonitor monitor = MonitorFactory.createDefaultMonitor(config.getMonitor());
        return new DefaultPluginManager(registry, selectorRegistry, dispatcher.getEventBus(), monitor, config);
    }

    /** 仅声明元数据、生命周期为空的最简插件。 */
    static class NoopPlugin extends AbstractPlugin {
        private final PluginDescriptor descriptor;
        NoopPlugin(String id, int order, List<PluginDependency> deps, boolean critical) {
            PluginDescriptor.Builder b = PluginDescriptor.builder(id, "1.0.0")
                    .capabilities(EnumSet.of(PluginCapability.OTHER))
                    .order(order)
                    .critical(critical);
            if (deps != null) b.dependencies(deps);
            this.descriptor = b.build();
        }
        @Override public PluginDescriptor getDescriptor() { return descriptor; }
    }

    /** 注册一个固定名称选择器的插件（用于资源级唯一校验）。 */
    static class NamedSelectorPlugin extends AbstractPlugin {
        private final PluginDescriptor descriptor;
        private final String selectorName;
        private SelectorRegistry registry;
        NamedSelectorPlugin(String id, String selectorName, int order) {
            this.selectorName = selectorName;
            this.descriptor = PluginDescriptor.builder(id, "1.0.0")
                    .capabilities(EnumSet.of(PluginCapability.SELECTOR))
                    .order(order)
                    .critical(false)
                    .build();
        }
        @Override public PluginDescriptor getDescriptor() { return descriptor; }
        @Override public void init(PluginContext ctx) { this.registry = ctx.selectorRegistry(); }
        @Override public void start() {
            registry.register(new Selector() {
                @Override public <T extends ExtAbility> T select(List<T> candidates) { return null; }
                @Override public String getName() { return selectorName; }
            });
        }
    }

    static class CriticalFailingPlugin extends AbstractPlugin {
        private final PluginDescriptor descriptor;
        CriticalFailingPlugin(String id) {
            this.descriptor = PluginDescriptor.builder(id, "1.0.0")
                    .capabilities(EnumSet.of(PluginCapability.OTHER))
                    .critical(true)
                    .build();
        }
        @Override public PluginDescriptor getDescriptor() { return descriptor; }
        @Override public void start() { throw new IllegalStateException("critical boom"); }
    }

    static class NonCriticalFailingPlugin extends AbstractPlugin {
        private final PluginDescriptor descriptor;
        NonCriticalFailingPlugin(String id) {
            this.descriptor = PluginDescriptor.builder(id, "1.0.0")
                    .capabilities(EnumSet.of(PluginCapability.OTHER))
                    .critical(false)
                    .order(99)
                    .build();
        }
        @Override public PluginDescriptor getDescriptor() { return descriptor; }
        @Override public void start() { throw new IllegalStateException("optional boom"); }
    }

    private static List<PluginDependency> depsOn(String... ids) {
        List<PluginDependency> deps = new ArrayList<>();
        for (String id : ids) deps.add(new PluginDependency(id, null));
        return deps;
    }

    // =============== 依赖顺序 ===============

    /**
     * 回归测试：当被依赖者的 order 大于依赖者时，仍必须保证依赖顺序。
     * dep(order=100) 被 main(order=1) 依赖 => 结果必须为 [dep, main]。
     */
    @Test
    void dependency_order_must_win_over_order_field() {
        DefaultPluginManager pm = newManager();
        pm.register(new NoopPlugin("main", 1, depsOn("dep"), false));
        pm.register(new NoopPlugin("dep", 100, null, false));
        pm.resolve();

        List<String> order = pm.getLoadReport().getOrderedPluginIds();
        Assertions.assertEquals(Arrays.asList("dep", "main"), order,
                "被依赖插件必须先于依赖方装配，order 字段不得破坏拓扑顺序");
    }

    /**
     * 无相互依赖的插件之间，按 order 升序装配；order 相同按 id 字典序兜底。
     */
    @Test
    void order_is_tiebreak_for_independent_plugins() {
        DefaultPluginManager pm = newManager();
        pm.register(new NoopPlugin("a", 5, null, false));
        pm.register(new NoopPlugin("b", 1, null, false));
        pm.register(new NoopPlugin("c", 3, null, false));
        pm.resolve();

        Assertions.assertEquals(Arrays.asList("b", "c", "a"), pm.getLoadReport().getOrderedPluginIds());
    }

    @Test
    void diamond_dependency_resolves_in_topological_order() {
        // a <- b, a <- c, (b,c) <- d
        DefaultPluginManager pm = newManager();
        pm.register(new NoopPlugin("d", 5, depsOn("b", "c"), false));
        pm.register(new NoopPlugin("b", 1, depsOn("a"), false));
        pm.register(new NoopPlugin("c", 0, depsOn("a"), false));
        pm.register(new NoopPlugin("a", 0, null, false));
        pm.resolve();

        List<String> order = pm.getLoadReport().getOrderedPluginIds();
        Assertions.assertEquals("a", order.get(0));
        Assertions.assertEquals("d", order.get(3));
        Assertions.assertTrue(order.indexOf("b") < order.indexOf("d"));
        Assertions.assertTrue(order.indexOf("c") < order.indexOf("d"));
    }

    // =============== 依赖异常 ===============

    @Test
    void missing_dependency_throws() {
        DefaultPluginManager pm = newManager();
        pm.register(new NoopPlugin("main", 0, depsOn("absent"), false));
        Assertions.assertThrows(PluginDependencyException.class, pm::resolve);
    }

    @Test
    void cyclic_dependency_throws() {
        DefaultPluginManager pm = newManager();
        pm.register(new NoopPlugin("x", 0, depsOn("y"), false));
        pm.register(new NoopPlugin("y", 0, depsOn("x"), false));
        Assertions.assertThrows(PluginDependencyException.class, pm::resolve);
    }

    @Test
    void duplicate_plugin_id_throws_on_register() {
        DefaultPluginManager pm = newManager();
        pm.register(new NoopPlugin("dup", 0, null, false));
        Assertions.assertThrows(PluginException.class,
                () -> pm.register(new NoopPlugin("dup", 0, null, false)));
    }

    // =============== 资源级唯一 ===============

    @Test
    void duplicate_selector_name_marks_second_plugin_failed() {
        DefaultPluginManager pm = newManager();
        pm.register(new NamedSelectorPlugin("sel.a", "sameName", 0));
        pm.register(new NamedSelectorPlugin("sel.b", "sameName", 1));
        pm.resolve();
        pm.installAll();

        Assertions.assertEquals(PluginState.STARTED, pm.getPluginStates().get("sel.a"));
        Assertions.assertEquals(PluginState.FAILED, pm.getPluginStates().get("sel.b"),
                "同名选择器的第二个插件应装配失败并被标记 FAILED");
        Assertions.assertTrue(pm.getLoadReport().getErrors().containsKey("sel.b"));
    }

    // =============== 关键 / 非关键失败差异化 ===============

    @Test
    void critical_plugin_failure_blocks_install() {
        DefaultPluginManager pm = newManager();
        pm.register(new CriticalFailingPlugin("must.have"));
        pm.resolve();
        Assertions.assertThrows(PluginException.class, pm::installAll);
    }

    @Test
    void non_critical_failure_does_not_block_others() {
        DefaultPluginManager pm = newManager();
        pm.register(new NamedSelectorPlugin("sel.ok", "okName", 0));
        pm.register(new NonCriticalFailingPlugin("opt.fail"));
        pm.resolve();
        pm.installAll();

        Assertions.assertEquals(PluginState.STARTED, pm.getPluginStates().get("sel.ok"));
        Assertions.assertEquals(PluginState.FAILED, pm.getPluginStates().get("opt.fail"));
    }
}
