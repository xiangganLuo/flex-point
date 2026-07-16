package com.flexpoint.test.springboot;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.PluginState;
import com.flexpoint.plugin.observability.ObservabilityPlugin;
import com.flexpoint.plugin.selector.code.CodeSelector;
import com.flexpoint.plugin.selector.code.CodeSelectorPlugin;
import com.flexpoint.plugin.selector.codeversion.CodeVersionSelector;
import com.flexpoint.plugin.selector.codeversion.CodeVersionSelectorPlugin;
import com.flexpoint.springboot.config.FlexPointAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 「配置即装配」自动配置集成测试：通过 flexpoint.plugins.* 开关，
 * 官方插件（classpath 存在 + enabled=true）被自动注册为 Bean 并装配进 FlexPoint。
 *
 * @author xiangganluo
 */
public class PluginsAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FlexPointAutoConfiguration.class));

    @Test
    void plugins_are_assembled_by_configuration() {
        runner.withPropertyValues(
                "flexpoint.enabled=true",
                "flexpoint.plugins.tag.enabled=true",
                "flexpoint.plugins.retry.enabled=true",
                "flexpoint.plugins.retry.max-attempts=5"
        ).run(context -> {
            assertThat(context).hasSingleBean(FlexPoint.class);
            // 两个插件按配置装配为 Bean
            Map<String, Plugin> plugins = context.getBeansOfType(Plugin.class);
            assertThat(plugins.values()).extracting(Plugin::getId)
                    .contains("selector.tag", "resilience.retry");

            // 且已装配进 FlexPoint（状态 STARTED）、选择器已注册
            FlexPoint fp = context.getBean(FlexPoint.class);
            Map<String, PluginState> states = fp.getPluginStates();
            assertThat(states).containsEntry("selector.tag", PluginState.STARTED);
            assertThat(states).containsEntry("resilience.retry", PluginState.STARTED);
            assertThat(fp.hasSelector("tagSelector")).isTrue();
        });
    }

    @Test
    void plugins_disabled_by_default() {
        runner.withPropertyValues("flexpoint.enabled=true").run(context -> {
            assertThat(context).hasSingleBean(FlexPoint.class);
            // 未开启任何 flexpoint.plugins.*，不应装配官方插件 Bean
            assertThat(context.getBeansOfType(Plugin.class)).isEmpty();
        });
    }

    @Test
    void observability_plugin_is_assembled_when_enabled() {
        runner.withPropertyValues(
                "flexpoint.enabled=true",
                "flexpoint.plugins.observability.enabled=true"
        ).run(context -> {
            assertThat(context).hasSingleBean(ObservabilityPlugin.class);

            FlexPoint fp = context.getBean(FlexPoint.class);
            assertThat(fp.getPluginStates())
                    .containsEntry(ObservabilityPlugin.PLUGIN_ID, PluginState.STARTED);
        });
    }

    @Test
    void code_selector_plugin_is_assembled_with_resolver_bean() {
        runner.withUserConfiguration(CodeResolverConfig.class)
                .withPropertyValues(
                        "flexpoint.enabled=true",
                        "flexpoint.plugins.code.enabled=true"
                ).run(context -> {
                    assertThat(context).hasSingleBean(CodeSelectorPlugin.class);
                    // 仅提供普通 CodeResolver + 只开 code：不应触发 code-version
                    assertThat(context.getBeansOfType(CodeVersionSelectorPlugin.class)).isEmpty();

                    FlexPoint fp = context.getBean(FlexPoint.class);
                    assertThat(fp.getPluginStates())
                            .containsEntry(CodeSelectorPlugin.PLUGIN_ID, PluginState.STARTED);
                    assertThat(fp.hasSelector("codeSelector")).isTrue();
                });
    }

    @Test
    void code_version_selector_plugin_is_assembled_with_resolver_bean() {
        runner.withUserConfiguration(CodeVersionResolverConfig.class)
                .withPropertyValues(
                        "flexpoint.enabled=true",
                        "flexpoint.plugins.code-version.enabled=true"
                ).run(context -> {
                    assertThat(context).hasSingleBean(CodeVersionSelectorPlugin.class);

                    FlexPoint fp = context.getBean(FlexPoint.class);
                    assertThat(fp.getPluginStates())
                            .containsEntry(CodeVersionSelectorPlugin.PLUGIN_ID, PluginState.STARTED);
                    assertThat(fp.hasSelector("codeVersionSelector")).isTrue();
                });
    }

    // =============== 用户侧协作 Bean ===============

    @Configuration
    static class CodeResolverConfig {
        @Bean
        CodeSelector.CodeResolver codeResolver() {
            return () -> "a";
        }
    }

    @Configuration
    static class CodeVersionResolverConfig {
        @Bean
        CodeVersionSelector.CodeVersionResolver codeVersionResolver() {
            // resolveVersion() 有默认实现，lambda 仅需实现 resolveCode()
            return () -> "a";
        }
    }
}
