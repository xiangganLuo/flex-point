package com.flexpoint.example.java;

import com.flexpoint.example.java.manager.FlexPointManager;
import com.flexpoint.example.java.service.OrderService;

/**
 * Java原生环境下的Flex Point使用示例
 * 演示如何在非Spring环境下使用Flex Point框架，简化版本管理
 * @author luoxianggan
 */
public class JavaExampleMain {

    public static void main(String[] args) {
        System.out.println("=== Flex Point Java原生使用示例（简化版） ===\n");

        // 获取FlexPoint管理器实例
        FlexPointManager manager = FlexPointManager.getInstance();

        // 获取订单服务
        OrderService orderService = manager.getOrderService();

        // 示例: 订单处理
        demonstrateOrderProcessing(orderService);

        System.out.println("\n=== 示例运行完成 ===");
    }

    /**
     * 演示订单处理功能
     */
    private static void demonstrateOrderProcessing(OrderService orderService) {
        System.out.println("--- 订单处理示例 ---");

        // 商城订单处理
        String mallResult = orderService.processOrder("MALL001", 1500.0, "mall");
        System.out.println("商城订单处理结果: " + mallResult);

        // 物流订单处理
        String logisticsResult = orderService.processOrder("LOG001", 800.0, "logistics");
        System.out.println("物流订单处理结果: " + logisticsResult);

        // 未知业务类型
        String unknownResult = orderService.processOrder("UNK001", 500.0, "unknown");
        System.out.println("未知业务类型结果: " + unknownResult);

        // 简化版本调用
        String simpleResult = orderService.processOrder("SIMPLE001", 300.0);
        System.out.println("简化版本处理结果: " + simpleResult);

        System.out.println();
    }
}