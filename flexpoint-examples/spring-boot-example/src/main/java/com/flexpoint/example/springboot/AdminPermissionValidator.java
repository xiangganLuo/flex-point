package com.flexpoint.example.springboot;

import com.flexpoint.common.annotations.ExtensionInfo;
import org.springframework.stereotype.Component;

/**
 * 管理员权限校验实现
 */
@Component
@ExtensionInfo(id = "admin-permission", description = "管理员权限校验器")
public class AdminPermissionValidator implements DemoAbility {
    @Override
    public String getCode() {
        return "admin";
    }
    @Override
    public boolean hasPermission(Long userId, String action) {
        // 示例：管理员拥有所有权限
        return true;
    }
} 