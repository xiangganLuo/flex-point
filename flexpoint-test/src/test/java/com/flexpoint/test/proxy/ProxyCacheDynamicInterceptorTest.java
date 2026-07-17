package com.flexpoint.test.proxy;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.interceptor.ExtInvocation;
import com.flexpoint.core.ext.interceptor.ExtInvocationInterceptor;
import com.flexpoint.core.ext.interceptor.InterceptorRegistry;
import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.selector.AbstractSelector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 修复 4/4b 回归：
 * <ol>
 *   <li>同一 (extType, ability) 多次 findAbility 返回同一代理实例（==），代理被缓存复用；</li>
 *   <li>运行期通过 enable/disable 增删拦截器后，<b>已缓存的代理</b>在后续调用中能反映该增删。</li>
 * </ol>
 *
 * @author xiangganluo
 */
public class ProxyCacheDynamicInterceptorTest {

    @FpSelector("firstSelector")
    interface DemoAbility extends ExtAbility {
        String exec();
    }

    static class DemoAbilityImpl implements DemoAbility {
        @Override public String getCode() { return "a"; }
        @Override public String exec() { return "done"; }
    }

    static class FirstSelector extends AbstractSelector {
        @Override protected <T extends ExtAbility> List<T> filter(List<T> candidates) { return candidates; }
        @Override public String getName() { return "firstSelector"; }
    }

    /** 计数拦截器插件：start 注册、stop 注销同一拦截器实例，拦截器每次调用递增共享计数器。 */
    static class CountingInterceptorPlugin extends AbstractPlugin {
        static final String ID = "test.counting-interceptor";
        final AtomicInteger count = new AtomicInteger();
        private InterceptorRegistry registry;
        private final ExtInvocationInterceptor interceptor = new ExtInvocationInterceptor() {
            @Override public Object intercept(ExtInvocation inv) throws Throwable {
                count.incrementAndGet();
                return inv.proceed();
            }
            @Override public int order() { return 100; }
            @Override public String name() { return "counting"; }
        };

        @Override public String getId() { return ID; }
        @Override public void init(PluginContext ctx) { this.registry = ctx.interceptorRegistry(); }
        @Override public void start() { registry.register(interceptor); }
        @Override public void stop() { registry.unregister(interceptor); }
    }

    @Test
    void same_ability_returns_cached_proxy() {
        FlexPoint fp = FlexPointBuilder.create().build();
        fp.registerSelector(new FirstSelector());
        fp.register(new DemoAbilityImpl());

        DemoAbility p1 = fp.findAbility(DemoAbility.class);
        DemoAbility p2 = fp.findAbility(DemoAbility.class);

        Assertions.assertNotNull(p1);
        Assertions.assertSame(p1, p2, "同一 (extType, ability) 应复用同一代理实例");

        fp.shutdown();
    }

    @Test
    void cached_proxy_reflects_runtime_interceptor_toggle() {
        List<Plugin> plugins = new ArrayList<>();
        CountingInterceptorPlugin plugin = new CountingInterceptorPlugin();
        plugins.add(plugin);
        FlexPoint fp = FlexPointBuilder.create().withPlugins(plugins).build();
        fp.registerSelector(new FirstSelector());
        fp.register(new DemoAbilityImpl());

        // 缓存一个代理实例，后续始终复用它，验证「已缓存的代理」感知拦截器动态增删
        DemoAbility proxy = fp.findAbility(DemoAbility.class);

        // 初始：拦截器已注册，调用一次计数 +1
        proxy.exec();
        Assertions.assertEquals(1, plugin.count.get());

        // disable：注销拦截器；已缓存代理的后续调用不应再触发拦截器
        fp.disablePlugin(CountingInterceptorPlugin.ID);
        proxy.exec();
        Assertions.assertEquals(1, plugin.count.get(), "注销后已缓存代理不应再执行拦截器");

        // enable：重新注册拦截器；已缓存代理的后续调用应重新触发拦截器
        fp.enablePlugin(CountingInterceptorPlugin.ID);
        proxy.exec();
        Assertions.assertEquals(2, plugin.count.get(), "重新注册后已缓存代理应再次执行拦截器");

        fp.shutdown();
    }
}
