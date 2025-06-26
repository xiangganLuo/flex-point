package com.flexpoint.example.spring;

import com.flexpoint.common.annotations.ExtensionAbilityReference;
import org.springframework.stereotype.Service;

@Service
public class DemoService {
    @ExtensionAbilityReference(code = "mall")
    private DemoAbility demoAbility;

    public void checkOrderStatus() {
        boolean valid = demoAbility.isValidStatus("NEW", "PAID");
        System.out.println("订单状态变更校验结果: " + valid);
    }
} 