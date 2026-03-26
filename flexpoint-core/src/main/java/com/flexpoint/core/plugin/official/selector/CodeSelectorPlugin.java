package com.flexpoint.core.plugin.official.selector;

import com.flexpoint.core.plugin.*;
import com.flexpoint.core.selector.SelectorRegistry;
import com.flexpoint.core.plugin.official.selector.resolves.CodeSelector;

import java.util.EnumSet;
import java.util.Objects;

/**
 * 官方内置：Code 选择器插件。
 *
 * <p>将 {@link CodeSelector} 以插件方式注册到 {@link SelectorRegistry}。
 * 业务方需通过构造传入 {@link CodeSelector.CodeResolver} 以解析 code。</p>
 */
public final class CodeSelectorPlugin implements Plugin {

    public static final String PLUGIN_ID = "core.selector.code";

    private final PluginDescriptor descriptor;
    private CodeSelector selector;
    private SelectorRegistry registry;

    public CodeSelectorPlugin(CodeSelector.CodeResolver resolver) {
        Objects.requireNonNull(resolver, "CodeResolver must not be null");
        this.selector = new CodeSelector(resolver);
        this.descriptor = PluginDescriptor.builder(PLUGIN_ID, "1.0.0")
                .capabilities(EnumSet.of(PluginCapability.SELECTOR))
                .order(10)
                .critical(false)
                .build();
    }

    @Override
    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void init(PluginContext context) {
        this.registry = context.selectorRegistry();
    }

    @Override
    public void start() {
        registry.register(selector);
    }

    @Override
    public void stop() {
        if (registry != null && selector != null) {
            registry.unregister(selector.getName());
        }
    }

    @Override
    public void destroy() {
        this.registry = null;
        this.selector = null;
    }
}
