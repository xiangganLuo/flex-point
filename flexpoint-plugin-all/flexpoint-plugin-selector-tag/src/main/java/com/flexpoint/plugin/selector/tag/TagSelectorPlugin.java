package com.flexpoint.plugin.selector.tag;

import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * 官方内置：标签选择器插件。
 *
 * <p>将 {@link TagSelector} 注册到 {@link SelectorRegistry}。无需任何构造参数，
 * 路由信息取自标准上下文的 labels。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class TagSelectorPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "selector.tag";

    private TagSelector selector;
    private SelectorRegistry registry;

    public TagSelectorPlugin() {
        this.selector = new TagSelector();
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
