package com.flexpoint.test;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.event.DefaultEventBus;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.selector.Selector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 第0阶段核心优化执行测试
 */
public class PhaseZeroExecutionTest {

    @FpSelector("phase0Selector")
    interface PhaseAbility extends ExtAbility {
    }

    static class PhaseAbilityA implements PhaseAbility {
        @Override
        public String getCode() {
            return "A";
        }
    }

    static class PhaseAbilityB implements PhaseAbility {
        @Override
        public String getCode() {
            return "B";
        }
    }

    static class PhaseSelector implements Selector {
        @Override
        public <T extends ExtAbility> T select(List<T> candidates) {
            return candidates.isEmpty() ? null : candidates.get(0);
        }

        @Override
        public String getName() {
            return "phase0Selector";
        }
    }

    @Test
    void shouldWireEventBusInBuilder() {
        FlexPoint flexPoint = FlexPointBuilder.create().build();
        Assertions.assertNotNull(flexPoint);
        Assertions.assertNotNull(flexPoint.getEventBus());
    }

    @Test
    void shouldUseUnifiedCountPath() {
        FlexPoint flexPoint = FlexPointBuilder.create().build();
        flexPoint.registerSelector(new PhaseSelector());
        flexPoint.register(new PhaseAbilityA());
        flexPoint.register(new PhaseAbilityB());

        Assertions.assertEquals(2, flexPoint.getExtCount());
    }

    @Test
    void shouldIgnoreNullEventAfterShutdown() {
        DefaultEventBus eventBus = new DefaultEventBus();
        eventBus.shutdown();
        Assertions.assertDoesNotThrow(() -> eventBus.publish(null));
    }

    @Test
    void shouldKeepEventBusScopedPerFlexPointInstance() {
        FlexPoint flexPoint1 = FlexPointBuilder.create().build();
        Object bus1 = flexPoint1.getEventBus();
        Assertions.assertNotNull(bus1);

        FlexPoint flexPoint2 = FlexPointBuilder.create().build();
        Object bus2 = flexPoint2.getEventBus();

        Assertions.assertNotSame(bus1, bus2, "每个 FlexPoint 实例应持有自己的 EventBus");

        // 先关闭第一个实例，不应影响第二个实例的 EventBus 引用
        flexPoint1.shutdown();
        Assertions.assertSame(bus2, flexPoint2.getEventBus(), "关闭其他实例不应影响当前实例事件总线");
        flexPoint2.shutdown();
    }
}
