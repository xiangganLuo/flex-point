package com.flexpoint.plugin.selector.code;

import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 官方内置：Code 选择器插件。
 *
 * <p>将 {@link CodeSelector} 以插件方式注册到 {@link SelectorRegistry}。
 * 业务方需通过构造传入 {@link CodeSelector.CodeResolver} 以解析 code。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class CodeSelectorPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "core.selector.code";

    private CodeSelector selector;
    private SelectorRegistry registry;

    public CodeSelectorPlugin(CodeSelector.CodeResolver resolver) {
        Objects.requireNonNull(resolver, "CodeResolver must not be null");
        this.selector = new CodeSelector(resolver);
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
