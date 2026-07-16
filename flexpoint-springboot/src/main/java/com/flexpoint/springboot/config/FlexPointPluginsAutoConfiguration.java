package com.flexpoint.springboot.config;

import com.flexpoint.plugin.observe.audit.AuditLogPlugin;
import com.flexpoint.plugin.observe.metrics.MetricsSummaryPlugin;
import com.flexpoint.plugin.observe.slowcall.SlowCallPlugin;
import com.flexpoint.plugin.resilience.ResiliencePlugin;
import com.flexpoint.plugin.resilience.retry.RetryPlugin;
import com.flexpoint.plugin.selector.ab.AbTestSelectorPlugin;
import com.flexpoint.plugin.selector.gray.GraySelectorPlugin;
import com.flexpoint.plugin.selector.tag.TagSelectorPlugin;
import com.flexpoint.plugin.selector.tenant.TenantSelectorPlugin;
import com.flexpoint.plugin.selector.weight.WeightSelectorPlugin;
import com.flexpoint.springboot.properties.FlexPointPluginsProperties;
import com.flexpoint.springboot.properties.FlexPointProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 官方插件「配置即装配」自动配置。
 *
 * <p>各官方插件以 optional 依赖引入：消费方按需引入插件模块，并通过
 * {@code flexpoint.plugins.<name>.enabled=true} 开启。为兼容“插件类不在 classpath”的情况，
 * 每个插件的 @Bean 放入独立的嵌套 {@link Configuration}，并在<b>类级</b>加 {@link ConditionalOnClass}
 * —— Spring 以 ASM 评估该条件、缺失插件类时整体跳过该嵌套配置（不会 introspect 其 @Bean 返回类型）。</p>
 *
 * <p>装配出的 {@code Plugin} Bean 由 {@code FlexPointCoreAutoConfiguration} 统一收集并装配进 {@code FlexPoint}。
 * 缓存选择器需 delegate，不适合纯属性装配，请编程式使用。</p>
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
}
