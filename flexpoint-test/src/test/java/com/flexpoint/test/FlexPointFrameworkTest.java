package com.flexpoint.test;

import com.flexpoint.common.ExtensionAbility;
import com.flexpoint.common.annotations.ExtensionInfo;
import com.flexpoint.common.annotations.ExtensionAbilityReference;
import com.flexpoint.core.ExtensionAbilityFactory;
import com.flexpoint.core.cache.DefaultExtensionCacheManager;
import com.flexpoint.core.monitor.DefaultExtensionMonitor;
import com.flexpoint.core.registry.DefaultExtensionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
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
    private DefaultExtensionCacheManager cacheManager;
    private DefaultExtensionMonitor monitor;

    @BeforeEach
    void setUp() {
        registry = new DefaultExtensionRegistry();
        cacheManager = new DefaultExtensionCacheManager();
        monitor = new DefaultExtensionMonitor();
        factory = new ExtensionAbilityFactory(registry, cacheManager, monitor);
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
    void testCacheFunctionality() {
        // 创建并注册扩展点
        TestExtensionImpl testExtension = new TestExtensionImpl();
        registry.register(TestExtension.class, testExtension, null);
        
        // 第一次查找，应该从注册中心获取
        TestExtension found1 = factory.findAbility(TestExtension.class);
        assertNotNull(found1);
        
        // 第二次查找，应该从缓存获取
        TestExtension found2 = factory.findAbility(TestExtension.class);
        assertNotNull(found2);
        
        // 验证缓存统计
        var stats = cacheManager.getCacheStatistics();
        assertTrue(stats.getHitCount() > 0);
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