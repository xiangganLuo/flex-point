package com.flexpoint.core.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 * 插件元数据描述。
 *
 * <p>包含插件标识、版本、能力、顺序与依赖等信息，
 * 用于依赖解析、冲突检测与装配排序。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public final class PluginDescriptor {
    private final String pluginId;
    private final String version;
    private final String apiVersion;
    private final int order;
    private final List<PluginDependency> dependencies;
    private final EnumSet<PluginCapability> capabilities;
    private final boolean critical;

    private PluginDescriptor(Builder b) {
        this.pluginId = Objects.requireNonNull(b.pluginId, "pluginId");
        this.version = Objects.requireNonNull(b.version, "version");
        this.apiVersion = (b.apiVersion != null ? b.apiVersion : "1.0");
        this.order = b.order;
        List<PluginDependency> deps = (b.dependencies != null ? b.dependencies : Collections.<PluginDependency>emptyList());
        this.dependencies = new ArrayList<>(deps);
        this.capabilities = (b.capabilities == null ? EnumSet.noneOf(PluginCapability.class) : EnumSet.copyOf(b.capabilities));
        this.critical = b.critical;
        // 构造时做基础校验，避免不合法对象进入后续流程
        PluginDescriptorValidator.validateOrThrow(this);
    }

    public String getPluginId() { return pluginId; }
    public String getVersion() { return version; }
    public String getApiVersion() { return apiVersion; }
    public int getOrder() { return order; }
    public List<PluginDependency> getDependencies() { return Collections.unmodifiableList(new ArrayList<>(dependencies)); }
    public EnumSet<PluginCapability> getCapabilities() { return EnumSet.copyOf(capabilities); }
    public boolean isCritical() { return critical; }

    public static Builder builder(String pluginId, String version) { return new Builder(pluginId, version); }

    /**
     * 构建器：便于描述信息的渐进式构造与校验。
     */
    public static final class Builder {
        private final String pluginId;
        private final String version;
        private String apiVersion;
        private int order;
        private List<PluginDependency> dependencies;
        private EnumSet<PluginCapability> capabilities;
        private boolean critical;

        private Builder(String pluginId, String version) {
            this.pluginId = pluginId;
            this.version = version;
        }

        /** 设置兼容的插件 API 版本 */
        public Builder apiVersion(String v) { this.apiVersion = v; return this; }
        /** 设置加载顺序（数值越小越先装配） */
        public Builder order(int o) { this.order = o; return this; }
        /** 声明依赖项（将用于依赖解析） */
        public Builder dependencies(List<PluginDependency> deps) { this.dependencies = deps; return this; }
        /** 声明插件能力集合（用于冲突检测） */
        public Builder capabilities(EnumSet<PluginCapability> caps) { this.capabilities = caps; return this; }
        /** 标记为关键插件（失败将阻断构建） */
        public Builder critical(boolean c) { this.critical = c; return this; }
        public PluginDescriptor build() { return new PluginDescriptor(this); }
    }
}
