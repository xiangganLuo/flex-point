package com.flexpoint.plugin.selector.tenant;

import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * 官方内置：租户选择器插件。
 *
 * <p>将 {@link TenantSelector} 注册到 {@link SelectorRegistry}。tenantId 由业务方
 * 实现 {@link TenantSelector.TenantResolver} 提供；是否回退到默认候选由 {@code fallback} 控制。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class TenantSelectorPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "selector.tenant";

    private TenantSelector selector;
    private SelectorRegistry registry;

    /** 不启用回退。 */
    public TenantSelectorPlugin(TenantSelector.TenantResolver resolver) {
        this(false, resolver);
    }

    public TenantSelectorPlugin(boolean fallback, TenantSelector.TenantResolver resolver) {
        this.selector = new TenantSelector(fallback, resolver);
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
