package com.flexpoint.example.java.ability.impl;

import com.flexpoint.core.extension.ExtensionTags;
import com.flexpoint.example.java.ability.OrderProcessAbility;

/**
 * 商城订单处理实现 - V1版本
 * 演示商城业务场景的订单处理逻辑
 */
public class MallOrderProcessAbility implements OrderProcessAbility {
    @Override
    public String getCode() {
        return "mall-app";
    }

    @Override
    public String processOrder(String orderId, String orderData) {
        System.out.println("商城订单处理V1: orderId=" + orderId + ", data=" + orderData);
        return "商城订单处理完成(V1): " + orderId;
    }

    @Override
    public String getOrderStatus(String orderId) {
        System.out.println("商城订单状态查询V1: orderId=" + orderId);
        return "商城订单状态(V1): 已支付";
    }

    @Override
    public ExtensionTags getTags() {
        return ExtensionTags.builder()
            .set("version", "1.0.0")
            .build();
    }

} 