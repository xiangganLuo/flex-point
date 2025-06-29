package com.flexpoint.test;

import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.registry.ExtensionAbility;
import com.flexpoint.core.FlexPointManager;
import com.flexpoint.core.monitor.DefaultExtensionMonitor;
import com.flexpoint.core.registry.FlexPointExtensionAbilityRegistry;
import com.flexpoint.core.registry.metadata.DefaultExtensionMetadata;
import com.flexpoint.core.registry.metadata.ExtensionMetadata;
import com.flexpoint.core.resolution.DefaultExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ExtensionResolutionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExtensionPointManager核心功能测试
 *
 * @author xiangganluo
 * @version 1.0.0
 */
class FlexPointCoreTest {

    private FlexPointManager manager;
    private FlexPointExtensionAbilityRegistry registry;
    private DefaultExtensionMonitor monitor;

    @BeforeEach
    void setUp() {
        manager = FlexPointBuilder.create().build();
        registry = (FlexPointExtensionAbilityRegistry) manager.getExtensionAbilityRegistry();
        monitor = (DefaultExtensionMonitor) manager.getExtensionMonitor();
    }

    @Test
    void testFlexPointBuilder() {
        // 测试建造者模式
        FlexPointManager manager = FlexPointBuilder.create()
                .withConfig(new FlexPointConfig())
                .withRegistry(new FlexPointExtensionAbilityRegistry())
                .withMonitor(new DefaultExtensionMonitor())
                .withResolverFactory(null)
                .build();

        assertNotNull(manager);
        assertNotNull(manager.getExtensionAbilityRegistry());
        assertNotNull(manager.getExtensionMonitor());
    }

    @Test
    void testExtensionPointManagerRegister() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        ExtensionMetadata metadata = DefaultExtensionMetadata.builder()
                .extensionId("test-impl-1")
                .version("1.0.0")
                .priority(100)
                .description("Test implementation 1")
                .enabled(true)
                .extensionType("TestExtension")
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .build();

        // 注册扩展点
        manager.register(TestExtension.class, testExtension, metadata);

        // 查找扩展点
        TestExtension found = manager.findAbility(TestExtension.class);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testExtensionPointManagerRegisterWithoutMetadata() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();

        // 注册扩展点（无元数据）
        manager.register(TestExtension.class, testExtension);

        // 查找扩展点
        TestExtension found = manager.findAbility(TestExtension.class);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testExtensionPointManagerGetExtensions() {
        // 创建两个扩展点
        TestExtensionImpl testExtension1 = new TestExtensionImpl();
        TestExtensionImpl2 testExtension2 = new TestExtensionImpl2();

        // 注册扩展点
        manager.register(TestExtension.class, testExtension1);
        manager.register(TestExtension.class, testExtension2);

        // 获取所有扩展点
        List<TestExtension> extensions = manager.getExtensions(TestExtension.class);
        assertEquals(2, extensions.size());
    }

    @Test
    void testExtensionPointManagerGetExtensionsWithContext() {
        // 创建两个扩展点
        TestExtensionImpl testExtension1 = new TestExtensionImpl();
        TestExtensionImpl2 testExtension2 = new TestExtensionImpl2();

        // 注册扩展点
        manager.register(TestExtension.class, testExtension1);
        manager.register(TestExtension.class, testExtension2);

        // 获取所有扩展点（带上下文）
        List<TestExtension> extensions = manager.getExtensions(TestExtension.class);
        assertEquals(2, extensions.size());
    }

    @Test
    void testExtensionPointManagerGetExtensionMetadata() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        ExtensionMetadata metadata = DefaultExtensionMetadata.builder()
                .extensionId("test-impl-1")
                .version("1.0.0")
                .priority(100)
                .description("Test implementation 1")
                .enabled(true)
                .extensionType("TestExtension")
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .build();

        // 注册扩展点
        manager.register(TestExtension.class, testExtension, metadata);

        // 获取元数据
        ExtensionMetadata found = manager.getExtensionMetadata(TestExtension.class, "test-impl-1");
        assertNotNull(found);
        assertEquals("test-impl-1", found.getExtensionId());
        assertEquals("1.0.0", found.getVersion());
    }

    @Test
    void testExtensionPointManagerGetExtensionMetrics() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        manager.register(TestExtension.class, testExtension);

        // 调用扩展点
        TestExtension found = manager.findAbility(TestExtension.class);
        found.sayHello();

        // 获取监控指标
        var metrics = manager.getExtensionMetrics("TestExtensionImpl");
        assertNotNull(metrics);
        assertTrue(metrics.getTotalInvocations() > 0);
    }

    @Test
    void testExtensionPointManagerGetAllExtensionMetrics() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        manager.register(TestExtension.class, testExtension);

        // 调用扩展点
        TestExtension found = manager.findAbility(TestExtension.class);
        found.sayHello();

        // 获取所有监控指标
        Map<String, DefaultExtensionMonitor.ExtensionMetrics> allMetrics = manager.getAllExtensionMetrics();
        assertNotNull(allMetrics);
        assertFalse(allMetrics.isEmpty());
    }

    @Test
    void testExtensionPointManagerWithCustomResolver() {
        // 创建自定义解析策略
        ExtensionResolutionStrategy customResolver = new CustomExtensionResolutionStrategy();

        // 使用自定义解析策略构建ExtensionPointManager
        FlexPointManager manager = FlexPointBuilder.create()
                .withResolver(customResolver)
                .build();

        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        manager.register(TestExtension.class, testExtension);

        // 查找扩展点
        TestExtension found = manager.findAbility(TestExtension.class);
        assertNotNull(found);
    }

    @Test
    void testExtensionPointManagerRegisterResolver() {
        // 创建自定义解析策略
        ExtensionResolutionStrategy customResolver = new CustomExtensionResolutionStrategy();

        // 注册自定义解析策略
        manager.registerResolver(customResolver);

        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        manager.register(TestExtension.class, testExtension);

        // 查找扩展点
        TestExtension found = manager.findAbility(TestExtension.class);
        assertNotNull(found);
    }

    @Test
    void testExtensionPointManagerWithConfig() {
        // 创建配置
        FlexPointConfig config = new FlexPointConfig();
        config.setEnabled(true);

        // 使用配置构建ExtensionPointManager
        FlexPointManager manager = FlexPointBuilder.create(config).build();

        assertNotNull(manager);
        assertNotNull(manager.getExtensionAbilityRegistry());
        assertNotNull(manager.getExtensionMonitor());
    }

    // 测试用的扩展点接口和实现
    static class CustomExtensionResolutionStrategy extends DefaultExtensionResolutionStrategy {
        @Override
        public String getStrategyName() {
            return "CustomExtensionResolutionStrategy";
        }
    }

    interface TestExtension extends ExtensionAbility {
        String sayHello();
    }

    static class TestExtensionImpl implements TestExtension {
        @Override
        public String getCode() {
            return "test";
        }

        @Override
        public String sayHello() {
            return "Hello from TestExtensionImpl";
        }
    }

    static class TestExtensionImpl2 implements TestExtension {
        @Override
        public String getCode() {
            return "test2";
        }

        @Override
        public String sayHello() {
            return "Hello from TestExtensionImpl2";
        }
    }
} 