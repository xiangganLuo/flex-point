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

import java.util.HashSet;
import java.util.Set;

public class GrayRuleTest {
    private FlexPoint flexPoint;

    @Selector("GrayStrategy")
    public interface GrayAbility extends ExtensionAbility {
        String process(String input);
    }
    static class GrayImpl implements GrayAbility {
        @Override public String getCode() { return "gray"; }
        @Override public String process(String userId) { return "gray"; }
    }
    static class NormalImpl implements GrayAbility {
        @Override public String getCode() { return "normal"; }
        @Override public String process(String userId) { return "normal"; }
    }
    static class Gray extends AbstractSelector {
        private final Set<String> grayUsers;
        public Gray(Set<String> grayUsers) { this.grayUsers = grayUsers; }
        @Override
        protected SelectionContext extractContext() {
            String userId = UserContext.get();
            String code = grayUsers.contains(userId) ? "gray" : "normal";
            return new SelectionContext(code, null);
        }
        @Override
        public String getName() { return "GrayStrategy"; }
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
    public void testGrayUserRouting() {
        Set<String> grayUsers = new HashSet<>();
        grayUsers.add("u1");
        grayUsers.add("u2");
        flexPoint.registerSelector(new Gray(grayUsers));
        flexPoint.register(new GrayImpl());
        flexPoint.register(new NormalImpl());

        UserContext.set("u1");
        GrayAbility a1 = flexPoint.findAbility(GrayAbility.class);
        Assertions.assertEquals("gray", a1.process("u1"));

        UserContext.set("u3");
        GrayAbility a2 = flexPoint.findAbility(GrayAbility.class);
        Assertions.assertEquals("normal", a2.process("u3"));

        UserContext.clear();
    }
} 