package com.flexpoint.example.java.ability.impl;

import com.flexpoint.example.java.ability.OrderProcessAbility;

/**
 * 商城订单处理实现
 * 演示商城业务场景的订单处理逻辑
 */
public class MallOrderProcessAbility implements OrderProcessAbility {
    
    @Override
    public String getCode() {
        return "mall";
    }
    
    @Override
    public String processOrder(String orderId, double amount) {
        System.out.println("商城订单处理: orderId=" + orderId + ", amount=" + amount);
        
        // 商城订单处理逻辑
        if (amount > 1000) {
            return "商城大额订单处理完成，已发送VIP通知";
        } else {
            return "商城普通订单处理完成";
        }
    }
} 