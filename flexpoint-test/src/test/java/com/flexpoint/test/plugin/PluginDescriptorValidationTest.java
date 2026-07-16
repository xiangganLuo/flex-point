package com.flexpoint.test.plugin;

import com.flexpoint.core.plugin.PluginCapability;
import com.flexpoint.core.plugin.PluginDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

/**
 * 插件描述符构造期校验用例（覆盖 Phase B 任务 A1 校验器）。
 *
 * @author xiangganluo
 */
public class PluginDescriptorValidationTest {

    @Test
    void valid_descriptor_builds_successfully() {
        PluginDescriptor d = PluginDescriptor.builder("core.selector.code", "1.2.3")
                .capabilities(EnumSet.of(PluginCapability.SELECTOR))
                .build();
        Assertions.assertEquals("core.selector.code", d.getPluginId());
        Assertions.assertEquals("1.2.3", d.getVersion());
        // apiVersion 未指定时应有默认值
        Assertions.assertNotNull(d.getApiVersion());
    }

    @Test
    void semver_with_suffix_is_accepted() {
        Assertions.assertDoesNotThrow(() ->
                PluginDescriptor.builder("p", "1.0.0-alpha")
                        .capabilities(EnumSet.of(PluginCapability.OTHER)).build());
        Assertions.assertDoesNotThrow(() ->
                PluginDescriptor.builder("p", "2.3.4+build.5")
                        .capabilities(EnumSet.of(PluginCapability.OTHER)).build());
    }

    @Test
    void empty_id_is_rejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                PluginDescriptor.builder("  ", "1.0.0")
                        .capabilities(EnumSet.of(PluginCapability.OTHER)).build());
    }

    @Test
    void illegal_id_characters_are_rejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                PluginDescriptor.builder("bad id!", "1.0.0")
                        .capabilities(EnumSet.of(PluginCapability.OTHER)).build());
    }

    @Test
    void invalid_semver_is_rejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                PluginDescriptor.builder("p", "1.0")
                        .capabilities(EnumSet.of(PluginCapability.OTHER)).build());
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                PluginDescriptor.builder("p", "abc")
                        .capabilities(EnumSet.of(PluginCapability.OTHER)).build());
    }
}
