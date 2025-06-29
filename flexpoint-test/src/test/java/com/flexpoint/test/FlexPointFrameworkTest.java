package com.flexpoint.test;

import com.flexpoint.common.annotations.ExtensionAbilityReference;
import com.flexpoint.common.annotations.ExtensionInfo;
import com.flexpoint.core.registry.ExtensionAbility;
import com.flexpoint.core.FlexPointManager;
import com.flexpoint.core.monitor.DefaultExtensionMonitor;
import com.flexpoint.core.registry.FlexPointExtensionAbilityRegistry;
import com.flexpoint.core.registry.metadata.DefaultExtensionMetadata;
import com.flexpoint.core.registry.metadata.ExtensionMetadata;
import com.flexpoint.core.resolution.DefaultExtensionResolutionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 扩展点框架测试
 *
 * @author xiangganluo
 * @version 1.0.0
 */
class FlexPointFrameworkTest {

    private FlexPointManager manager;
    private FlexPointExtensionAbilityRegistry registry;
    private DefaultExtensionMonitor monitor;

    @BeforeEach
    void setUp() {
        registry = new FlexPointExtensionAbilityRegistry();
        monitor = new DefaultExtensionMonitor();
        manager = new FlexPointManager(registry, monitor);
    }

    @Test
    void testBasicExtensionRegistration() {
        // 创建测试扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        
        // 注册扩展点
        registry.register(TestExtension.class, testExtension, null);
        
        // 查找扩展点
        TestExtension found = manager.findAbility(TestExtension.class);
        
        assertNotNull(found);
        assertEquals("test", found.getCode());
        assertEquals("Hello from test extension", found.sayHello());
    }

    @Test
    void testCodeResolution() {
        // 创建两个不同code的扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        TestExtensionImpl2 testExtension2 = new TestExtensionImpl2();
        
        // 注册扩展点
        registry.register(TestExtension.class, testExtension, null);
        registry.register(TestExtension.class, testExtension2, null);
        
        // 使用code查找
        Map<String, Object> context = new HashMap<>();
        context.put("code", "test");
        
        TestExtension found = manager.findAbility(TestExtension.class, context);
        assertNotNull(found);
        assertEquals("test", found.getCode());
        
        // 使用不同的code查找
        context.put("code", "test2");
        TestExtension found2 = manager.findAbility(TestExtension.class, context);
        assertNotNull(found2);
        assertEquals("test2", found2.getCode());
    }

    @Test
    void testMonitoring() {
        // 创建并注册扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        registry.register(TestExtension.class, testExtension, null);
        
        // 调用扩展点
        TestExtension found = manager.findAbility(TestExtension.class);
        found.sayHello();
        
        // 验证监控指标
        var metrics = monitor.getMetrics("TestExtensionImpl");
        assertNotNull(metrics);
        assertTrue(metrics.getTotalInvocations() > 0);
        assertTrue(metrics.getSuccessInvocations() > 0);
    }

    @Test
    void testFindAbilityWithoutContext() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        registry.register(TestExtension.class, testExtension, null);
        
        // 不带context查找，应该使用默认code
        TestExtension found = manager.findAbility(TestExtension.class);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testFindAbilityWithNullContext() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        registry.register(TestExtension.class, testExtension, null);
        
        // 使用null context查找
        TestExtension found = manager.findAbility(TestExtension.class, null);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testFindAbilityWithEmptyContext() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        registry.register(TestExtension.class, testExtension, null);
        
        // 使用空context查找
        Map<String, Object> context = new HashMap<>();
        TestExtension found = manager.findAbility(TestExtension.class, context);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testFindAbilityOpt() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        registry.register(TestExtension.class, testExtension, null);
        
        // 使用Optional方式查找
        var found = manager.findAbilityOpt(TestExtension.class);
        assertTrue(found.isPresent());
        assertEquals("test", found.get().getCode());
    }

    @Test
    void testFindAbilityById() {
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
        
        registry.register(TestExtension.class, testExtension, metadata);
        
        // 根据ID查找
        TestExtension found = manager.findAbilityById(TestExtension.class, "test-impl-1");
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testGetExtensionMetadata() {
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
        
        registry.register(TestExtension.class, testExtension, metadata);
        
        // 获取元数据
        ExtensionMetadata found = manager.getExtensionMetadata(TestExtension.class, "test-impl-1");
        assertNotNull(found);
        assertEquals("test-impl-1", found.getExtensionId());
        assertEquals("1.0.0", found.getVersion());
        assertEquals(100, found.getPriority());
    }

    @Test
    void testGetExtensionMetrics() {
        // 创建并注册扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        registry.register(TestExtension.class, testExtension, null);
        
        // 调用扩展点
        TestExtension found = manager.findAbility(TestExtension.class);
        found.sayHello();
        
        // 获取监控指标
        var metrics = manager.getExtensionMetrics("TestExtensionImpl");
        assertNotNull(metrics);
        assertTrue(metrics.getTotalInvocations() > 0);
        assertTrue(metrics.getSuccessInvocations() > 0);
    }

    @Test
    void testGetAllExtensionMetrics() {
        // 创建并注册扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        registry.register(TestExtension.class, testExtension, null);
        
        // 调用扩展点
        TestExtension found = manager.findAbility(TestExtension.class);
        found.sayHello();
        
        // 获取所有监控指标
        Map<String, DefaultExtensionMonitor.ExtensionMetrics> allMetrics = manager.getAllExtensionMetrics();
        assertNotNull(allMetrics);
        assertFalse(allMetrics.isEmpty());
    }

    @Test
    void testMultipleExtensionsWithSameCode() {
        // 创建两个相同code的扩展点
        TestExtensionImpl testExtension1 = new TestExtensionImpl();
        TestExtensionImpl testExtension2 = new TestExtensionImpl();
        
        // 注册扩展点
        registry.register(TestExtension.class, testExtension1, null);
        registry.register(TestExtension.class, testExtension2, null);
        
        // 查找扩展点，应该返回第一个
        TestExtension found = manager.findAbility(TestExtension.class);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testExtensionNotFound() {
        // 查找不存在的扩展点
        TestExtension found = manager.findAbility(TestExtension.class);
        assertNull(found);
    }

    @Test
    void testExtensionNotFoundOpt() {
        // 查找不存在的扩展点（Optional方式）
        var found = manager.findAbilityOpt(TestExtension.class);
        assertFalse(found.isPresent());
    }

    @Test
    void testRegistryGetExtensions() {
        // 创建两个扩展点
        TestExtensionImpl testExtension1 = new TestExtensionImpl();
        TestExtensionImpl2 testExtension2 = new TestExtensionImpl2();
        
        // 注册扩展点
        registry.register(TestExtension.class, testExtension1, null);
        registry.register(TestExtension.class, testExtension2, null);
        
        // 获取所有扩展点
        List<TestExtension> extensions = manager.getExtensions(TestExtension.class);
        assertEquals(2, extensions.size());
        
        // 验证扩展点
        assertTrue(extensions.stream().anyMatch(ext -> "test".equals(ext.getCode())));
        assertTrue(extensions.stream().anyMatch(ext -> "test2".equals(ext.getCode())));
    }

    @Test
    void testDefaultExtensionResolutionStrategy() {
        // 创建两个不同code的扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        TestExtensionImpl2 testExtension2 = new TestExtensionImpl2();
        
        // 注册扩展点
        registry.register(TestExtension.class, testExtension, null);
        registry.register(TestExtension.class, testExtension2, null);
        
        // 使用默认解析策略
        DefaultExtensionResolutionStrategy strategy = new DefaultExtensionResolutionStrategy();
        Map<String, Object> context = new HashMap<>();
        context.put("code", "test");
        
        List<TestExtension> extensions = manager.getExtensions(TestExtension.class);
        TestExtension selected = strategy.resolve(extensions, context);
        
        assertNotNull(selected);
        assertEquals("test", selected.getCode());
    }

    @Test
    void testDefaultExtensionResolutionStrategyWithoutCode() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        registry.register(TestExtension.class, testExtension, null);
        
        // 使用默认解析策略（无code）
        DefaultExtensionResolutionStrategy strategy = new DefaultExtensionResolutionStrategy();
        Map<String, Object> context = new HashMap<>();
        
        List<TestExtension> extensions = manager.getExtensions(TestExtension.class);
        TestExtension selected = strategy.resolve(extensions, context);
        
        assertNotNull(selected);
        assertEquals("test", selected.getCode());
    }

    @Test
    void testDefaultExtensionResolutionStrategyWithEmptyCode() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        registry.register(TestExtension.class, testExtension, null);
        
        // 使用默认解析策略（空code）
        DefaultExtensionResolutionStrategy strategy = new DefaultExtensionResolutionStrategy();
        Map<String, Object> context = new HashMap<>();
        context.put("code", "");
        
        List<TestExtension> extensions = manager.getExtensions(TestExtension.class);
        TestExtension selected = strategy.resolve(extensions, context);
        
        assertNotNull(selected);
        assertEquals("test", selected.getCode());
    }

    // 测试用的扩展点接口和实现
    interface TestExtension extends ExtensionAbility {
        String sayHello();
    }

    @ExtensionInfo(id = "test-impl-1", description = "Test implementation 1")
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

    @ExtensionInfo(id = "test-impl-2", description = "Test implementation 2")
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

    static class TestService {
        @ExtensionAbilityReference(code = "test")
        private TestExtension testExtension;

        public String callExtension() {
            return testExtension.sayHello();
        }
    }
} 