package com.flexpoint.plugin.selector.ab;

import com.flexpoint.core.context.FlexPointContext;
import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Function;

/**
 * 官方内置：A/B 分流选择器插件。
 *
 * <p>将 {@link AbTestSelector} 注册到 {@link SelectorRegistry}。桶配置与 keyProvider
 * 通过构造参数传入，便于接入层按属性装配。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class AbTestSelectorPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "selector.ab";

    private AbTestSelector selector;
    private SelectorRegistry registry;

    /** 默认以 uid 作为分流 key。 */
    public AbTestSelectorPlugin(Map<String, Integer> buckets) {
        this.selector = new AbTestSelector(buckets);
    }

    public AbTestSelectorPlugin(Map<String, Integer> buckets, Function<FlexPointContext, String> keyProvider) {
        this.selector = new AbTestSelector(buckets, keyProvider);
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
