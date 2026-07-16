package com.flexpoint.example.java.plugin;

import com.flexpoint.core.plugin.AbstractPlugin;

/**
 * "故障"插件：start 抛异常，用于演示降级——
 * 插件失败不阻断构建，其它插件仍正常工作。
 *
 * @author xiangganluo
 */
public class FaultyOptionalPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "example.faulty";

    @Override
    public String getId() {
        return PLUGIN_ID;
    }

    @Override
    public void start() {
        throw new IllegalStateException("模拟插件启动失败");
    }
}
