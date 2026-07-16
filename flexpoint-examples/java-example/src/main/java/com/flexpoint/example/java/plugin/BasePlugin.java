package com.flexpoint.example.java.plugin;

import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;

/**
 * 前置基础插件（先于其它插件注册，用于演示装配顺序=注册顺序）。
 *
 * @author xiangganluo
 */
public class BasePlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "example.base";

    @Override
    public String getId() {
        return PLUGIN_ID;
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
