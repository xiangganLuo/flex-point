package com.flexpoint.example.java.plugin;

import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginCapability;
import com.flexpoint.core.plugin.PluginDescriptor;

import java.util.EnumSet;

/**
 * 非关键的"故障"插件：start 抛异常，用于演示降级——
 * 非关键插件失败不阻断构建，其它插件仍正常工作。
 *
 * @author xiangganluo
 */
public class FaultyOptionalPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "example.faulty";

    private final PluginDescriptor descriptor = PluginDescriptor.builder(PLUGIN_ID, "1.0.0")
            .capabilities(EnumSet.of(PluginCapability.OTHER))
            .order(20)
            .critical(false)
            .build();

    @Override
    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void start() {
        throw new IllegalStateException("模拟非关键插件启动失败");
    }
}
