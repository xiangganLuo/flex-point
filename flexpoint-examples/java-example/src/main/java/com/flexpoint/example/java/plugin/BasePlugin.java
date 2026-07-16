package com.flexpoint.example.java.plugin;

import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginCapability;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.plugin.PluginDescriptor;

import java.util.EnumSet;

/**
 * 前置基础插件（被 {@link GreetingSelectorPlugin} 依赖）。
 * <p>用于演示依赖顺序：必须先于依赖它的插件装配。</p>
 *
 * @author xiangganluo
 */
public class BasePlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "example.base";

    private final PluginDescriptor descriptor = PluginDescriptor.builder(PLUGIN_ID, "1.0.0")
            .capabilities(EnumSet.of(PluginCapability.OTHER))
            .order(0)
            .critical(false)
            .build();

    @Override
    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void init(PluginContext context) {
        System.out.println("[BasePlugin] init：准备基础资源");
    }

    @Override
    public void start() {
        System.out.println("[BasePlugin] start：基础能力就绪");
    }
}
