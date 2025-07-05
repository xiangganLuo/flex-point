package com.flexpoint.test.complx;

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

public class MultiFieldRuleTest {
    private FlexPoint flexPoint;

    @Selector("MultiFieldStrategy")
    public interface MultiFieldAbility extends ExtensionAbility {
        String process(String input);
    }
    static class V1Impl implements MultiFieldAbility {
        @Override public String getCode() { return "biz"; }
        @Override public String version() { return "1.0.0"; }
        @Override public String process(String input) { return "v1"; }
    }
    static class V2Impl implements MultiFieldAbility {
        @Override public String getCode() { return "biz"; }
        @Override public String version() { return "2.0.0"; }
        @Override public String process(String input) { return "v2"; }
    }
    static class MultiField extends AbstractSelector {
        private final String version;
        public MultiField(String version) { this.version = version; }
        @Override
        protected SelectionContext extractContext() {
            // code固定为biz，version动态
            return new SelectionContext("biz", version);
        }
        @Override
        public String getName() { return "MultiFieldStrategy"; }
    }

    @BeforeEach
    public void setup() {
        FlexPointConfig config = new FlexPointConfig();
        config.setEnabled(true);
        flexPoint = FlexPointBuilder.create(config).build();
    }

    @Test
    public void testMultiFieldVersionRouting() {
        flexPoint.registerSelector(new MultiField("2.0.0"));
        flexPoint.register(new V1Impl());
        flexPoint.register(new V2Impl());
        MultiFieldAbility ab = flexPoint.findAbility(MultiFieldAbility.class);
        Assertions.assertEquals("v2", ab.process("test"));
    }
} 