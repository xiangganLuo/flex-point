package com.flexpoint.test;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.plugin.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 插件 SPI 最小示例与用例（极简模型）：
 * - 通过插件注册选择器并装配到 FlexPoint；
 * - 演示装配顺序 = 注册顺序；
 * - 演示插件失败的降级。
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
        @Override public String getId() { return "test.selector"; }
        @Override public void init(PluginContext context) { this.ctx = context; }
        @Override public void start() {
            // 简单选择器：固定选择 code = "plugin" 的实现
            ctx.selectorRegistry().register(new com.flexpoint.core.selector.Selector() {
                @Override public String getName() { return "PluginSelector"; }

                @Override
                public <T extends ExtAbility> com.flexpoint.core.selector.SelectionResult<T> select(List<T> candidates) {
                    T picked = null;
                    for (T c : candidates) { if ("plugin".equals(c.getCode())) { picked = c; break; } }
                    return com.flexpoint.core.selector.SelectionResult.of(getName(), candidates, picked);
                }
            });
        }
    }

    static class SecondPlugin extends AbstractPlugin {
        static final List<String> ORDER = new ArrayList<String>();
        @Override public String getId() { return "test.second"; }
        @Override public void init(PluginContext context) { ORDER.add("selector"); }
        @Override public void start() { ORDER.add("dep"); }
    }

    static class FailingOptionalPlugin extends AbstractPlugin {
        @Override public String getId() { return "test.fail.opt"; }
        @Override public void start() { throw new IllegalStateException("boom"); }
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
    void should_install_in_registration_order() {
        SecondPlugin.ORDER.clear();
        List<Plugin> ps = new ArrayList<Plugin>();
        ps.add(new TestSelectorPlugin());
        ps.add(new SecondPlugin());
        FlexPointBuilder.create()
                .withPlugins(ps)
                .build();
        // TestSelectorPlugin 先装配（其 init/start 不记录），SecondPlugin 后装配
        List<String> expect = new ArrayList<String>();
        expect.add("selector");
        expect.add("dep");
        Assertions.assertEquals(expect, SecondPlugin.ORDER);
    }

    @Test
    void plugin_failure_should_not_block_build() {
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
