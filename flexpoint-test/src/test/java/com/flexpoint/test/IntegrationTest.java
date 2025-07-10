package com.flexpoint.test;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.context.Context;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.selector.Selector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IntegrationTest {
    private FlexPoint flexPoint;

    @FpSelector
    public interface DemoAbilityDef extends ExtensionAbility {
    }

    static class DemoAbility implements DemoAbilityDef {
        @Override public String getCode() { return "demo"; }
    }
    static class SpecialAbility implements DemoAbilityDef {
        @Override public String getCode() { return "special"; }
    }
    static class DemoSelector implements Selector {
        @Override
        public <T extends ExtensionAbility> T select(java.util.List<T> candidates, Context context) {
            for (T ext : candidates) {
                if ("special".equals(ext.getCode())) {
                    return ext;
                }
            }
            return null;
        }
        @Override
        public String getName() { return "DemoStrategy"; }
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
        flexPoint.registerSelector(new DemoSelector());
        // 注册扩展点
        flexPoint.register(new DemoAbility());
        flexPoint.register(new SpecialAbility());
        // 查找扩展点（应命中special）
        DemoAbilityDef found = flexPoint.findAbility(DemoAbilityDef.class);
        Assertions.assertNotNull(found);
        Assertions.assertEquals("special", found.getCode());
        // 监控统计
        String extId = found.getCode() + ":" + found.version();
        flexPoint.recordInvocation(extId, 123, true);
        ExtensionMonitor.ExtensionMetrics metrics = flexPoint.getExtensionMetrics(extId);
        Assertions.assertEquals(1, metrics.getTotalInvocations());
        // 注销扩展点
        flexPoint.unregister(extId);
        Assertions.assertNull(flexPoint.findAbility(DemoAbilityDef.class));
    }
} 