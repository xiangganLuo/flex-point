package com.flexpoint.example.java.ability.impl;

import com.flexpoint.example.java.ability.OrderProcessAbility;

/**
 * 商城订单处理实现 - V2版本
 * 演示商城业务场景的订单处理逻辑（增强版）
 * 
 * @author xiangganluo
 * @version 2.0.0
 */
public class MallOrderProcessAbilityV2 implements OrderProcessAbility {
    
    @Override
    public String getCode() {
        return "mall";
    }
    
    @Override
    public String processOrder(String orderId, double amount) {
        System.out.println("商城订单处理V2: orderId=" + orderId + ", amount=" + amount);
        
        // V2版本的订单处理逻辑（增强版）
        if (amount > 2000) {
            return "商城超大额订单处理完成(V2)，已发送VIP+通知，并启动风控检查";
        } else if (amount > 1000) {
            return "商城大额订单处理完成(V2)，已发送VIP通知，并记录用户偏好";
        } else if (amount > 500) {
            return "商城中额订单处理完成(V2)，已记录用户行为";
        } else {
            return "商城普通订单处理完成(V2)，已优化处理流程";
        }
    }

    @Override
    public String version() {
        return "2.0.0";
    }
} 