package com.flexpoint.test;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.extension.ExtensionAbilityFactory;
import com.flexpoint.core.monitor.DefaultExtensionMonitor;
import com.flexpoint.core.registry.DefaultExtensionRegistry;
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
 * FlexPoint核心功能测试
 *
 * @author xiangganluo
 * @version 1.0.0
 */
class FlexPointCoreTest {

    private FlexPoint flexPoint;
    private ExtensionAbilityFactory factory;
    private DefaultExtensionRegistry registry;
    private DefaultExtensionMonitor monitor;

    @BeforeEach
    void setUp() {
        flexPoint = FlexPointBuilder.create().build();
        factory = flexPoint.getAbilityFactory();
        registry = (DefaultExtensionRegistry) flexPoint.getRegistry();
        monitor = (DefaultExtensionMonitor) flexPoint.getMonitor();
    }

    @Test
    void testFlexPointBuilder() {
        // 测试建造者模式
        FlexPoint flexPoint = FlexPointBuilder.create()
                .withConfig(new FlexPointConfig())
                .withRegistry(new DefaultExtensionRegistry())
                .withMonitor(new DefaultExtensionMonitor())
                .withResolverFactory(null)
                .build();

        assertNotNull(flexPoint);
        assertNotNull(flexPoint.getAbilityFactory());
        assertNotNull(flexPoint.getRegistry());
        assertNotNull(flexPoint.getMonitor());
    }

    @Test
    void testFlexPointRegister() {
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
        flexPoint.register(TestExtension.class, testExtension, metadata);

        // 查找扩展点
        TestExtension found = factory.findAbility(TestExtension.class);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testFlexPointRegisterWithoutMetadata() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();

        // 注册扩展点（无元数据）
        flexPoint.register(TestExtension.class, testExtension);

        // 查找扩展点
        TestExtension found = factory.findAbility(TestExtension.class);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testFlexPointGetExtensions() {
        // 创建两个扩展点
        TestExtensionImpl testExtension1 = new TestExtensionImpl();
        TestExtensionImpl2 testExtension2 = new TestExtensionImpl2();

        // 注册扩展点
        flexPoint.register(TestExtension.class, testExtension1);
        flexPoint.register(TestExtension.class, testExtension2);

        // 获取所有扩展点
        List<TestExtension> extensions = flexPoint.getExtensions(TestExtension.class);
        assertEquals(2, extensions.size());
    }

    @Test
    void testFlexPointGetExtensionsWithContext() {
        // 创建两个扩展点
        TestExtensionImpl testExtension1 = new TestExtensionImpl();
        TestExtensionImpl2 testExtension2 = new TestExtensionImpl2();

        // 注册扩展点
        flexPoint.register(TestExtension.class, testExtension1);
        flexPoint.register(TestExtension.class, testExtension2);

        // 获取所有扩展点（带上下文）
        List<TestExtension> extensions = flexPoint.getExtensions(TestExtension.class);
        assertEquals(2, extensions.size());
    }

    @Test
    void testFlexPointGetExtensionMetadata() {
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
        flexPoint.register(TestExtension.class, testExtension, metadata);

        // 获取元数据
        ExtensionMetadata found = flexPoint.getExtensionMetadata(TestExtension.class, "test-impl-1");
        assertNotNull(found);
        assertEquals("test-impl-1", found.getExtensionId());
        assertEquals("1.0.0", found.getVersion());
    }

    @Test
    void testFlexPointGetExtensionMetrics() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        flexPoint.register(TestExtension.class, testExtension);

        // 调用扩展点
        TestExtension found = factory.findAbility(TestExtension.class);
        found.sayHello();

        // 获取监控指标
        var metrics = flexPoint.getExtensionMetrics("TestExtensionImpl");
        assertNotNull(metrics);
        assertTrue(metrics.getTotalInvocations() > 0);
    }

    @Test
    void testFlexPointGetAllExtensionMetrics() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        flexPoint.register(TestExtension.class, testExtension);

        // 调用扩展点
        TestExtension found = factory.findAbility(TestExtension.class);
        found.sayHello();

        // 获取所有监控指标
        Map<String, DefaultExtensionMonitor.ExtensionMetrics> allMetrics = flexPoint.getAllExtensionMetrics();
        assertNotNull(allMetrics);
        assertFalse(allMetrics.isEmpty());
    }

    @Test
    void testFlexPointWithCustomResolver() {
        // 创建自定义解析策略
        ExtensionResolutionStrategy customResolver = new CustomExtensionResolutionStrategy();

        // 使用自定义解析策略构建FlexPoint
        FlexPoint flexPoint = FlexPointBuilder.create()
                .withResolver(customResolver)
                .build();

        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        flexPoint.register(TestExtension.class, testExtension);

        // 查找扩展点
        TestExtension found = flexPoint.getAbilityFactory().findAbility(TestExtension.class);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testFlexPointRegisterResolver() {
        // 创建自定义解析策略
        ExtensionResolutionStrategy customResolver = new CustomExtensionResolutionStrategy();

        // 注册自定义解析策略
        flexPoint.registerResolver(customResolver);

        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        flexPoint.register(TestExtension.class, testExtension);

        // 查找扩展点
        TestExtension found = flexPoint.getAbilityFactory().findAbility(TestExtension.class);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testFlexPointWithConfig() {
        // 创建配置
        FlexPointConfig config = new FlexPointConfig();
        config.setEnabled(true);
        config.getMonitor().setEnabled(true);
        config.getRegistry().setEnabled(true);

        // 使用配置构建FlexPoint
        FlexPoint flexPoint = FlexPointBuilder.create()
                .withConfig(config)
                .build();

        assertNotNull(flexPoint);
        assertTrue(flexPoint.getConfig().isEnabled());
        assertTrue(flexPoint.getConfig().getMonitor().isEnabled());
        assertTrue(flexPoint.getConfig().getRegistry().isEnabled());
    }

    // 自定义解析策略
    static class CustomExtensionResolutionStrategy extends DefaultExtensionResolutionStrategy {
        @Override
        public String getStrategyName() {
            return "CustomExtensionResolutionStrategy";
        }
    }

    // 测试扩展点接口
    interface TestExtension extends ExtensionAbility {
        String sayHello();
    }

    // 测试扩展点实现1
    static class TestExtensionImpl implements TestExtension {
        @Override
        public String getCode() {
            return "test";
        }

        @Override
        public String sayHello() {
            return "Hello from test extension";
        }
    }

    // 测试扩展点实现2
    static class TestExtensionImpl2 implements TestExtension {
        @Override
        public String getCode() {
            return "test2";
        }

        @Override
        public String sayHello() {
            return "Hello from test2 extension";
        }
    }
} 