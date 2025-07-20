package com.flexpoint.example.springboot.ext;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.ext.ExtAbility;

/**
 * 订单处理扩展点接口
 * @author xiangganluo
 */
@FpSelector("codeVersionSelector")
public interface OrderProcessAbility extends ExtAbility {
    
    /**
     * 处理订单
     */
    String processOrder(String orderId, String orderData);
    
    /**
     * 获取订单状态
     */
    String getOrderStatus(String orderId);
} 