package com.flexpoint.example.springboot.framework.flexpoint.ability;

import com.flexpoint.common.annotations.ExtensionResolver;
import com.flexpoint.core.registry.ExtensionAbility;

/**
 * 订单处理扩展点接口
 */
@ExtensionResolver("ExampleResolutionStrategy")
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