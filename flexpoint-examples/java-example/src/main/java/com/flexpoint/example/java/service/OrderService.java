package com.flexpoint.example.java.service;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.extension.ExtensionAbilityFactory;
import com.flexpoint.example.java.ability.OrderProcessAbility;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单服务类
 * 演示如何在业务服务中使用扩展点
 */
public class OrderService {
    
    private final ExtensionAbilityFactory abilityFactory;
    
    public OrderService(FlexPoint flexPoint) {
        this.abilityFactory = flexPoint.getAbilityFactory();
    }
    
    /**
     * 处理订单
     * @param orderId 订单ID
     * @param amount 订单金额
     * @param businessType 业务类型
     * @return 处理结果
     */
    public String processOrder(String orderId, double amount, String businessType) {
        System.out.println("开始处理订单: orderId=" + orderId + ", amount=" + amount + ", businessType=" + businessType);
        
        // 构造上下文
        Map<String, Object> context = new HashMap<>();
        context.put("code", businessType);
        
        // 查找订单处理扩展点
        OrderProcessAbility orderProcessor = abilityFactory.findAbility(OrderProcessAbility.class, context);
        if (orderProcessor == null) {
            return "未找到对应的订单处理器";
        }
        
        // 处理订单
        String result = orderProcessor.processOrder(orderId, amount);
        System.out.println("订单处理结果: " + result);
        
        return result;
    }
} 