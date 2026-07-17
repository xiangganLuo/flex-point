package com.flexpoint.test.plugin;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.plugin.PluginState;
import com.flexpoint.core.selector.SelectionResult;
import com.flexpoint.core.selector.Selector;
import com.flexpoint.core.selector.SelectorRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 修复 2 回归：插件 init 中注册选择器、start 首次失败降级为 FAILED；
 * 随后 enable 应先做防御式清理（stop/destroy）再 init，避免「选择器名称已存在」而永久卡死。
 *
 * @author xiangganluo
 */
public class PluginEnableRecoveryTest {

    /** init 注册选择器；start 首次抛异常、第二次成功；stop 反注册选择器。 */
    static class FlakyStartPlugin extends AbstractPlugin {
        static final String ID = "test.flaky-start";
        static final String SELECTOR_NAME = "flakySelector";

        private final AtomicInteger startCount = new AtomicInteger();
        private SelectorRegistry registry;
        private final Selector selector = new Selector() {
            @Override public String getName() { return SELECTOR_NAME; }
            @Override public <T extends ExtAbility> SelectionResult<T> select(List<T> c) {
                T picked = c.isEmpty() ? null : c.get(0);
                return SelectionResult.of(getName(), c, picked);
            }
        };

        @Override public String getId() { return ID; }

        @Override public void init(PluginContext ctx) {
            this.registry = ctx.selectorRegistry();
            // 同名重复注册会抛 IllegalStateException，故 init 必须幂等（依赖 enable 前置清理）
            registry.register(selector);
        }

        @Override public void start() {
            if (startCount.incrementAndGet() == 1) {
                throw new IllegalStateException("首次启动故意失败");
            }
        }

        @Override public void stop() {
            registry.unregister(SELECTOR_NAME);
        }
    }

    private FlexPoint build() {
        List<Plugin> ps = new ArrayList<>();
        ps.add(new FlakyStartPlugin());
        return FlexPointBuilder.create().withPlugins(ps).build();
    }

    @Test
    void enable_from_failed_cleans_up_then_recovers() {
        FlexPoint fp = build();

        // 装配后：start 首次失败 -> FAILED，但选择器已在 init 阶段注册
        Assertions.assertEquals(PluginState.FAILED, fp.getPluginStates().get(FlakyStartPlugin.ID));
        Assertions.assertTrue(fp.hasSelector(FlakyStartPlugin.SELECTOR_NAME));

        // enable：不应因「选择器名称已存在」抛异常；应先清理再 init，第二次 start 成功
        Assertions.assertDoesNotThrow(() -> fp.enablePlugin(FlakyStartPlugin.ID));
        Assertions.assertEquals(PluginState.STARTED, fp.getPluginStates().get(FlakyStartPlugin.ID));
        Assertions.assertTrue(fp.hasSelector(FlakyStartPlugin.SELECTOR_NAME));

        fp.shutdown();
    }
}
