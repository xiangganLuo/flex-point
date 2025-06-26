package com.flexpoint.example.spring;

import com.flexpoint.common.annotations.ExtensionInfo;
import org.springframework.stereotype.Component;

/**
 * 商城订单状态校验实现
 */
@Component
@ExtensionInfo(id = "mall-validator", description = "商城订单状态校验器")
public class MallOrderStatusValidator implements DemoAbility {
    @Override
    public String getCode() {
        return "mall";
    }
    @Override
    public boolean isValidStatus(String fromStatus, String toStatus) {
        // 示例：只允许从NEW到PAID
        return "NEW".equals(fromStatus) && "PAID".equals(toStatus);
    }
} 