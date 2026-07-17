package com.flexpoint.test.monitor;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.DefaultExtMonitor;
import com.flexpoint.core.monitor.ExtMetrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 修复 1 回归：纯内核（无 MetricsProvider）下 getExtMetrics 返回非 null 的空指标（各值为 0）。
 *
 * @author xiangganluo
 */
public class EmptyMetricsTest {

    static class SampleAbility implements ExtAbility {
        @Override public String getCode() { return "sample"; }
    }

    @Test
    void pure_core_getExtMetrics_returns_non_null_zero_metrics() {
        DefaultExtMonitor monitor = new DefaultExtMonitor(new FlexPointConfig.MonitorConfig());

        ExtMetrics metrics = monitor.getExtMetrics(new SampleAbility());

        Assertions.assertNotNull(metrics, "无 MetricsProvider 时不应返回 null");
        Assertions.assertEquals(0L, metrics.getTotalInvocations());
        Assertions.assertEquals(0L, metrics.getSuccessInvocations());
        Assertions.assertEquals(0L, metrics.getFailureInvocations());
        Assertions.assertEquals(0.0, metrics.getSuccessRate());
        Assertions.assertEquals(0.0, metrics.getAverageResponseTime());
        Assertions.assertEquals(0L, metrics.getMaxResponseTime());
        Assertions.assertEquals(0L, metrics.getMinResponseTime(), "最小响应时间应为 0 而非 Long.MAX_VALUE");
        Assertions.assertEquals(0L, metrics.getExceptionCount());
        Assertions.assertEquals(0L, metrics.getLastInvocationTime());
        Assertions.assertEquals(0.0, metrics.getQPS());
    }

    @Test
    void empty_factory_returns_singleton() {
        Assertions.assertSame(ExtMetrics.empty(), ExtMetrics.empty());
    }
}
