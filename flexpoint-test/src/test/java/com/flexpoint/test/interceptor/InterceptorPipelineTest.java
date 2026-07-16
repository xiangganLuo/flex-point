package com.flexpoint.test.interceptor;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.interceptor.DefaultExtInvocation;
import com.flexpoint.core.ext.interceptor.DefaultInterceptorRegistry;
import com.flexpoint.core.ext.interceptor.ExtInvocation;
import com.flexpoint.core.ext.interceptor.ExtInvocationInterceptor;
import com.flexpoint.core.ext.interceptor.ExtInvocationTerminal;
import com.flexpoint.core.ext.interceptor.InterceptorRegistry;
import com.flexpoint.core.event.EventType;
import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.selector.AbstractSelector;
import com.flexpoint.core.selector.Selector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 调用拦截器 SPI 单测：注册表排序、可重入链（重试）、以及经 FlexPoint 的 around 集成
 * （拦截器环绕事件埋点终端，重试产生多组调用事件）。
 *
 * @author xiangganluo
 */
public class InterceptorPipelineTest {

    // =============== 注册表排序 ===============

    @Test
    void registry_returns_interceptors_sorted_by_order() {
        InterceptorRegistry reg = new DefaultInterceptorRegistry();
        reg.register(namedOrder("c", 30));
        reg.register(namedOrder("a", 10));
        reg.register(namedOrder("b", 20));
        List<ExtInvocationInterceptor> list = reg.getInterceptors();
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals("a", list.get(0).name());
        Assertions.assertEquals("b", list.get(1).name());
        Assertions.assertEquals("c", list.get(2).name());
    }

    // =============== 可重入链（重试语义） ===============

    @Test
    void invocation_chain_is_reentrant_for_retry() throws Throwable {
        // 终端：前两次抛异常，第三次成功
        AtomicInteger terminalCalls = new AtomicInteger();
        ExtInvocationTerminal terminal = () -> {
            int n = terminalCalls.incrementAndGet();
            if (n < 3) throw new IllegalStateException("fail-" + n);
            return "ok";
        };
        // 重试拦截器：最多 3 次调用 proceed()
        ExtInvocationInterceptor retry = new ExtInvocationInterceptor() {
            @Override public Object intercept(ExtInvocation inv) throws Throwable {
                Throwable last = null;
                for (int i = 0; i < 3; i++) {
                    try { return inv.proceed(); }
                    catch (Throwable t) { last = t; }
                }
                throw last;
            }
        };
        List<ExtInvocationInterceptor> chain = new ArrayList<>();
        chain.add(retry);

        Method m = Sample.class.getMethod("run");
        ExtInvocation invocation = new DefaultExtInvocation(new Sample(), m, new Object[0], chain, terminal);
        Object result = invocation.proceed();

        Assertions.assertEquals("ok", result);
        Assertions.assertEquals(3, terminalCalls.get(), "重试应使终端被调用 3 次（可重入）");
    }

    // =============== 经 FlexPoint 的集成 ===============

    @FpSelector("firstSelector")
    interface DemoAbility extends ExtAbility {
        String exec();
    }

    static class FailTwiceAbility implements DemoAbility {
        final AtomicInteger calls = new AtomicInteger();
        @Override public String getCode() { return "a"; }
        @Override public String exec() {
            int n = calls.incrementAndGet();
            if (n < 3) throw new IllegalStateException("biz-fail-" + n);
            return "done";
        }
    }

    static class FirstSelector extends AbstractSelector {
        @Override protected <T extends ExtAbility> List<T> filter(List<T> candidates) { return candidates; }
        @Override public String getName() { return "firstSelector"; }
    }

    /** 注册一个重试拦截器的测试插件。 */
    static class RetryInterceptorPlugin extends AbstractPlugin {
        private InterceptorRegistry registry;
        @Override public String getId() { return "test.retry-interceptor"; }
        @Override public void init(PluginContext ctx) { this.registry = ctx.interceptorRegistry(); }
        @Override public void start() {
            registry.register(new ExtInvocationInterceptor() {
                @Override public Object intercept(ExtInvocation inv) throws Throwable {
                    Throwable last = null;
                    for (int i = 0; i < 3; i++) {
                        try { return inv.proceed(); }
                        catch (Throwable t) { last = t; }
                    }
                    throw last;
                }
                @Override public int order() { return 300; }
            });
        }
    }

    @Test
    void retry_interceptor_wraps_event_terminal_and_retries() {
        List<Plugin> plugins = new ArrayList<>();
        plugins.add(new RetryInterceptorPlugin());
        FlexPoint fp = FlexPointBuilder.create().withPlugins(plugins).build();
        fp.registerSelector(new FirstSelector());
        FailTwiceAbility ability = new FailTwiceAbility();
        fp.register(ability);

        final List<EventType> events = new ArrayList<>();
        fp.getEventBus().subscribe(ctx -> events.add(ctx.getEventType()));

        DemoAbility proxy = fp.findAbility(DemoAbility.class);
        Assertions.assertNotNull(proxy);
        // 前两次业务异常被重试拦截器吞下，第三次成功
        String r = proxy.exec();
        Assertions.assertEquals("done", r);
        Assertions.assertEquals(3, ability.calls.get(), "重试拦截器应使目标方法被调用 3 次");

        // 事件埋点为最内层拦截器：每次实际调用都发事件 → 2 次 FAIL + 1 次 SUCCESS
        long before = events.stream().filter(e -> e == EventType.INVOKE_BEFORE).count();
        long fail = events.stream().filter(e -> e == EventType.INVOKE_FAIL).count();
        long success = events.stream().filter(e -> e == EventType.INVOKE_SUCCESS).count();
        Assertions.assertEquals(3, before);
        Assertions.assertEquals(2, fail);
        Assertions.assertEquals(1, success);

        fp.shutdown();
    }

    // =============== helpers ===============

    static class Sample implements ExtAbility {
        @Override public String getCode() { return "s"; }
        public String run() { return "x"; }
    }

    private static ExtInvocationInterceptor namedOrder(String name, int order) {
        return new ExtInvocationInterceptor() {
            @Override public Object intercept(ExtInvocation invocation) throws Throwable { return invocation.proceed(); }
            @Override public int order() { return order; }
            @Override public String name() { return name; }
        };
    }
}
