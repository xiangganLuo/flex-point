package com.flexpoint.example.springboot.ext.logistics;

import com.flexpoint.example.springboot.ext.OrderProcessAbility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 物流应用订单处理实现
 * @author xiangganluo
 */
@Slf4j
@Component
public class LogisticsOrderProcessAbility implements OrderProcessAbility {

    @Override
    public String getCode() {
        return "logistics-app";
    }

    @Override
    public String processOrder(String orderId, String orderData) {
        log.info("物流应用处理订单: orderId={}, orderData={}", orderId, orderData);
        return "物流订单处理完成: " + orderId;
    }
    
    @Override
    public String getOrderStatus(String orderId) {
        log.info("物流应用查询订单状态: orderId={}", orderId);
        return "物流订单状态: 运输中";
    }
} 