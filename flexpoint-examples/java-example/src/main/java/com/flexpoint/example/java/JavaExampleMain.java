package com.flexpoint.example.java;

import com.flexpoint.example.java.context.AppContext;
import com.flexpoint.example.java.manager.FlexPointManager;
import com.flexpoint.example.java.service.OrderService;

/**
 * Java原生环境下的Flex Point使用示例
 * 演示如何通过上下文自动选择扩展点实现
 * @author xiangganluo
 */
public class JavaExampleMain {
    public static void main(String[] args) {
        System.out.println("=== Flex Point Java原生自动选择器示例 ===\n");
        FlexPointManager manager = FlexPointManager.getInstance();
        OrderService orderService = manager.getOrderService();
        // 商城订单处理
        AppContext.setAppCode("mall-app");
        String mallResult = orderService.processOrder("MALL001", "商城订单数据");
        String mallStatus = orderService.getOrderStatus("MALL001");
        System.out.println("商城订单处理结果: " + mallResult);
        System.out.println("商城订单状态: " + mallStatus);
        AppContext.clear();

        // 物流订单处理
        AppContext.setAppCode("logistics-app");
        String logisticsResult = orderService.processOrder("LOG001", "物流订单数据");
        String logisticsStatus = orderService.getOrderStatus("LOG001");
        System.out.println("物流订单处理结果: " + logisticsResult);
        System.out.println("物流订单状态: " + logisticsStatus);
        AppContext.clear();

        // 未知业务类型
        AppContext.setAppCode("unknown-app");
        String unknownResult = orderService.processOrder("UNK001", "未知订单数据");
        System.out.println("未知业务类型结果: " + unknownResult);
        AppContext.clear();

        System.out.println("\n=== 示例运行完成 ===");
    }
}