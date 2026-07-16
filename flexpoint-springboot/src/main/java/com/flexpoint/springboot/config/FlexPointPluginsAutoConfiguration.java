package com.flexpoint.springboot.config;

import com.flexpoint.plugin.observability.ObservabilityPlugin;
import com.flexpoint.plugin.observability.alert.AlertStrategy;
import com.flexpoint.plugin.observability.metrics.MetricsCollector;
import com.flexpoint.plugin.observe.audit.AuditLogPlugin;
import com.flexpoint.plugin.observe.metrics.MetricsSummaryPlugin;
import com.flexpoint.plugin.observe.slowcall.SlowCallPlugin;
import com.flexpoint.plugin.resilience.ResiliencePlugin;
import com.flexpoint.plugin.resilience.retry.RetryPlugin;
import com.flexpoint.plugin.selector.ab.AbTestSelectorPlugin;
import com.flexpoint.plugin.selector.code.CodeSelector;
import com.flexpoint.plugin.selector.code.CodeSelectorPlugin;
import com.flexpoint.plugin.selector.codeversion.CodeVersionSelector;
import com.flexpoint.plugin.selector.codeversion.CodeVersionSelectorPlugin;
import com.flexpoint.plugin.selector.gray.GraySelectorPlugin;
import com.flexpoint.plugin.selector.tag.TagSelectorPlugin;
import com.flexpoint.plugin.selector.tenant.TenantSelectorPlugin;
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
 * 部分插件需容器提供额外协作 Bean 才装配：code/code-version 需业务方的
 * {@code CodeResolver}/{@code CodeVersionResolver}。</p>
 *
 * <p>缓存选择器 {@code CachingSelectorPlugin} 未纳入「配置即装配」：它是装饰器，需要一个 delegate
 * {@code Selector}，而本框架的 {@code FlexPointSpringSelectorRegister} 会把容器内所有 {@code Selector}
 * Bean 直接注册进 {@code SelectorRegistry}——delegate 已按其名注册，缓存装饰器默认沿用同名，纯属性装配必然
 * 导致「选择器名称重复」冲突；且内置选择器均由插件内部创建、并非 {@code Selector} Bean，无法作为 delegate 注入。
 * 因此缓存选择器请以显式 {@code @Bean}（自行控制 delegate 与名称）方式使用，会被上述注册器自动纳入。</p>
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
    @ConditionalOnClass(TagSelectorPlugin.class)
    static class TagCfg {
        @Bean
        @ConditionalOnMissingBean(TagSelectorPlugin.class)
        @ConditionalOnProperty(prefix = P + ".tag", name = "enabled", havingValue = "true")
        public TagSelectorPlugin tagSelectorPlugin() {
            return new TagSelectorPlugin();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(GraySelectorPlugin.class)
    static class GrayCfg {
        @Bean
        @ConditionalOnMissingBean(GraySelectorPlugin.class)
        @ConditionalOnProperty(prefix = P + ".gray", name = "enabled", havingValue = "true")
        public GraySelectorPlugin graySelectorPlugin(FlexPointPluginsProperties props) {
            return new GraySelectorPlugin(props.getGray().getPercentage());
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(AbTestSelectorPlugin.class)
    static class AbCfg {
        @Bean
        @ConditionalOnMissingBean(AbTestSelectorPlugin.class)
        @ConditionalOnProperty(prefix = P + ".ab", name = "enabled", havingValue = "true")
        public AbTestSelectorPlugin abTestSelectorPlugin(FlexPointPluginsProperties props) {
            return new AbTestSelectorPlugin(props.getAb().getBuckets());
        }
    }

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

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(TenantSelectorPlugin.class)
    static class TenantCfg {
        @Bean
        @ConditionalOnMissingBean(TenantSelectorPlugin.class)
        @ConditionalOnProperty(prefix = P + ".tenant", name = "enabled", havingValue = "true")
        public TenantSelectorPlugin tenantSelectorPlugin(FlexPointPluginsProperties props) {
            return new TenantSelectorPlugin(props.getTenant().isFallback());
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
