package com.flexpoint.test.resolution;

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

public class ResolutionTest {
    private FlexPoint flexPoint;

    @ExtensionResolverSelector("ContextStrategy")
    interface AbAbility extends ExtensionAbility {
    }

    static class A implements AbAbility { public String getCode() { return "A"; } }
    static class B implements AbAbility { public String getCode() { return "B"; } }

    @ExtensionResolverSelector("AnnoStrategy")
    interface AnnotatedAbility extends ExtensionAbility {}
    static class AnnotatedA implements AnnotatedAbility { public String getCode() { return "A"; } }
    static class AnnotatedB implements AnnotatedAbility { public String getCode() { return "B"; } }

    static class AnnoStrategy extends AbstractExtensionResolutionStrategy {
        @Override
        protected ResolutionContext extractContext() { return new ResolutionContext("B", null); }
        @Override
        public String getStrategyName() { return "AnnoStrategy"; }
    }
    static class ContextStrategy extends AbstractExtensionResolutionStrategy {
        private final String code;
        public ContextStrategy(String code) { this.code = code; }
        @Override
        protected ResolutionContext extractContext() { return new ResolutionContext(code, null); }
        @Override
        public String getStrategyName() { return "ContextStrategy"; }
    }

    @BeforeEach
    public void setup() {
        FlexPointConfig config = new FlexPointConfig();
        config.setEnabled(true);
        flexPoint = FlexPointBuilder.create(config).build();
    }

    @Test
    public void testRegisterAndUseCustomStrategy() {
        flexPoint.registerResolver(new ContextStrategy("B"));
        flexPoint.register(new A());
        flexPoint.register(new B());
        ExtensionAbility found = flexPoint.findAbility(AbAbility.class);
        Assertions.assertEquals("B", found.getCode());
    }

    @Test
    public void testAnnotationStrategyPriority() {
        flexPoint.registerResolver(new AnnoStrategy());
        flexPoint.register(new AnnotatedA());
        flexPoint.register(new AnnotatedB());
        AnnotatedAbility found = flexPoint.findAbility(AnnotatedAbility.class);
        Assertions.assertEquals("B", found.getCode());
    }

    @Test
    public void testStrategyNotFoundException() {
        flexPoint.register(new AnnotatedA());
        Assertions.assertThrows(RuntimeException.class, () -> flexPoint.findAbility(AnnotatedAbility.class));
    }
} 