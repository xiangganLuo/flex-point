package com.flexpoint.core.plugin;

/**
 * 插件顶层接口，统一暴露标识与生命周期。
 *
 * <p>所有插件必须实现本接口，以便被 {@code PluginManager}
 * 统一注册与编排生命周期。插件模型保持极简：仅以 {@link #getId()}
 * 作为全局唯一标识，不再承载依赖/顺序/版本/能力/关键性等治理概念。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public interface Plugin extends PluginLifecycle {

    /**
     * 返回插件的全局唯一标识。
     *
     * @return 插件 ID（不可为 null / 空）
     */
    String getId();
}
