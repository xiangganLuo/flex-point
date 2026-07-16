package com.flexpoint.test.springboot;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.PluginState;
import com.flexpoint.springboot.config.FlexPointAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

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
}
