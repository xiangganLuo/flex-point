package com.flexpoint.test.extension;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.extension.DefaultExtensionAbilityRegistry;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.extension.ExtensionAbilityRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtensionRegistryTest {
    private ExtensionAbilityRegistry registry;

    interface DemoAbilityDef extends ExtensionAbility {

    }

    static class DemoAbility implements DemoAbilityDef {
        @Override
        public String getCode() { return "demo"; }
    }
    static class DemoAbility2 implements DemoAbilityDef {
        @Override
        public String getCode() { return "demo2"; }
    }

    @BeforeEach
    public void setup() {
        FlexPointConfig.RegistryConfig config = new FlexPointConfig.RegistryConfig();
        config.setEnabled(true);
        config.setAllowDuplicateRegistration(false);
        registry = new DefaultExtensionAbilityRegistry(config);
    }

    @Test
    public void testRegisterAndFind() {
        DemoAbility demo = new DemoAbility();
        registry.register(demo);
        ExtensionAbility found = registry.getExtensionById("demo:0.0.0");
        Assertions.assertNotNull(found);
        Assertions.assertEquals("demo", found.getCode());
    }

    @Test
    public void testUnregister() {
        DemoAbility demo = new DemoAbility();
        registry.register(demo);
        registry.unregister("demo:0.0.0");
        Assertions.assertNull(registry.getExtensionById("demo:0.0.0"));
    }

    @Test
    public void testDuplicateRegisterNotAllowed() {
        DemoAbility demo = new DemoAbility();
        registry.register(demo);
        Assertions.assertThrows(IllegalStateException.class, () -> registry.register(demo));
    }

    @Test
    public void testGetAllExtensionAbility() {
        registry.register(new DemoAbility());
        registry.register(new DemoAbility2());
        List<DemoAbilityDef> all = registry.getAllExtensionAbility(DemoAbilityDef.class);
        Assertions.assertTrue(all.size() >= 2);
    }

    @Test
    public void testThreadSafeRegister() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger success = new AtomicInteger();
        for (int i = 0; i < threadCount; i++) {
            int idx = i;
            new Thread(() -> {
                try {
                    ExtensionAbility ab = new ExtensionAbility() {
                        @Override
                        public String getCode() { return "t" + idx; }
                    };
                    registry.register(ab);
                    success.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        Assertions.assertEquals(threadCount, success.get());
    }
} 