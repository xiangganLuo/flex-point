package com.flexpoint.core.monitor.alert;

import com.flexpoint.core.monitor.enums.AlertType;

import java.util.Map;

/**
 * 告警策略扩展接口
 * 仅定义扩展点，无具体实现
 * @author xiangganluo
 */
public interface AlertStrategy {

    /**
     * 触发告警
     * @param extensionId 扩展点ID
     * @param alertType 告警类型（枚举）
     * @param message 告警信息
     * @param context 业务上下文
     */
    void alert(String extensionId, AlertType alertType, String message, Map<String, Object> context);

    /**
     * 判断是否需要告警
     * @param extensionId 扩展点ID
     * @param alertType 告警类型
     * @param context 业务上下文
     * @return 是否需要告警
     */
    default boolean shouldAlert(String extensionId, AlertType alertType, Map<String, Object> context) {
        return true;
    }
} 