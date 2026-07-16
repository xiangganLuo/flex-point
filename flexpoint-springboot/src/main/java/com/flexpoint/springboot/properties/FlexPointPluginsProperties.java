package com.flexpoint.springboot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 官方插件「配置即装配」属性。
 *
 * <p>通过 {@code flexpoint.plugins.<name>.enabled=true} 开启对应官方插件，
 * 由 {@code FlexPointPluginsAutoConfiguration} 按开关与参数自动注册为 {@code Plugin} Bean。
 * 选择器类插件从标准上下文 {@code FlexPointContext} 读取路由信息，无需业务编码。</p>
 *
 * @author xiangganluo
 */
@Data
@ConfigurationProperties(prefix = FlexPointPluginsProperties.PREFIX)
public class FlexPointPluginsProperties {

    public static final String PREFIX = "flexpoint.plugins";

    /** 标签选择器 */
    private Tag tag = new Tag();
    /** 灰度选择器 */
    private Gray gray = new Gray();
    /** A/B 分流选择器 */
    private Ab ab = new Ab();
    /** 权重选择器 */
    private Weight weight = new Weight();
    /** 租户选择器 */
    private Tenant tenant = new Tenant();
    /** 审计日志 */
    private Audit audit = new Audit();
    /** 慢调用告警 */
    private SlowCall slowcall = new SlowCall();
    /** 指标汇总 */
    private Metrics metrics = new Metrics();
    /** 重试 */
    private Retry retry = new Retry();
    /** 超时+熔断 */
    private Resilience resilience = new Resilience();

    @Data
    public static class Tag {
        private boolean enabled = false;
    }

    @Data
    public static class Gray {
        private boolean enabled = false;
        /** 灰度百分比 0-100 */
        private int percentage = 0;
    }

    @Data
    public static class Ab {
        private boolean enabled = false;
        /** 分桶权重，如 {A:50, B:50} */
        private Map<String, Integer> buckets = new LinkedHashMap<>();
    }

    @Data
    public static class Weight {
        private boolean enabled = false;
    }

    @Data
    public static class Tenant {
        private boolean enabled = false;
        /** 租户无命中时是否回退到默认候选 */
        private boolean fallback = false;
    }

    @Data
    public static class Audit {
        private boolean enabled = false;
        private boolean logSelection = true;
        private boolean logInvocation = true;
    }

    @Data
    public static class SlowCall {
        private boolean enabled = false;
        /** 慢调用阈值（毫秒） */
        private long thresholdMs = 200L;
    }

    @Data
    public static class Metrics {
        private boolean enabled = false;
        /** 汇总输出周期（秒） */
        private int intervalSeconds = 60;
    }

    @Data
    public static class Retry {
        private boolean enabled = false;
        /** 最大尝试次数（含首次） */
        private int maxAttempts = 3;
        /** 重试前退避（毫秒） */
        private long backoffMs = 0L;
    }

    @Data
    public static class Resilience {
        private boolean enabled = false;
        /** 单次调用超时（毫秒），<=0 不启用超时 */
        private long timeoutMs = 0L;
        /** 熔断失败率阈值 0-1 */
        private double failureRateThreshold = 0.5D;
        /** 熔断统计最小调用数 */
        private int minimumCalls = 20;
        /** 熔断打开持续时间（毫秒） */
        private long openMillis = 5000L;
    }
}
