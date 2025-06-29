package com.flexpoint.test;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.resolution.AbstractExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ResolutionContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IntegrationTest {
    private FlexPoint flexPoint;

    static class DemoAbility implements ExtensionAbility {
        @Override public String getCode() { return "demo"; }
    }
    static class SpecialAbility implements ExtensionAbility {
        @Override public String getCode() { return "special"; }
    }
    static class DemoStrategy extends AbstractExtensionResolutionStrategy {
        @Override
        protected ResolutionContext extractContext() { return new ResolutionContext("special", null); }
        @Override
        public String getStrategyName() { return "DemoStrategy"; }
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
        // 注册解析策略
        flexPoint.registerResolver(new DemoStrategy());
        // 注册扩展点
        flexPoint.register(new DemoAbility());
        flexPoint.register(new SpecialAbility());
        // 查找扩展点（应命中special）
        ExtensionAbility found = flexPoint.findAbility(DemoAbility.class);
        Assertions.assertNotNull(found);
        Assertions.assertEquals("special", found.getCode());
        // 监控统计
        String extId = found.getCode() + ":" + found.version();
        flexPoint.recordInvocation(extId, 123, true);
        ExtensionMonitor.ExtensionMetrics metrics = flexPoint.getExtensionMetrics(extId);
        Assertions.assertEquals(1, metrics.getTotalInvocations());
        // 注销扩展点
        flexPoint.unregister(extId);
        Assertions.assertNull(flexPoint.findAbility(DemoAbility.class));
    }
} 