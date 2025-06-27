package com.flexpoint.example.java.manager;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.config.FlexPointConfigValidator;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.example.java.ability.OrderProcessAbility;
import com.flexpoint.example.java.ability.impl.*;
import com.flexpoint.example.java.resolution.CustomExtensionResolutionStrategy;
import com.flexpoint.example.java.service.OrderService;

import java.util.List;
import java.util.Map;

/**
 * FlexPoint管理器
 * 提供门面模式，简化FlexPoint的使用
 * @author luoxianggan
 */
public class FlexPointManager {
    
    private static FlexPointManager instance;
    private final FlexPoint flexPoint;
    private final OrderService orderService;
    
    private FlexPointManager() {
        // 创建FlexPoint实例
        this.flexPoint = FlexPointBuilder.create()
                .withConfig(createConfig())
                .build();
        
        // 注册自定义解析策略
        registerCustomResolvers();
        
        // 注册扩展点实现
        registerExtensions();
        
        // 创建订单服务
        this.orderService = new OrderService(flexPoint);
    }
    
    /**
     * 获取单例实例
     */
    public static FlexPointManager getInstance() {
        if (instance == null) {
            instance = new FlexPointManager();
        }
        return instance;
    }

    /**
     * 获取FlexPoint实例
     */
    public FlexPoint getFlexPoint() {
        return flexPoint;
    }

    /**
     * 获取订单服务
     */
    public OrderService getOrderService() {
        return orderService;
    }

    /**
     * 创建配置
     */
    private FlexPointConfig createConfig() {
        FlexPointConfig config = new FlexPointConfig();
        config.setEnabled(true);
        
        // 监控配置
        config.getMonitor().setEnabled(true);
        config.getMonitor().setLogInvocation(true);
        config.getMonitor().setLogResolution(true);
        config.getMonitor().setLogExceptionDetails(true);
        config.getMonitor().setPerformanceStatsEnabled(true);
        
        // 注册配置
        config.getRegistry().setEnabled(true);
        config.getRegistry().setAllowDuplicateRegistration(false);
        
        // 验证配置
        System.out.println("=== 配置验证 ===");
        boolean isValid = FlexPointConfigValidator.quickValidate(config);
        System.out.println("配置验证结果: " + (isValid ? "通过" : "失败"));
        
        if (FlexPointConfigValidator.isMonitorEnabled(config)) {
            System.out.println("监控功能已启用");
        }
        
        if (FlexPointConfigValidator.isRegistryEnabled(config)) {
            System.out.println("注册功能已启用");
        }
        
        return config;
    }
    
    /**
     * 注册自定义解析策略
     */
    private void registerCustomResolvers() {
        CustomExtensionResolutionStrategy customResolver = new CustomExtensionResolutionStrategy();
        flexPoint.registerResolver(customResolver);
        System.out.println("已注册自定义解析策略: " + customResolver.getStrategyName());
    }
    
    /**
     * 注册扩展点实现
     */
    private void registerExtensions() {
        // 注册订单处理扩展点
        registerOrderProcessExtensions();
        
        System.out.println("扩展点注册完成");
    }
    
    /**
     * 注册订单处理扩展点
     */
    private void registerOrderProcessExtensions() {
        // 商城订单处理
        OrderProcessAbility mallOrderProcessor = new MallOrderProcessAbility();
        flexPoint.register(OrderProcessAbility.class, mallOrderProcessor);
        
        // 物流订单处理
        OrderProcessAbility logisticsOrderProcessor = new LogisticsOrderProcessAbility();
        flexPoint.register(OrderProcessAbility.class, logisticsOrderProcessor);
        
        System.out.println("订单处理扩展点注册完成");
    }
    
    /**
     * 获取扩展点统计信息
     */
    public void printExtensionStatistics() {
        System.out.println("=== 扩展点统计信息 ===");
        
        try {
            // 获取所有已注册的扩展点类型
            List<Class<? extends ExtensionAbility>> registeredTypes = flexPoint.getRegistry().getRegisteredTypes();
            System.out.println("已注册的扩展点类型数量: " + registeredTypes.size());
            
            for (Class<? extends ExtensionAbility> type : registeredTypes) {
                System.out.println("扩展点类型: " + type.getSimpleName());
                List<? extends ExtensionAbility> extensions = flexPoint.getExtensions(type);
                System.out.println("  实现数量: " + extensions.size());
                
                for (ExtensionAbility extension : extensions) {
                    System.out.println("    实现: " + extension.getClass().getSimpleName() + 
                                     " (code: " + extension.getCode() + ")");
                }
            }
            
            // 获取监控指标
            Map<String, ExtensionMonitor.ExtensionMetrics> allMetrics =
                    flexPoint.getAllExtensionMetrics();
            if (!allMetrics.isEmpty()) {
                System.out.println("监控指标:");
                allMetrics.forEach((extensionId, metrics) -> {
                    System.out.println("  " + extensionId + ": 总调用=" + metrics.getTotalInvocations() + 
                                     ", 成功=" + metrics.getSuccessInvocations() + 
                                     ", 失败=" + metrics.getFailureInvocations());
                });
            }
        } catch (Exception e) {
            System.out.println("获取统计信息时发生错误: " + e.getMessage());
        }
        
        System.out.println("=====================");
    }
} 