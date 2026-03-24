package com.flexpoint.test;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.common.exception.MultipleExtMatchedException;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.event.EventContext;
import com.flexpoint.core.event.EventType;
import com.flexpoint.core.selector.resolves.CodeSelector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 集成测试类
 *
 * @author xiangganluo
 */
public class IntegrationTest {
    private FlexPoint flexPoint;

    @FpSelector("DemoStrategy")
    public interface DemoAbilityDef extends ExtAbility {
    }

    static class DemoAbility implements DemoAbilityDef {
        @Override
        public String getCode() {
            return "demo";
        }
    }

    static class SpecialAbility implements DemoAbilityDef {
        @Override
        public String getCode() {
            return "special";
        }
    }

    static class DemoSelector extends CodeSelector {
        public DemoSelector(CodeResolver resolver) {
            super(resolver);
        }

        @Override
        public String getName() {
            return "DemoStrategy";
        }
    }

    @BeforeEach
    public void setup() {
        FlexPointConfig config = new FlexPointConfig();
        config.setEnabled(true);
        config.getMonitor().setEnabled(true);
        config.getRegistry().setEnabled(true);
        flexPoint = FlexPointBuilder.create(config).build();
    }

    @Test
    public void testFullIntegrationFlow() {
        // 注册选择器
        flexPoint.registerSelector(new DemoSelector(() -> "demo"));
        // 注册扩展点
        flexPoint.register(new DemoAbility());
        flexPoint.register(new SpecialAbility());

        // 查找扩展点（应命中 demo）
        DemoAbilityDef found = flexPoint.findAbility(DemoAbilityDef.class);
        Assertions.assertNotNull(found);
        Assertions.assertEquals("demo", found.getCode());
    }

    @Test
    public void testEventContextExtIdWhenExtAbilityIsNull() {
        EventContext context = EventContext.create(EventType.SELECTOR_FOUND);
        Assertions.assertNull(context.getExtId());
    }

    @Test
    public void testMultipleException() {
        // 注册选择器
        flexPoint.registerSelector(new DemoSelector(() -> "special"));
        // 注册扩展点
        flexPoint.register(new DemoAbility());
        flexPoint.register(new SpecialAbility());
        flexPoint.register(new SpecialAbility());

        Assertions.assertThrows(
                MultipleExtMatchedException.class,
                () -> flexPoint.findAbility(DemoAbilityDef.class)
        );
    }
}
