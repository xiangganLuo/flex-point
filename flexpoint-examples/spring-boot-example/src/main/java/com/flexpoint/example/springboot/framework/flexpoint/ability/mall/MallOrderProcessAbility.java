package com.flexpoint.example.springboot.framework.flexpoint.ability.mall;

import com.flexpoint.example.springboot.framework.flexpoint.ability.OrderProcessAbility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 商城应用订单处理实现
 */
@Slf4j
@Component
public class MallOrderProcessAbility implements OrderProcessAbility {

    @Override
    public String getCode() {
        return "mall-app";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String processOrder(String orderId, String orderData) {
        log.info("商城应用处理订单: orderId={}, orderData={}", orderId, orderData);
        return "商城订单处理完成: " + orderId;
    }
    
    @Override
    public String getOrderStatus(String orderId) {
        log.info("商城应用查询订单状态: orderId={}", orderId);
        return "商城订单状态: 已支付";
    }
}