package com.flexpoint.example.java.ability.impl;

import com.flexpoint.example.java.ability.OrderProcessAbility;

/**
 * 物流订单处理实现
 * 演示物流业务场景的订单处理逻辑
 */
public class LogisticsOrderProcessAbility implements OrderProcessAbility {
    
    @Override
    public String getCode() {
        return "logistics";
    }
    
    @Override
    public String processOrder(String orderId, double amount) {
        System.out.println("物流订单处理: orderId=" + orderId + ", amount=" + amount);
        
        // 物流订单处理逻辑
        if (amount > 500) {
            return "物流大额订单处理完成，已安排专车配送";
        } else {
            return "物流普通订单处理完成，已安排标准配送";
        }
    }

    @Override
    public String version() {
        return "1.0.0";
    }
} 