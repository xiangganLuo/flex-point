package com.flexpoint.core.monitor;

import com.flexpoint.core.monitor.alert.AlertStrategy;
import com.flexpoint.core.monitor.enums.AlertType;
import com.flexpoint.core.monitor.enums.CollectorType;
import com.flexpoint.core.monitor.enums.EventType;
import com.flexpoint.core.monitor.event.MonitorEventListener;
import com.flexpoint.core.monitor.metrics.MetricsCollector;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    public void addMetricsCollector(MetricsCollector collector) {
        if (collector != null) collectors.add(collector);
    }
    public void removeMetricsCollector(MetricsCollector collector) {
        collectors.remove(collector);
    }
    public List<MetricsCollector> getCollectors() {
        return Collections.unmodifiableList(collectors);
    }

    public void addAlertStrategy(AlertStrategy strategy) {
        if (strategy != null) alertStrategies.add(strategy);
    }
    public void removeAlertStrategy(AlertStrategy strategy) {
        alertStrategies.remove(strategy);
    }
    public List<AlertStrategy> getAlertStrategies() {
        return Collections.unmodifiableList(alertStrategies);
    }

    public void addEventListener(MonitorEventListener listener) {
        if (listener != null) eventListeners.add(listener);
    }
    public void removeEventListener(MonitorEventListener listener) {
        eventListeners.remove(listener);
    }
    public List<MonitorEventListener> getEventListeners() {
        return Collections.unmodifiableList(eventListeners);
    }

    // --- 埋点触发方法 ---

    /**
     * 调用前（可选）
     */
    public void beforeInvoke(String extensionId, Map<String, Object> context) {
        for (MonitorEventListener l : eventListeners) {
            try {
                l.onEvent(EventType.CUSTOM, extensionId, context);
            } catch (Exception e) {
                log.warn("事件监听器beforeInvoke异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 调用后
     */
    public void afterInvoke(String extensionId, long duration, boolean success, Map<String, Object> context, ExtensionMonitor.ExtensionMetrics metrics) {
        for (MetricsCollector c : collectors) {
            try {
                c.collect(extensionId, metrics, context, CollectorType.REALTIME);
            } catch (Exception e) {
                log.warn("采集器afterInvoke异常: {}", e.getMessage());
            }
        }
        for (MonitorEventListener l : eventListeners) {
            try {
                l.onEvent(success ? EventType.INVOKE_SUCCESS : EventType.INVOKE_FAIL, extensionId, context);
            } catch (Exception e) {
                log.warn("事件监听器afterInvoke异常: {}", e.getMessage());
            }
        }
        if (!success) {
            for (AlertStrategy a : alertStrategies) {
                try {
                    if (a.shouldAlert(extensionId, AlertType.EXCEPTION, context)) {
                        a.alert(extensionId, AlertType.EXCEPTION, "扩展点调用失败", context);
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
    public void onException(String extensionId, Throwable exception, Map<String, Object> context, ExtensionMonitor.ExtensionMetrics metrics) {
        for (MetricsCollector c : collectors) {
            try {
                c.collect(extensionId, metrics, context, CollectorType.REALTIME);
            } catch (Exception e) {
                log.warn("采集器onException异常: {}", e.getMessage());
            }
        }
        for (MonitorEventListener l : eventListeners) {
            try {
                l.onEvent(EventType.EXCEPTION, extensionId, context);
            } catch (Exception e) {
                log.warn("事件监听器onException异常: {}", e.getMessage());
            }
        }
        for (AlertStrategy a : alertStrategies) {
            try {
                if (a.shouldAlert(extensionId, AlertType.EXCEPTION, context)) {
                    a.alert(extensionId, AlertType.EXCEPTION, exception.getMessage(), context);
                }
            } catch (Exception e) {
                log.warn("告警策略onException异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 阈值超限
     */
    public void onThreshold(String extensionId, String message, Map<String, Object> context) {
        for (MonitorEventListener l : eventListeners) {
            try {
                l.onEvent(EventType.THRESHOLD_EXCEEDED, extensionId, context);
            } catch (Exception e) {
                log.warn("事件监听器onThreshold异常: {}", e.getMessage());
            }
        }
        for (AlertStrategy a : alertStrategies) {
            try {
                if (a.shouldAlert(extensionId, AlertType.FAILURE_RATE, context)) {
                    a.alert(extensionId, AlertType.FAILURE_RATE, message, context);
                }
            } catch (Exception e) {
                log.warn("告警策略onThreshold异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 指标重置
     */
    public void onMetricsReset(String extensionId, Map<String, Object> context) {
        for (MonitorEventListener l : eventListeners) {
            try {
                l.onEvent(EventType.METRICS_RESET, extensionId, context);
            } catch (Exception e) {
                log.warn("事件监听器onMetricsReset异常: {}", e.getMessage());
            }
        }
        for (MetricsCollector c : collectors) {
            try {
                c.collect(extensionId, null, context, CollectorType.REALTIME);
            } catch (Exception e) {
                log.warn("采集器onMetricsReset异常: {}", e.getMessage());
            }
        }
    }
} 