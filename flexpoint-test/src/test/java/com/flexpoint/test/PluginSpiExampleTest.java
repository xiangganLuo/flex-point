package com.flexpoint.test;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.plugin.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * 插件 SPI 最小示例与用例：
 * - 通过插件注册选择器并装配到 FlexPoint
 * - 演示插件依赖顺序
 * - 演示非关键插件失败的降级
 */
public class PluginSpiExampleTest {

    // =============== 示例扩展点定义与实现 ===============
    @FpSelector("PluginSelector")
    interface PluginDemoAbility extends ExtAbility {
        String hi();
    }

    static class PluginAbility implements PluginDemoAbility {
        @Override public String getCode() { return "plugin"; }
        @Override public String hi() { return "plugin-ok"; }
    }

    static class OtherAbility implements PluginDemoAbility {
        @Override public String getCode() { return "other"; }
        @Override public String hi() { return "other"; }
    }

    // =============== 示例插件实现 ===============
    static class TestSelectorPlugin extends AbstractPlugin {
        private PluginContext ctx;
        @Override public void init(PluginContext context) { this.ctx = context; }
        @Override public void start() {
            // 简单选择器：固定选择 code = "plugin" 的实现
            ctx.selectorRegistry().register(new com.flexpoint.core.selector.Selector() {
                @Override
                public <T extends ExtAbility> T select(List<T> candidates) {
                    for (T c : candidates) { if ("plugin".equals(c.getCode())) return c; }
                    return null;
                }

                @Override public String getName() { return "PluginSelector"; }
            });
        }
        @Override public PluginDescriptor getDescriptor() {
            return PluginDescriptor.builder("test.selector", "1.0.0")
                    .capabilities(EnumSet.of(PluginCapability.SELECTOR))
                    .order(0)
                    .critical(false)
                    .build();
        }
    }

    static class DependantPlugin extends AbstractPlugin {
        static final List<String> ORDER = new ArrayList<String>();
        @Override public void start() { ORDER.add("dep"); }
        @Override public void init(PluginContext context) { ORDER.add("selector"); }
        @Override public PluginDescriptor getDescriptor() {
            List<PluginDependency> deps = new ArrayList<PluginDependency>();
            deps.add(new PluginDependency("test.selector", null));
            return PluginDescriptor.builder("test.dependant", "1.0.0")
                    .capabilities(EnumSet.of(PluginCapability.OTHER))
                    .dependencies(deps)
                    .order(1)
                    .build();
        }
    }

    static class FailingOptionalPlugin extends AbstractPlugin {
        @Override public void start() { throw new IllegalStateException("boom"); }
        @Override public PluginDescriptor getDescriptor() {
            return PluginDescriptor.builder("test.fail.opt", "1.0.0")
                    .capabilities(EnumSet.of(PluginCapability.OTHER))
                    .critical(false)
                    .order(2)
                    .build();
        }
    }

    @Test
    void should_select_ability_via_plugin_selector() {
        List<Plugin> ps = new ArrayList<Plugin>();
        ps.add(new TestSelectorPlugin());
        FlexPoint fp = FlexPointBuilder.create()
                .withPlugins(ps)
                .build();

        fp.register(new PluginAbility());
        fp.register(new OtherAbility());

        PluginDemoAbility a = fp.findAbility(PluginDemoAbility.class);
        Assertions.assertNotNull(a);
        Assertions.assertEquals("plugin-ok", a.hi());
    }

    @Test
    void should_respect_dependency_order() {
        DependantPlugin.ORDER.clear();
        List<Plugin> ps = new ArrayList<Plugin>();
        ps.add(new TestSelectorPlugin());
        ps.add(new DependantPlugin());
        FlexPointBuilder.create()
                .withPlugins(ps)
                .build();
        // init(selector) 先于 dependant.start()
        List<String> expect = new ArrayList<String>();
        expect.add("selector");
        expect.add("dep");
        Assertions.assertEquals(expect, DependantPlugin.ORDER);
    }

    @Test
    void non_critical_plugin_failure_should_not_block_build() {
        List<Plugin> ps = new ArrayList<Plugin>();
        ps.add(new TestSelectorPlugin());
        ps.add(new FailingOptionalPlugin());
        FlexPoint fp = FlexPointBuilder.create()
                .withPlugins(ps)
                .build();
        fp.register(new PluginAbility());
        PluginDemoAbility a = fp.findAbility(PluginDemoAbility.class);
        Assertions.assertEquals("plugin-ok", a.hi());
    }
}
