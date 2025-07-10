package com.flexpoint.example.java.ability;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.extension.ExtensionAbility;

/**
 * 订单处理扩展点接口
 * 演示多业务场景的扩展点定义
 */
@FpSelector
public interface OrderProcessAbility extends ExtensionAbility {
    /**
     * 处理订单
     */
    String processOrder(String orderId, String orderData);

    /**
     * 查询订单状态
     */
    String getOrderStatus(String orderId);
} 