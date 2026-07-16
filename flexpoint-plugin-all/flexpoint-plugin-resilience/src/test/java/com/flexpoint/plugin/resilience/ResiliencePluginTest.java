package com.flexpoint.plugin.resilience;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.event.EventBus;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.ext.interceptor.DefaultInterceptorRegistry;
import com.flexpoint.core.ext.interceptor.ExtInvocationInterceptor;
import com.flexpoint.core.ext.interceptor.InterceptorRegistry;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.selector.SelectorRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ResiliencePlugin} 生命周期测试：验证 start 后熔断/超时拦截器已注册、stop 后注销。
 *
 * @author xiangganluo
 */
class ResiliencePluginTest {

    /** 仅暴露 InterceptorRegistry 的最小化 PluginContext 测试替身。 */
    private static final class TestPluginContext implements PluginContext {
        private final InterceptorRegistry registry;

        TestPluginContext(InterceptorRegistry registry) {
            this.registry = registry;
        }

        @Override public ExtAbilityRegistry extRegistry() { return null; }
        @Override public SelectorRegistry selectorRegistry() { return null; }
        @Override public EventBus eventBus() { return null; }
        @Override public ExtMonitor monitor() { return null; }
        @Override public FlexPointConfig config() { return null; }
        @Override public InterceptorRegistry interceptorRegistry() { return registry; }
    }

    @Test
    void startRegistersBothInterceptorsAndStopUnregisters() throws Exception {
        DefaultInterceptorRegistry registry = new DefaultInterceptorRegistry();
        ResiliencePlugin plugin = new ResiliencePlugin(0L); // 关闭超时以避免创建线程池

        plugin.init(new TestPluginContext(registry));
        plugin.start();

        List<ExtInvocationInterceptor> interceptors = registry.getInterceptors();
        assertEquals(2, interceptors.size(), "应注册熔断与超时两个拦截器");
        // 按 order 升序：熔断(100) 在超时(400) 之前
        assertTrue(interceptors.get(0) instanceof CircuitBreakerInterceptor);
        assertTrue(interceptors.get(1) instanceof TimeoutInterceptor);
        assertEquals(100, interceptors.get(0).order());
        assertEquals(400, interceptors.get(1).order());

        plugin.stop();
        assertEquals(0, registry.getInterceptors().size(), "stop 后应全部注销");

        plugin.destroy();
        assertEquals("resilience.guard", plugin.getId());
    }
}
