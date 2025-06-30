package com.flexpoint.test.complx;

import com.flexpoint.common.annotations.ExtensionResolverSelector;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.resolution.AbstractExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ResolutionContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MultiFieldRuleTest {
    private FlexPoint flexPoint;

    @ExtensionResolverSelector("MultiFieldStrategy")
    interface MultiVerAbility extends ExtensionAbility {
        String process();
    }
    static class V1Impl implements MultiVerAbility {
        @Override public String getCode() { return "biz"; }
        @Override public String version() { return "1.0.0"; }
        @Override public String process() { return "v1"; }
    }
    static class V2Impl implements MultiVerAbility {
        @Override public String getCode() { return "biz"; }
        @Override public String version() { return "2.0.0"; }
        @Override public String process() { return "v2"; }
    }
    static class MultiFieldStrategy extends AbstractExtensionResolutionStrategy {
        private final String version;
        public MultiFieldStrategy(String version) { this.version = version; }
        @Override
        protected ResolutionContext extractContext() {
            // code固定为biz，version动态
            return new ResolutionContext("biz", version);
        }
        @Override
        public String getStrategyName() { return "MultiFieldStrategy"; }
    }

    @BeforeEach
    public void setup() {
        FlexPointConfig config = new FlexPointConfig();
        config.setEnabled(true);
        flexPoint = FlexPointBuilder.create(config).build();
    }

    @Test
    public void testMultiFieldVersionRouting() {
        flexPoint.registerResolver(new MultiFieldStrategy("2.0.0"));
        flexPoint.register(new V1Impl());
        flexPoint.register(new V2Impl());
        MultiVerAbility ab = flexPoint.findAbility(MultiVerAbility.class);
        Assertions.assertEquals("v2", ab.process());
    }
} 