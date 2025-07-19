package com.flexpoint.example.java.ability.impl;

import com.flexpoint.core.extension.ExtensionTags;
import com.flexpoint.example.java.ability.OrderProcessAbility;

/**
 * 物流订单处理实现
 * 演示物流业务场景的订单处理逻辑
 * @author xiangganluo
 */
public class LogisticsOrderProcessAbility implements OrderProcessAbility {
    @Override
    public String getCode() {
        return "logistics-app";
    }

    @Override
    public String processOrder(String orderId, String orderData) {
        System.out.println("物流订单处理: orderId=" + orderId + ", data=" + orderData);
        return "物流订单处理完成: " + orderId;
    }

    @Override
    public String getOrderStatus(String orderId) {
        System.out.println("物流订单状态查询: orderId=" + orderId);
        return "物流订单状态: 运输中";
    }

    @Override
    public ExtensionTags getTags() {
        return ExtensionTags.builder()
            .set("version", "1.0.0")
            .build();
    }
} 