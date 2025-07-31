package com.flexpoint.core.monitor.handler;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.ExtMetrics;
import com.flexpoint.core.monitor.alert.AlertStrategy;
import com.flexpoint.core.monitor.enums.AlertType;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * 告警分发节点
 * 负责调用所有 AlertStrategy 进行告警
 * @author luoxianggan
 */
@Slf4j
public class AlertHandler implements MonitorHandler {

    private static final String MSG_INVOCATION_FAILED = "扩展点调用失败";
    private static final String MSG_EXCEPTION_OCCURRED = "扩展点调用异常";

    private final List<AlertStrategy> alertStrategies;

    public AlertHandler(List<AlertStrategy> alertStrategies) {
        this.alertStrategies = alertStrategies == null ? Collections.emptyList() : alertStrategies;
    }

    @Override
    public void handleInvocation(ExtAbility extAbility, long duration, boolean success, ExtMetrics metrics) {
        if (success) {
            return;
        }
        doAlert(extAbility.getExtId(), MSG_INVOCATION_FAILED);
    }

    @Override
    public void handleException(ExtAbility extAbility, Throwable exception, ExtMetrics metrics) {
        String message = exception != null ? exception.getMessage() : MSG_EXCEPTION_OCCURRED;
        doAlert(extAbility.getExtId(), message);
    }

    private void doAlert(String extId, String message) {
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
