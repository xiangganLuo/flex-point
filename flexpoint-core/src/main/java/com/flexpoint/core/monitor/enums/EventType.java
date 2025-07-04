package com.flexpoint.core.monitor.enums;

/**
 * 监控事件类型枚举
 * 用于标识不同的监控事件场景
 *
 * @author xiangganluo
 */
public enum EventType {
    /** 调用成功 */
    INVOKE_SUCCESS,
    /** 调用失败 */
    INVOKE_FAIL,
    /** 异常 */
    EXCEPTION,
    /** 阈值超限 */
    THRESHOLD_EXCEEDED,
    /** 指标重置 */
    METRICS_RESET,
    /** 自定义 */
    CUSTOM
} 