package com.flexpoint.core.plugin;

/**
 * 插件顶层接口，统一暴露描述信息与生命周期。
 *
 * <p>所有插件必须实现本接口，以便被 {@code PluginManager}
 * 统一注册、解析依赖与编排生命周期。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public interface Plugin extends PluginLifecycle {

    /**
     * 返回插件的元数据描述。
     *
     * @return 插件描述信息（不可为 null）
     */
    PluginDescriptor getDescriptor();
}
