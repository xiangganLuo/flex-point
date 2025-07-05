package com.flexpoint.example.java.service;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.example.java.ability.OrderProcessAbility;

/**
 * 订单服务类
 * 演示如何在业务服务中使用扩展点，简化版本管理
 */
public class OrderService {
    
    private final FlexPoint flexPoint;
    
    public OrderService(FlexPoint flexPoint) {
        this.flexPoint = flexPoint;
    }
    
    /**
     * 处理订单
     * 使用默认扩展点处理订单
     * 
     * @param orderId 订单ID
     * @param amount 订单金额
     * @param businessType 业务类型（用于日志记录）
     * @return 处理结果
     */
    public String processOrder(String orderId, double amount, String businessType) {
        System.out.println("开始处理订单: orderId=" + orderId + ", amount=" + amount + ", businessType=" + businessType);
        
        // 查找订单处理扩展点
        OrderProcessAbility orderProcessor = flexPoint.findAbility(OrderProcessAbility.class);
        if (orderProcessor == null) {
            return "未找到对应的订单处理器";
        }
        
        // 处理订单
        String result = orderProcessor.processOrder(orderId, amount);
        System.out.println("订单处理结果: " + result);
        
        return result;
    }
    
    /**
     * 处理订单（简化版本）
     * 
     * @param orderId 订单ID
     * @param amount 订单金额
     * @return 处理结果
     */
    public String processOrder(String orderId, double amount) {
        return processOrder(orderId, amount, "default");
    }
} 