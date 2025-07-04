package com.flexpoint.core.monitor.enums;

/**
 * 告警类型枚举
 * 用于标识不同的监控告警场景
 *
 * @author xiangganluo
 */
public enum AlertType {
    /** 响应超时 */
    TIMEOUT,
    /** 异常发生 */
    EXCEPTION,
    /** 失败率过高 */
    FAILURE_RATE,
    /** 自定义 */
    CUSTOM
} 