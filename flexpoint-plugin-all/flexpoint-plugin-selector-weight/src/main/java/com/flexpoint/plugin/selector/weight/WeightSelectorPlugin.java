package com.flexpoint.plugin.selector.weight;

import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * 官方内置：权重选择器插件。
 *
 * <p>将 {@link WeightSelector} 注册到 {@link SelectorRegistry}。可选注入随机源（seed 或 {@link Random}），
 * 便于测试或可复现分流。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class WeightSelectorPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "selector.weight";

    private WeightSelector selector;
    private SelectorRegistry registry;

    /** 使用默认随机源。 */
    public WeightSelectorPlugin() {
        this.selector = new WeightSelector();
    }

    /** 使用固定 seed。 */
    public WeightSelectorPlugin(long seed) {
        this.selector = new WeightSelector(seed);
    }

    /** 注入自定义随机源。 */
    public WeightSelectorPlugin(Random random) {
        this.selector = new WeightSelector(random);
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
