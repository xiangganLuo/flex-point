package com.flexpoint.test.selector;

import com.flexpoint.core.event.DefaultEventBus;
import com.flexpoint.core.event.EventDispatcher;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.selector.AbstractSelector;
import com.flexpoint.core.selector.DefaultSelectorRegistry;
import com.flexpoint.core.selector.SelectorRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 选择器注册表可观测能力（P5：getSelectorNames/size）与资源级唯一。
 *
 * @author xiangganluo
 */
public class SelectorRegistryTest {

    static class NamedSelector extends AbstractSelector {
        private final String name;
        NamedSelector(String name) { this.name = name; }
        @Override protected <T extends ExtAbility> List<T> filter(List<T> candidates) { return candidates; }
        @Override public String getName() { return name; }
    }

    private SelectorRegistry newRegistry() {
        return new DefaultSelectorRegistry(new EventDispatcher(new DefaultEventBus()));
    }

    @Test
    void names_and_size_reflect_registrations() {
        SelectorRegistry reg = newRegistry();
        Assertions.assertEquals(0, reg.size());
        Assertions.assertTrue(reg.getSelectorNames().isEmpty());

        reg.register(new NamedSelector("s1"));
        reg.register(new NamedSelector("s2"));

        Assertions.assertEquals(2, reg.size());
        Assertions.assertTrue(reg.getSelectorNames().contains("s1"));
        Assertions.assertTrue(reg.getSelectorNames().contains("s2"));
    }

    @Test
    void names_snapshot_is_unmodifiable() {
        SelectorRegistry reg = newRegistry();
        reg.register(new NamedSelector("s1"));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> reg.getSelectorNames().add("x"));
    }

    @Test
    void duplicate_selector_name_is_rejected() {
        SelectorRegistry reg = newRegistry();
        reg.register(new NamedSelector("dup"));
        Assertions.assertThrows(IllegalStateException.class,
                () -> reg.register(new NamedSelector("dup")));
    }
}
