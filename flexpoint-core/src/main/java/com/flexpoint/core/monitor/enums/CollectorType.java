package com.flexpoint.core.monitor.enums;

/**
 * 采集器类型枚举
 * 用于标识不同的监控数据采集场景
 *
 * @author xiangganluo
 */
public enum CollectorType {
    /** 实时采集 */
    REALTIME,
    /** 定时批量采集 */
    SCHEDULED,
    /** 自定义 */
    CUSTOM
} 