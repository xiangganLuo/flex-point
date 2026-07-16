package com.flexpoint.plugin.selector.cache;

import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.selector.Selector;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 官方内置：缓存选择器插件（装饰器）。
 *
 * <p>将 {@link CachingSelector} 注册到 {@link SelectorRegistry}。被包装的 delegate、TTL 与名称
 * 通过构造参数传入。注意 delegate 本身若也需要注册，请由其对应插件负责；本插件仅注册缓存装饰器。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class CachingSelectorPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "selector.cache";

    private CachingSelector selector;
    private SelectorRegistry registry;

    /** 永不过期，名称沿用 delegate。 */
    public CachingSelectorPlugin(Selector delegate) {
        Objects.requireNonNull(delegate, "delegate must not be null");
        this.selector = new CachingSelector(delegate);
    }

    /** 指定 TTL，名称沿用 delegate。 */
    public CachingSelectorPlugin(Selector delegate, long ttlMillis) {
        Objects.requireNonNull(delegate, "delegate must not be null");
        this.selector = new CachingSelector(delegate, ttlMillis);
    }

    /** 指定 TTL 与名称。 */
    public CachingSelectorPlugin(Selector delegate, long ttlMillis, String name) {
        Objects.requireNonNull(delegate, "delegate must not be null");
        this.selector = new CachingSelector(delegate, ttlMillis, name);
    }

    @Override
    public String getId() {
        return PLUGIN_ID;
    }

    @Override
    public void init(PluginContext context) {
        this.registry = context.selectorRegistry();
        log.debug("[{}] init: 获取 SelectorRegistry", PLUGIN_ID);
    }

    @Override
    public void start() {
        log.debug("[{}] start: 注册选择器 {}", PLUGIN_ID, selector.getName());
        registry.register(selector);
    }

    @Override
    public void stop() {
        if (registry != null && selector != null) {
            log.debug("[{}] stop: 反注册选择器 {}", PLUGIN_ID, selector.getName());
            registry.unregister(selector.getName());
        }
    }

    @Override
    public void destroy() {
        this.registry = null;
        this.selector = null;
    }
}
