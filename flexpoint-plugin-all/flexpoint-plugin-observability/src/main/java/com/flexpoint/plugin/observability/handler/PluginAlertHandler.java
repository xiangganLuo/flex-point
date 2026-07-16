package com.flexpoint.plugin.observability.handler;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.ExtMetrics;
import com.flexpoint.core.monitor.handler.MonitorHandler;
import com.flexpoint.plugin.observability.alert.AlertStrategy;
import com.flexpoint.plugin.observability.enums.AlertType;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * 插件域：告警处理器。
 * - 通过注入的 AlertStrategy 列表分发告警；
 * - 兼容 shouldAlert 判定。
 *
 * @author xiangganluo
 */
@Slf4j
public class PluginAlertHandler implements MonitorHandler {
    private static final String MSG_INVOCATION_FAILED = "扩展点调用失败";
    private static final String MSG_EXCEPTION_OCCURRED = "扩展点调用异常";

    private final List<AlertStrategy> alertStrategies;

    public PluginAlertHandler(List<AlertStrategy> alertStrategies) {
        this.alertStrategies = alertStrategies == null ? Collections.emptyList() : alertStrategies;
    }

    public PluginAlertHandler() {
        this(Collections.emptyList());
    }

    @Override
    public void handleInvocation(ExtAbility extAbility, long duration, boolean success, ExtMetrics metrics) {
        if (success) return;
        doAlert(extAbility.getExtId(), MSG_INVOCATION_FAILED);
    }

    @Override
    public void handleException(ExtAbility extAbility, Throwable exception, ExtMetrics metrics) {
        String message = exception != null ? exception.getMessage() : MSG_EXCEPTION_OCCURRED;
        doAlert(extAbility.getExtId(), message);
    }

    private void doAlert(String extId, String message) {
        if (alertStrategies.isEmpty()) {
            log.debug("无告警策略，跳过告警: extId={}, message={}", extId, message);
            return;
        }
        log.debug("分发告警: extId={}, message={}, 策略数={}", extId, message, alertStrategies.size());
        for (AlertStrategy alertStrategy : alertStrategies) {
            try {
                if (alertStrategy.shouldAlert(extId, AlertType.EXCEPTION)) {
                    alertStrategy.alert(extId, AlertType.EXCEPTION, message);
                }
            } catch (Exception e) {
                log.warn("告警策略异常", e);
            }
        }
    }
}
