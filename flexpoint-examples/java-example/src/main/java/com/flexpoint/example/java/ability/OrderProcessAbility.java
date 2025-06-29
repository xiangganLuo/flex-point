package com.flexpoint.example.java.ability;

import com.flexpoint.core.registry.ExtensionAbility;

/**
 * 订单处理扩展点接口
 * 演示多业务场景的扩展点定义
 */
public interface OrderProcessAbility extends ExtensionAbility {
    
    /**
     * 处理订单
     * @param orderId 订单ID
     * @param amount 订单金额
     * @return 处理结果
     */
    String processOrder(String orderId, double amount);
} 