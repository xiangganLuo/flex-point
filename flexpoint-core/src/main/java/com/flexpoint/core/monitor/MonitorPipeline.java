package com.flexpoint.core.monitor;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.alert.AlertStrategy;
import com.flexpoint.core.monitor.enums.AlertType;
import com.flexpoint.core.monitor.enums.CollectorType;
import com.flexpoint.core.monitor.enums.EventType;
import com.flexpoint.core.monitor.event.MonitorEventListener;
import com.flexpoint.core.monitor.metrics.MetricsCollector;
import com.flexpoint.core.utils.ExtUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 监控埋点管道，统一分发采集器、告警、事件监听等扩展点
 *
 * @author xiangganluo
 */
@Slf4j
public class MonitorPipeline {
    private final List<MetricsCollector> collectors = new CopyOnWriteArrayList<>();
    private final List<AlertStrategy> alertStrategies = new CopyOnWriteArrayList<>();
    private final List<MonitorEventListener> eventListeners = new CopyOnWriteArrayList<>();

    /**
     * 调用前（可选）
     */
    public void beforeInvoke(ExtAbility extAbility) {
        String ext = ExtUtil.getExtId(extAbility);
        for (MonitorEventListener l : eventListeners) {
            try {
                l.onEvent(EventType.CUSTOM, ext);
            } catch (Exception e) {
                log.warn("事件监听器beforeInvoke异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 调用后
     */
    public void afterInvoke(ExtAbility extAbility, long duration, boolean success, ExtMonitor.ExtMetrics metrics) {
        String ext = ExtUtil.getExtId(extAbility);
        for (MetricsCollector c : collectors) {
            try {
                c.collect(ext, metrics, CollectorType.REALTIME);
            } catch (Exception e) {
                log.warn("采集器afterInvoke异常: {}", e.getMessage());
            }
        }
        for (MonitorEventListener l : eventListeners) {
            try {
                l.onEvent(success ? EventType.INVOKE_SUCCESS : EventType.INVOKE_FAIL, ext);
            } catch (Exception e) {
                log.warn("事件监听器afterInvoke异常: {}", e.getMessage());
            }
        }
        if (!success) {
            for (AlertStrategy a : alertStrategies) {
                try {
                    if (a.shouldAlert(ext, AlertType.EXCEPTION)) {
                        a.alert(ext, AlertType.EXCEPTION, "扩展点调用失败");
                    }
                } catch (Exception e) {
                    log.warn("告警策略afterInvoke异常: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 异常
     */
    public void onException(ExtAbility extAbility, Throwable exception, ExtMonitor.ExtMetrics metrics) {
        String ext = ExtUtil.getExtId(extAbility);
        for (MetricsCollector c : collectors) {
            try {
                c.collect(ext, metrics, CollectorType.REALTIME);
            } catch (Exception e) {
                log.warn("采集器onException异常: {}", e.getMessage());
            }
        }
        for (MonitorEventListener l : eventListeners) {
            try {
                l.onEvent(EventType.EXCEPTION, ext);
            } catch (Exception e) {
                log.warn("事件监听器onException异常: {}", e.getMessage());
            }
        }
        for (AlertStrategy a : alertStrategies) {
            try {
                if (a.shouldAlert(ext, AlertType.EXCEPTION)) {
                    a.alert(ext, AlertType.EXCEPTION, exception.getMessage());
                }
            } catch (Exception e) {
                log.warn("告警策略onException异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 阈值超限
     */
    public void onThreshold(ExtAbility extAbility, String message) {
        String ext = ExtUtil.getExtId(extAbility);
        for (MonitorEventListener l : eventListeners) {
            try {
                l.onEvent(EventType.THRESHOLD_EXCEEDED, ext);
            } catch (Exception e) {
                log.warn("事件监听器onThreshold异常: {}", e.getMessage());
            }
        }
        for (AlertStrategy a : alertStrategies) {
            try {
                if (a.shouldAlert(ext, AlertType.FAILURE_RATE)) {
                    a.alert(ext, AlertType.FAILURE_RATE, message);
                }
            } catch (Exception e) {
                log.warn("告警策略onThreshold异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 指标重置
     */
    public void onMetricsReset(ExtAbility extAbility) {
        String ext = ExtUtil.getExtId(extAbility);
        for (MonitorEventListener l : eventListeners) {
            try {
                l.onEvent(EventType.METRICS_RESET, ext);
            } catch (Exception e) {
                log.warn("事件监听器onMetricsReset异常: {}", e.getMessage());
            }
        }
        for (MetricsCollector c : collectors) {
            try {
                c.collect(ext, null, CollectorType.REALTIME);
            } catch (Exception e) {
                log.warn("采集器onMetricsReset异常: {}", e.getMessage());
            }
        }
    }

} 