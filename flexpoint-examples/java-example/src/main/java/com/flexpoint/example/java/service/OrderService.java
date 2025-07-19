package com.flexpoint.example.java.service;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.example.java.ability.OrderProcessAbility;

/**
 * 订单服务类
 * 演示如何在业务服务中使用扩展点，自动感知上下文
 * @author xiangganluo
 */
public class OrderService {

    private final FlexPoint flexPoint;

    public OrderService(FlexPoint flexPoint) {
        this.flexPoint = flexPoint;
    }

    /**
     * 处理订单（自动根据上下文选择实现）
     */
    public String processOrder(String orderId, String orderData) {
        OrderProcessAbility ability = flexPoint.findAbility(OrderProcessAbility.class);
        if (ability == null) {
            return "未找到对应的订单处理器";
        }
        return ability.processOrder(orderId, orderData);
    }

    /**
     * 查询订单状态（自动根据上下文选择实现）
     */
    public String getOrderStatus(String orderId) {
        OrderProcessAbility ability = flexPoint.findAbility(OrderProcessAbility.class);
        if (ability == null) {
            return "未找到对应的订单处理器";
        }
        return ability.getOrderStatus(orderId);
    }
} 