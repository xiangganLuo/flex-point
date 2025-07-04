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

import java.util.HashMap;
import java.util.Map;

public class ABRuleTest {
    private FlexPoint flexPoint;

    @ExtensionResolverSelector("ABTestStrategy")
    interface ABTestAbility extends ExtensionAbility {
        String process(String userId);
    }
    static class GrayAbility implements ABTestAbility {
        @Override public String getCode() { return "gray"; }
        @Override public String process(String userId) { return "gray"; }
    }
    static class NormalAbility implements ABTestAbility {
        @Override public String getCode() { return "normal"; }
        @Override public String process(String userId) { return "normal"; }
    }
    static class ABTestStrategy extends AbstractExtensionResolutionStrategy {
        private final Map<String, String> userGroup;
        public ABTestStrategy(Map<String, String> userGroup) { this.userGroup = userGroup; }
        @Override
        protected ResolutionContext extractContext() {
            // 假设从ThreadLocal获取userId
            String userId = UserContext.get();
            String code = userGroup.getOrDefault(userId, "normal");
            return new ResolutionContext(code, null);
        }
        @Override
        public String getStrategyName() { return "ABTestStrategy"; }
    }
    static class UserContext {
        private static final ThreadLocal<String> holder = new ThreadLocal<>();
        public static void set(String userId) { holder.set(userId); }
        public static String get() { return holder.get(); }
        public static void clear() { holder.remove(); }
    }

    @BeforeEach
    public void setup() {
        FlexPointConfig config = new FlexPointConfig();
        config.setEnabled(true);
        flexPoint = FlexPointBuilder.create(config).build();
    }

    @Test
    public void testGrayAndABTestStrategy() {
        // user1 灰度，user2 普通
        Map<String, String> userGroup = new HashMap<>();
        userGroup.put("user1", "gray");
        userGroup.put("user2", "normal");
        flexPoint.registerResolver(new ABTestStrategy(userGroup));
        flexPoint.register(new GrayAbility());
        flexPoint.register(new NormalAbility());

        UserContext.set("user1");
        ABTestAbility ab1 = flexPoint.findAbility(ABTestAbility.class);
        Assertions.assertEquals("gray", ab1.process("user1"));

        UserContext.set("user2");
        ABTestAbility ab2 = flexPoint.findAbility(ABTestAbility.class);
        Assertions.assertEquals("normal", ab2.process("user2"));

        UserContext.clear();
    }
} 