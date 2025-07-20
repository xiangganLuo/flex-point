package com.flexpoint.core.monitor.alert;

import com.flexpoint.core.monitor.enums.AlertType;

/**
 * 告警策略扩展接口
 * 仅定义扩展点，无具体实现
 * @author xiangganluo
 */
public interface AlertStrategy {

    /**
     * 触发告警
     * @param ext 扩展点ID
     * @param alertType 告警类型（枚举）
     * @param message 告警信息
     */
    void alert(String ext, AlertType alertType, String message);

    /**
     * 判断是否需要告警
     * @param ext 扩展点ID
     * @param alertType 告警类型
     * @return 是否需要告警
     */
    default boolean shouldAlert(String ext, AlertType alertType) {
        return true;
    }
} 