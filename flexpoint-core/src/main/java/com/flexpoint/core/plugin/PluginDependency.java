package com.flexpoint.core.plugin;

import java.util.Objects;

/**
 * 插件依赖项。
 *
 * <p>描述当前插件对其他插件的依赖关系，支持版本约束占位符。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public final class PluginDependency {
    /** 依赖的插件唯一标识 */
    private final String pluginId;
    /** 版本约束（预留，当前阶段不解析语义） */
    private final String versionConstraint;

    public PluginDependency(String pluginId, String versionConstraint) {
        this.pluginId = Objects.requireNonNull(pluginId, "pluginId");
        this.versionConstraint = versionConstraint;
    }

    public String getPluginId() { return pluginId; }
    public String getVersionConstraint() { return versionConstraint; }
}
