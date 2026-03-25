package com.flexpoint.core.plugin;

import java.util.EnumSet;
import java.util.regex.Pattern;

/**
 * 插件描述符校验器。
 *
 * <p>负责对 {@link PluginDescriptor} 进行基础一致性校验，
 * 包括：空/非法 pluginId、非法版本号、能力声明的有效性等。</p>
 *
 * <p>版本号校验采用简化的 SemVer 规则：MAJOR.MINOR.PATCH，
 * 允许后缀（如 -alpha、+build）。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public final class PluginDescriptorValidator {

    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Za-z0-9_.:-]+$");
    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+(?:[-+].*)?$");

    private PluginDescriptorValidator() {}

    /**
     * 校验并在失败时抛出 {@link IllegalArgumentException}。
     *
     * @param descriptor 待校验的描述符
     * @throws IllegalArgumentException 当发现非法字段时
     */
    public static void validateOrThrow(PluginDescriptor descriptor) {
        if (descriptor == null) {
            throw new IllegalArgumentException("descriptor must not be null");
        }
        String id = descriptor.getPluginId();
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("pluginId must not be empty");
        }
        if (!ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("pluginId contains illegal characters: " + id);
        }

        String version = descriptor.getVersion();
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("version must not be empty");
        }
        if (!VERSION_PATTERN.matcher(version).matches()) {
            throw new IllegalArgumentException("version is not a valid semver: " + version);
        }

        EnumSet<PluginCapability> caps = descriptor.getCapabilities();
        if (caps == null) {
            throw new IllegalArgumentException("capabilities must not be null");
        }
        // EnumSet 本身去重，但仍显式校验防止实现被修改
        EnumSet<PluginCapability> copy = EnumSet.copyOf(caps);
        if (copy.size() != caps.size()) {
            throw new IllegalArgumentException("capabilities contains duplicates");
        }
    }
}

