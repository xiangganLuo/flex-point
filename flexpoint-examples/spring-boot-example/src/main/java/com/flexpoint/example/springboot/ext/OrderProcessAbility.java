package com.flexpoint.example.springboot.ext;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.extension.ExtensionAbility;

/**
 * 订单处理扩展点接口
 */
@FpSelector("codeVersionSelector")
public interface OrderProcessAbility extends ExtensionAbility {
    
    /**
     * 处理订单
     */
    String processOrder(String orderId, String orderData);
    
    /**
     * 获取订单状态
     */
    String getOrderStatus(String orderId);
} 