package com.flexpoint.test;

import com.flexpoint.core.registry.ExtensionAbility;
import com.flexpoint.core.resolution.AbstractExtensionResolutionStrategy;
import com.flexpoint.core.resolution.DefaultExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ExtensionResolutionStrategy;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 扩展点解析策略测试
 *
 * @author xiangganluo
 * @version 1.0.0
 */
class ExtensionResolutionStrategyTest {

    @Test
    void testDefaultExtensionResolutionStrategy() {
        ExtensionResolutionStrategy strategy = new DefaultExtensionResolutionStrategy();
        
        // 创建测试扩展点
        TestExtensionImpl ext1 = new TestExtensionImpl();
        TestExtensionImpl2 ext2 = new TestExtensionImpl2();
        List<TestExtension> extensions = Arrays.asList(ext1, ext2);
        
        // 测试有code的情况
        Map<String, Object> context = new HashMap<>();
        context.put("code", "test");
        
        TestExtension result = strategy.resolve(extensions, context);
        assertNotNull(result);
        assertEquals("test", result.getCode());
    }

    @Test
    void testDefaultExtensionResolutionStrategyWithEmptyCode() {
        ExtensionResolutionStrategy strategy = new DefaultExtensionResolutionStrategy();
        
        // 创建测试扩展点
        TestExtensionImpl ext1 = new TestExtensionImpl();
        TestExtensionImpl2 ext2 = new TestExtensionImpl2();
        List<TestExtension> extensions = Arrays.asList(ext1, ext2);
        
        // 测试空code的情况
        Map<String, Object> context = new HashMap<>();
        context.put("code", "");
        
        TestExtension result = strategy.resolve(extensions, context);
        assertNotNull(result);
        // 应该返回第一个扩展点作为默认值
        assertEquals("test", result.getCode());
    }

    @Test
    void testDefaultExtensionResolutionStrategyWithNullCode() {
        ExtensionResolutionStrategy strategy = new DefaultExtensionResolutionStrategy();
        
        // 创建测试扩展点
        TestExtensionImpl ext1 = new TestExtensionImpl();
        TestExtensionImpl2 ext2 = new TestExtensionImpl2();
        List<TestExtension> extensions = Arrays.asList(ext1, ext2);
        
        // 测试null code的情况
        Map<String, Object> context = new HashMap<>();
        context.put("code", null);
        
        TestExtension result = strategy.resolve(extensions, context);
        assertNotNull(result);
        // 应该返回第一个扩展点作为默认值
        assertEquals("test", result.getCode());
    }

    @Test
    void testDefaultExtensionResolutionStrategyWithNoCode() {
        ExtensionResolutionStrategy strategy = new DefaultExtensionResolutionStrategy();
        
        // 创建测试扩展点
        TestExtensionImpl ext1 = new TestExtensionImpl();
        TestExtensionImpl2 ext2 = new TestExtensionImpl2();
        List<TestExtension> extensions = Arrays.asList(ext1, ext2);
        
        // 测试没有code的情况
        Map<String, Object> context = new HashMap<>();
        
        TestExtension result = strategy.resolve(extensions, context);
        assertNotNull(result);
        // 应该返回第一个扩展点作为默认值
        assertEquals("test", result.getCode());
    }

    @Test
    void testDefaultExtensionResolutionStrategyWithNullContext() {
        ExtensionResolutionStrategy strategy = new DefaultExtensionResolutionStrategy();
        
        // 创建测试扩展点
        TestExtensionImpl ext1 = new TestExtensionImpl();
        TestExtensionImpl2 ext2 = new TestExtensionImpl2();
        List<TestExtension> extensions = Arrays.asList(ext1, ext2);
        
        // 测试null context的情况
        TestExtension result = strategy.resolve(extensions, null);
        assertNotNull(result);
        // 应该返回第一个扩展点作为默认值
        assertEquals("test", result.getCode());
    }

    @Test
    void testDefaultExtensionResolutionStrategyWithNonExistentCode() {
        ExtensionResolutionStrategy strategy = new DefaultExtensionResolutionStrategy();
        
        // 创建测试扩展点
        TestExtensionImpl ext1 = new TestExtensionImpl();
        TestExtensionImpl2 ext2 = new TestExtensionImpl2();
        List<TestExtension> extensions = Arrays.asList(ext1, ext2);
        
        // 测试不存在的code
        Map<String, Object> context = new HashMap<>();
        context.put("code", "non-existent");
        
        TestExtension result = strategy.resolve(extensions, context);
        assertNotNull(result);
        // 应该返回第一个扩展点作为默认值
        assertEquals("test", result.getCode());
    }

    @Test
    void testDefaultExtensionResolutionStrategyWithEmptyExtensions() {
        ExtensionResolutionStrategy strategy = new DefaultExtensionResolutionStrategy();
        
        // 测试空扩展点列表
        List<TestExtension> extensions = Arrays.asList();
        Map<String, Object> context = new HashMap<>();
        context.put("code", "test");
        
        TestExtension result = strategy.resolve(extensions, context);
        assertNull(result);
    }

    @Test
    void testDefaultExtensionResolutionStrategyWithNullExtensions() {
        ExtensionResolutionStrategy strategy = new DefaultExtensionResolutionStrategy();
        
        // 测试null扩展点列表
        Map<String, Object> context = new HashMap<>();
        context.put("code", "test");
        
        TestExtension result = strategy.resolve(null, context);
        assertNull(result);
    }

    @Test
    void testCustomExtensionResolutionStrategy() {
        ExtensionResolutionStrategy strategy = new CustomExtensionResolutionStrategy();
        
        // 创建测试扩展点
        TestExtensionImpl ext1 = new TestExtensionImpl();
        TestExtensionImpl2 ext2 = new TestExtensionImpl2();
        List<TestExtension> extensions = Arrays.asList(ext1, ext2);
        
        // 测试自定义解析策略
        Map<String, Object> context = new HashMap<>();
        context.put("appCode", "test2");
        
        TestExtension result = strategy.resolve(extensions, context);
        assertNotNull(result);
        assertEquals("test2", result.getCode());
    }

    @Test
    void testCustomExtensionResolutionStrategyWithFallback() {
        ExtensionResolutionStrategy strategy = new CustomExtensionResolutionStrategy();
        
        // 创建测试扩展点
        TestExtensionImpl ext1 = new TestExtensionImpl();
        TestExtensionImpl2 ext2 = new TestExtensionImpl2();
        List<TestExtension> extensions = Arrays.asList(ext1, ext2);
        
        // 测试自定义解析策略回退
        Map<String, Object> context = new HashMap<>();
        context.put("appCode", "non-existent");
        
        TestExtension result = strategy.resolve(extensions, context);
        assertNotNull(result);
        // 应该返回第一个扩展点作为默认值
        assertEquals("test", result.getCode());
    }

    @Test
    void testStrategyName() {
        ExtensionResolutionStrategy defaultStrategy = new DefaultExtensionResolutionStrategy();
        ExtensionResolutionStrategy customStrategy = new CustomExtensionResolutionStrategy();
        
        assertEquals("DefaultExtensionResolutionStrategy", defaultStrategy.getStrategyName());
        assertEquals("CustomExtensionResolutionStrategy", customStrategy.getStrategyName());
    }

    @Test
    void testAbstractExtensionResolutionStrategy() {
        // 测试抽象类的默认实现
        ExtensionResolutionStrategy strategy = new AbstractExtensionResolutionStrategy() {
            @Override
            protected String extractCode(Map<String, Object> context) {
                return "test";
            }
            
            @Override
            public String getStrategyName() {
                return "TestStrategy";
            }
        };
        
        // 创建测试扩展点
        TestExtensionImpl ext1 = new TestExtensionImpl();
        List<TestExtension> extensions = Arrays.asList(ext1);
        
        TestExtension result = strategy.resolve(extensions, null);
        assertNotNull(result);
        assertEquals("test", result.getCode());
    }

    // 自定义解析策略
    static class CustomExtensionResolutionStrategy extends AbstractExtensionResolutionStrategy {
        @Override
        protected String extractCode(Map<String, Object> context) {
            if (context != null && context.containsKey("appCode")) {
                String code = (String) context.get("appCode");
                if (code != null && !code.trim().isEmpty()) {
                    return code;
                }
            }
            return "default";
        }
        
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