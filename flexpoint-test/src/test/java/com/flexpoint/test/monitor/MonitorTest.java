package com.flexpoint.test.monitor;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.monitor.DefaultExtensionMonitor;
import com.flexpoint.core.monitor.ExtensionMonitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MonitorTest {
    private ExtensionMonitor monitor;
    private String extId = "demo:0.0.0";

    @BeforeEach
    public void setup() {
        FlexPointConfig.MonitorConfig config = new FlexPointConfig.MonitorConfig();
        config.setEnabled(true);
        monitor = new DefaultExtensionMonitor(config);
    }

    @Test
    public void testInvocationAndDuration() {
        monitor.recordInvocation(extId, 100, true);
        monitor.recordInvocation(extId, 200, true);
        ExtensionMonitor.ExtensionMetrics metrics = monitor.getMetrics(extId);
        Assertions.assertEquals(2, metrics.getTotalInvocations());
        Assertions.assertEquals(150, metrics.getAverageResponseTime());
    }

    @Test
    public void testExceptionCount() {
        monitor.recordException(extId, new RuntimeException("test"));
        ExtensionMonitor.ExtensionMetrics metrics = monitor.getMetrics(extId);
        Assertions.assertEquals(1, metrics.getExceptionCount());
    }

    @Test
    public void testResetMetrics() {
        monitor.recordInvocation(extId, 100, true);
        monitor.resetMetrics(extId);
        ExtensionMonitor.ExtensionMetrics metrics = monitor.getMetrics(extId);
        Assertions.assertEquals(0, metrics.getTotalInvocations());
    }
} 