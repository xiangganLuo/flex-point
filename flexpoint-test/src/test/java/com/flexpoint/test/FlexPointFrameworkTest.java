package com.flexpoint.test;

import com.flexpoint.common.annotations.ExtensionAbilityReference;
import com.flexpoint.common.annotations.ExtensionInfo;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.extension.ExtensionAbilityFactory;
import com.flexpoint.core.monitor.DefaultExtensionMonitor;
import com.flexpoint.core.registry.DefaultExtensionRegistry;
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

    private ExtensionAbilityFactory factory;
    private DefaultExtensionRegistry registry;
    private DefaultExtensionMonitor monitor;

    @BeforeEach
    void setUp() {
        registry = new DefaultExtensionRegistry();
        monitor = new DefaultExtensionMonitor();
        factory = new ExtensionAbilityFactory(registry, monitor);
    }

    @Test
    void testBasicExtensionRegistration() {
        // 创建测试扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        
        // 注册扩展点
        registry.register(TestExtension.class, testExtension, null);
        
        // 查找扩展点
        TestExtension found = factory.findAbility(TestExtension.class);
        
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
        
        TestExtension found = factory.findAbility(TestExtension.class, context);
        assertNotNull(found);
        assertEquals("test", found.getCode());
        
        // 使用不同的code查找
        context.put("code", "test2");
        TestExtension found2 = factory.findAbility(TestExtension.class, context);
        assertNotNull(found2);
        assertEquals("test2", found2.getCode());
    }

    @Test
    void testMonitoring() {
        // 创建并注册扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        registry.register(TestExtension.class, testExtension, null);
        
        // 调用扩展点
        TestExtension found = factory.findAbility(TestExtension.class);
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
        TestExtension found = factory.findAbility(TestExtension.class);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testFindAbilityWithNullContext() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        registry.register(TestExtension.class, testExtension, null);
        
        // 使用null context查找
        TestExtension found = factory.findAbility(TestExtension.class, null);
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
        TestExtension found = factory.findAbility(TestExtension.class, context);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testFindAbilityOpt() {
        // 创建扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        registry.register(TestExtension.class, testExtension, null);
        
        // 使用Optional方式查找
        var found = factory.findAbilityOpt(TestExtension.class);
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
        TestExtension found = factory.findAbilityById(TestExtension.class, "test-impl-1");
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
        ExtensionMetadata found = factory.getExtensionMetadata(TestExtension.class, "test-impl-1");
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
        TestExtension found = factory.findAbility(TestExtension.class);
        found.sayHello();
        
        // 获取监控指标
        var metrics = factory.getExtensionMetrics("TestExtensionImpl");
        assertNotNull(metrics);
        assertTrue(metrics.getTotalInvocations() > 0);
    }

    @Test
    void testGetAllExtensionMetrics() {
        // 创建并注册扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        registry.register(TestExtension.class, testExtension, null);
        
        // 调用扩展点
        TestExtension found = factory.findAbility(TestExtension.class);
        found.sayHello();
        
        // 获取所有监控指标
        Map<String, DefaultExtensionMonitor.ExtensionMetrics> allMetrics = factory.getAllExtensionMetrics();
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
        TestExtension found = factory.findAbility(TestExtension.class);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testExtensionNotFound() {
        // 不注册任何扩展点
        
        // 查找扩展点，应该返回null
        TestExtension found = factory.findAbility(TestExtension.class);
        assertNull(found);
    }

    @Test
    void testExtensionNotFoundOpt() {
        // 不注册任何扩展点
        
        // 使用Optional方式查找，应该返回空的Optional
        var found = factory.findAbilityOpt(TestExtension.class);
        assertFalse(found.isPresent());
    }

    @Test
    void testRegistryGetExtensions() {
        // 创建两个不同code的扩展点
        TestExtensionImpl testExtension1 = new TestExtensionImpl();
        TestExtensionImpl2 testExtension2 = new TestExtensionImpl2();
        
        // 注册扩展点
        registry.register(TestExtension.class, testExtension1, null);
        registry.register(TestExtension.class, testExtension2, null);
        
        // 获取所有扩展点
        List<TestExtension> extensions = registry.getExtensions(TestExtension.class);
        assertEquals(2, extensions.size());
        
        // 验证扩展点按优先级排序
        assertEquals("test", extensions.get(0).getCode());
        assertEquals("test2", extensions.get(1).getCode());
    }

    @Test
    void testDefaultExtensionResolutionStrategy() {
        DefaultExtensionResolutionStrategy strategy = new DefaultExtensionResolutionStrategy();
        
        // 测试有code的情况
        Map<String, Object> context = new HashMap<>();
        context.put("code", "test");
        
        TestExtensionImpl testExtension = new TestExtensionImpl();
        List<TestExtension> extensions = List.of(testExtension);
        
        TestExtension found = strategy.resolve(extensions, context);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testDefaultExtensionResolutionStrategyWithoutCode() {
        DefaultExtensionResolutionStrategy strategy = new DefaultExtensionResolutionStrategy();
        
        // 测试没有code的情况
        Map<String, Object> context = new HashMap<>();
        
        TestExtensionImpl testExtension = new TestExtensionImpl();
        List<TestExtension> extensions = List.of(testExtension);
        
        TestExtension found = strategy.resolve(extensions, context);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    @Test
    void testDefaultExtensionResolutionStrategyWithEmptyCode() {
        DefaultExtensionResolutionStrategy strategy = new DefaultExtensionResolutionStrategy();
        
        // 测试空code的情况
        Map<String, Object> context = new HashMap<>();
        context.put("code", "");
        
        TestExtensionImpl testExtension = new TestExtensionImpl();
        List<TestExtension> extensions = List.of(testExtension);
        
        TestExtension found = strategy.resolve(extensions, context);
        assertNotNull(found);
        assertEquals("test", found.getCode());
    }

    // 测试扩展点接口
    interface TestExtension extends ExtensionAbility {
        String sayHello();
    }

    // 测试扩展点实现1
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

    // 测试扩展点实现2
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

    // 测试代理注入
    static class TestService {
        @ExtensionAbilityReference(code = "test")
        private TestExtension testExtension;

        public String callExtension() {
            return testExtension.sayHello();
        }
    }
} 