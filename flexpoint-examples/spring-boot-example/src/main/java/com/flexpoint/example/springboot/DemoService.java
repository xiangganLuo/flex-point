package com.flexpoint.example.springboot;

import com.flexpoint.common.annotations.ExtensionAbilityReference;
import org.springframework.stereotype.Service;

@Service
public class DemoService {
    @ExtensionAbilityReference(code = "admin")
    private DemoAbility demoAbility;

    public void checkPermission() {
        boolean has = demoAbility.hasPermission(1L, "DELETE_USER");
        System.out.println("管理员权限校验结果: " + has);
    }
} 