package com.flexpoint.springboot.config;

import com.flexpoint.plugin.observability.ObservabilityPlugin;
import com.flexpoint.plugin.observability.alert.AlertStrategy;
import com.flexpoint.plugin.observability.metrics.MetricsCollector;
import com.flexpoint.plugin.observe.audit.AuditLogPlugin;
import com.flexpoint.plugin.observe.metrics.MetricsSummaryPlugin;
import com.flexpoint.plugin.observe.slowcall.SlowCallPlugin;
import com.flexpoint.plugin.resilience.ResiliencePlugin;
import com.flexpoint.plugin.resilience.retry.RetryPlugin;
import com.flexpoint.plugin.selector.code.CodeSelector;
import com.flexpoint.plugin.selector.code.CodeSelectorPlugin;
import com.flexpoint.plugin.selector.codeversion.CodeVersionSelector;
import com.flexpoint.plugin.selector.codeversion.CodeVersionSelectorPlugin;
import com.flexpoint.plugin.selector.weight.WeightSelectorPlugin;
import com.flexpoint.springboot.properties.FlexPointPluginsProperties;
import com.flexpoint.springboot.properties.FlexPointProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 官方插件「配置即装配」自动配置。
 *
 * <p>各官方插件以 optional 依赖引入：消费方按需引入插件模块，并通过
 * {@code flexpoint.plugins.<name>.enabled=true} 开启。为兼容“插件类不在 classpath”的情况，
 * 每个插件的 @Bean 放入独立的嵌套 {@link Configuration}，并在<b>类级</b>加 {@link ConditionalOnClass}
 * —— Spring 以 ASM 评估该条件、缺失插件类时整体跳过该嵌套配置（不会 introspect 其 @Bean 返回类型）。</p>
 *
 * <p>装配出的 {@code Plugin} Bean 由 {@code FlexPointCoreAutoConfiguration} 统一收集并装配进 {@code FlexPoint}。
 * code/code-version 需容器提供业务方的 {@code CodeResolver}/{@code CodeVersionResolver} Bean（{@code @ConditionalOnBean}）才装配。</p>
 *
 * <p><b>依赖运行期数据的选择器（tag/gray/ab/tenant/cache）不纳入「配置即装配」</b>：它们需要业务方实现的
 * 数据接口（如 {@code TagSelector.LabelResolver}、{@code GraySelector.GrayKeyResolver}、
 * {@code TenantSelector.TenantResolver} 等）提供路由数据，无法「零编码」自动装配；请以显式 {@code @Bean}
 * 声明对应 {@code *SelectorPlugin}（传入你的 Resolver），会被自动收集装配。缓存选择器同理，且它是装饰器
 * 默认沿用 delegate 同名注册，纯属性装配会与 {@code FlexPointSpringSelectorRegister} 自动注册的 delegate 冲突。</p>
 *
 * @author xiangganluo
 */
@Configuration
@ConditionalOnProperty(prefix = FlexPointProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FlexPointPluginsProperties.class)
public class FlexPointPluginsAutoConfiguration {

    private static final String P = FlexPointPluginsProperties.PREFIX;

    // =============== 选择器插件 ===============

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(WeightSelectorPlugin.class)
    static class WeightCfg {
        @Bean
        @ConditionalOnMissingBean(WeightSelectorPlugin.class)
        @ConditionalOnProperty(prefix = P + ".weight", name = "enabled", havingValue = "true")
        public WeightSelectorPlugin weightSelectorPlugin() {
            return new WeightSelectorPlugin();
        }
    }

    // =============== 观测/治理插件 ===============

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(AuditLogPlugin.class)
    static class AuditCfg {
        @Bean
        @ConditionalOnMissingBean(AuditLogPlugin.class)
        @ConditionalOnProperty(prefix = P + ".audit", name = "enabled", havingValue = "true")
        public AuditLogPlugin auditLogPlugin(FlexPointPluginsProperties props) {
            return new AuditLogPlugin(props.getAudit().isLogSelection(), props.getAudit().isLogInvocation());
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(SlowCallPlugin.class)
    static class SlowCallCfg {
        @Bean
        @ConditionalOnMissingBean(SlowCallPlugin.class)
        @ConditionalOnProperty(prefix = P + ".slowcall", name = "enabled", havingValue = "true")
        public SlowCallPlugin slowCallPlugin(FlexPointPluginsProperties props) {
            return new SlowCallPlugin(props.getSlowcall().getThresholdMs());
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(MetricsSummaryPlugin.class)
    static class MetricsCfg {
        @Bean
        @ConditionalOnMissingBean(MetricsSummaryPlugin.class)
        @ConditionalOnProperty(prefix = P + ".metrics", name = "enabled", havingValue = "true")
        public MetricsSummaryPlugin metricsSummaryPlugin(FlexPointPluginsProperties props) {
            return new MetricsSummaryPlugin(props.getMetrics().getIntervalSeconds());
        }
    }

    // =============== 行为增强插件 ===============

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RetryPlugin.class)
    static class RetryCfg {
        @Bean
        @ConditionalOnMissingBean(RetryPlugin.class)
        @ConditionalOnProperty(prefix = P + ".retry", name = "enabled", havingValue = "true")
        public RetryPlugin retryPlugin(FlexPointPluginsProperties props) {
            return new RetryPlugin(props.getRetry().getMaxAttempts(), props.getRetry().getBackoffMs());
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ResiliencePlugin.class)
    static class ResilienceCfg {
        @Bean
        @ConditionalOnMissingBean(ResiliencePlugin.class)
        @ConditionalOnProperty(prefix = P + ".resilience", name = "enabled", havingValue = "true")
        public ResiliencePlugin resiliencePlugin(FlexPointPluginsProperties props) {
            FlexPointPluginsProperties.Resilience r = props.getResilience();
            return new ResiliencePlugin(r.getTimeoutMs(), r.getFailureRateThreshold(), r.getMinimumCalls(), r.getOpenMillis());
        }
    }

    // =============== 可观测性插件 ===============

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ObservabilityPlugin.class)
    static class ObservabilityCfg {
        /**
         * 可观测性插件：从容器收集 {@link AlertStrategy} 与 {@link MetricsCollector}（若有）。
         * 二者均无时用无参构造（仅默认指标处理链）；有则用 List 构造注入自定义告警/采集。
         */
        @Bean
        @ConditionalOnMissingBean(ObservabilityPlugin.class)
        @ConditionalOnProperty(prefix = P + ".observability", name = "enabled", havingValue = "true")
        public ObservabilityPlugin observabilityPlugin(ObjectProvider<AlertStrategy> alertStrategies,
                                                       ObjectProvider<MetricsCollector> collectors) {
            List<AlertStrategy> alerts = alertStrategies.orderedStream().collect(Collectors.toList());
            List<MetricsCollector> cols = collectors.orderedStream().collect(Collectors.toList());
            if (alerts.isEmpty() && cols.isEmpty()) {
                return new ObservabilityPlugin();
            }
            return new ObservabilityPlugin(alerts, cols);
        }
    }

    // =============== Code / Code+Version 选择器插件 ===============

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(CodeSelectorPlugin.class)
    static class CodeCfg {
        /**
         * Code 选择器：需业务方提供 {@link CodeSelector.CodeResolver} Bean 解析 code。
         *
         * <p>取舍说明：{@code CodeVersionResolver} 继承自 {@code CodeResolver}，因此仅有
         * {@code CodeVersionResolver} Bean 时同样满足此 {@code @ConditionalOnBean(CodeResolver)}。
         * 但 code 与 code-version 装配各由自身 {@code enabled} 开关独立控制，且注册的选择器名不同
         * （codeSelector / codeVersionSelector），互不冲突。</p>
         */
        @Bean
        @ConditionalOnMissingBean(CodeSelectorPlugin.class)
        @ConditionalOnBean(CodeSelector.CodeResolver.class)
        @ConditionalOnProperty(prefix = P + ".code", name = "enabled", havingValue = "true")
        public CodeSelectorPlugin codeSelectorPlugin(CodeSelector.CodeResolver resolver) {
            return new CodeSelectorPlugin(resolver);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(CodeVersionSelectorPlugin.class)
    static class CodeVersionCfg {
        /**
         * Code + Version 选择器：需业务方提供 {@link CodeVersionSelector.CodeVersionResolver} Bean。
         * 用其专有子类型作 {@code @ConditionalOnBean} 条件，避免被普通 {@code CodeResolver} 误触发。
         */
        @Bean
        @ConditionalOnMissingBean(CodeVersionSelectorPlugin.class)
        @ConditionalOnBean(CodeVersionSelector.CodeVersionResolver.class)
        @ConditionalOnProperty(prefix = P + ".code-version", name = "enabled", havingValue = "true")
        public CodeVersionSelectorPlugin codeVersionSelectorPlugin(CodeVersionSelector.CodeVersionResolver resolver) {
            return new CodeVersionSelectorPlugin(resolver);
        }
    }
}
