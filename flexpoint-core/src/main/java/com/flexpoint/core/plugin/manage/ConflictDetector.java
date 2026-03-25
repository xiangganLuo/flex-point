package com.flexpoint.core.plugin.manage;

import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.PluginCapability;
import com.flexpoint.core.plugin.PluginDescriptor;
import com.flexpoint.core.plugin.exception.PluginConflictException;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * 简单能力冲突检测：默认相同 capability 只允许一个主插件。
 *
 * <p>可通过外层配置放宽为白名单（后续阶段支持）。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
final class ConflictDetector {
    static void detect(Collection<Plugin> plugins, Set<PluginCapability> singletons) {
        Map<PluginCapability, String> owner = new EnumMap<PluginCapability, String>(PluginCapability.class);
        for (Plugin p : plugins) {
            PluginDescriptor d = p.getDescriptor();
            for (PluginCapability cap : d.getCapabilities()) {
                if (singletons.contains(cap)) {
                    String prev = owner.putIfAbsent(cap, d.getPluginId());
                    if (prev != null) {
                        throw new PluginConflictException("Capability conflict: " + cap + " by " + prev + " and " + d.getPluginId());
                    }
                }
            }
        }
    }
}
