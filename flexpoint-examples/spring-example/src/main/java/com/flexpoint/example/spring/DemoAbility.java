package com.flexpoint.example.spring;


import com.flexpoint.common.ExtensionAbility;

/**
 * 订单状态校验扩展点
 */
public interface DemoAbility extends ExtensionAbility {
    /**
     * 校验订单状态变更是否合法
     * @param fromStatus 原状态
     * @param toStatus 目标状态
     * @return 是否允许变更
     */
    boolean isValidStatus(String fromStatus, String toStatus);
} 