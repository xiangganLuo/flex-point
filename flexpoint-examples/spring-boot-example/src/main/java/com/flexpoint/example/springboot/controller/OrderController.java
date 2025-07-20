package com.flexpoint.example.springboot.controller;

import com.flexpoint.common.annotations.FpExt;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.example.springboot.ext.OrderProcessAbility;
import com.flexpoint.example.springboot.framework.common.CommonResult;
import com.flexpoint.example.springboot.framework.flexpoint.context.SysAppContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单控制器
 * @author xiangganluo
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    /**
     * 通过门面类进行获取扩展点实例
     */
    private final FlexPoint flexPoint;

    /**
     * 处理订单
     */
    @PostMapping("/process")
    public CommonResult<String> processOrder(@RequestBody Map<String, String> request) {
        String orderId = request.get("orderId");
        String orderData = request.get("orderData");
        
        log.info("处理订单请求: orderId={}, appCode={}", orderId, SysAppContext.getAppCode());
        
        try {
            OrderProcessAbility ability = flexPoint.findAbility(OrderProcessAbility.class);
            if (ability == null) {
                return CommonResult.error("EXT_NOT_FOUND", "未找到对应的扩展点实现");
            }
            
            String result = ability.processOrder(orderId, orderData);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("处理订单失败", e);
            return CommonResult.error("PROCESS_FAILED", "处理订单失败: " + e.getMessage());
        }
    }

    /**
     * 通过@FpExt 注解获取扩展点实例
     */
    @FpExt
    private OrderProcessAbility orderProcessAbility;
    
    /**
     * 获取订单状态
     */
    @GetMapping("/{orderId}/status")
    public CommonResult<String> getOrderStatus(@PathVariable String orderId) {
        log.info("查询订单状态: orderId={}, appCode={}", orderId, SysAppContext.getAppCode());
        
        try {
            if (orderProcessAbility == null) {
                return CommonResult.error("EXTNOT_FOUND", "未找到对应的扩展点实现");
            }
            
            String status = orderProcessAbility.getOrderStatus(orderId);
            return CommonResult.success(status);
        } catch (Exception e) {
            log.error("查询订单状态失败", e);
            return CommonResult.error("QUERY_FAILED", "查询订单状态失败: " + e.getMessage());
        }
    }

} 