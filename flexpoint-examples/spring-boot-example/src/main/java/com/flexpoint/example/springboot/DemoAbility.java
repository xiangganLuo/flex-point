package com.flexpoint.example.springboot;

import com.flexpoint.common.ExtensionAbility;

/**
 * 用户权限校验扩展点
 */
public interface DemoAbility extends ExtensionAbility {
    /**
     * 校验用户是否有权限
     * @param userId 用户ID
     * @param action 操作
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String action);
} 