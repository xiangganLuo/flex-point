package com.flexpoint.test.selector;

import com.flexpoint.common.annotations.Selector;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.selector.AbstractSelector;
import com.flexpoint.core.selector.SelectionContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SelectorTest {
    private FlexPoint flexPoint;

    @Selector("ContextStrategy")
    interface AbAbility extends ExtensionAbility {
    }

    static class A implements AbAbility { public String getCode() { return "A"; } }
    static class B implements AbAbility { public String getCode() { return "B"; } }

    @Selector("AnnoStrategy")
    interface AnnotatedAbility extends ExtensionAbility {}
    static class AnnotatedA implements AnnotatedAbility { public String getCode() { return "A"; } }

    static class Context extends AbstractSelector {
        private final String code;
        public Context(String code) { this.code = code; }
        @Override
        protected SelectionContext extractContext() { return new SelectionContext(code, null); }
        @Override
        public String getName() { return "ContextStrategy"; }
    }

    @BeforeEach
    public void setup() {
        FlexPointConfig config = new FlexPointConfig();
        config.setEnabled(true);
        flexPoint = FlexPointBuilder.create(config).build();
    }

    @Test
    public void testRegisterAndUseCustomSelector() {
        flexPoint.registerSelector(new Context("B"));
        flexPoint.register(new A());
        flexPoint.register(new B());
        ExtensionAbility found = flexPoint.findAbility(AbAbility.class);
        Assertions.assertEquals("B", found.getCode());
    }

    @Test
    public void testSelectorNotFoundException() {
        flexPoint.register(new AnnotatedA());
        Assertions.assertThrows(RuntimeException.class, () -> flexPoint.findAbility(AnnotatedAbility.class));
    }
} 