package com.flexpoint.plugin.selector.ab;

import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 官方内置：A/B 分流选择器插件。
 *
 * <p>将 {@link AbTestSelector} 注册到 {@link SelectorRegistry}。桶配置与
 * {@link AbTestSelector.AbKeyResolver} 通过构造参数传入。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class AbTestSelectorPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "selector.ab";

    private AbTestSelector selector;
    private SelectorRegistry registry;

    public AbTestSelectorPlugin(Map<String, Integer> buckets, AbTestSelector.AbKeyResolver resolver) {
        this.selector = new AbTestSelector(buckets, resolver);
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
