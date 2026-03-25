package com.flexpoint.test;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.event.DefaultEventBus;
import com.flexpoint.core.event.EventPublisher;
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
        Assertions.assertNotNull(EventPublisher.getEventBus());
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
}
