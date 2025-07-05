package com.flexpoint.example.java.manager;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.config.FlexPointConfigValidator;
import com.flexpoint.example.java.ability.OrderProcessAbility;
import com.flexpoint.example.java.ability.impl.LogisticsOrderProcessAbility;
import com.flexpoint.example.java.ability.impl.MallOrderProcessAbilityV1;
import com.flexpoint.example.java.ability.impl.MallOrderProcessAbilityV2;
import com.flexpoint.example.java.selector.CustomSelector;
import com.flexpoint.example.java.service.OrderService;

/**
 * FlexPoint管理器
 * 提供门面模式，简化FlexPoint的使用，支持版本管理
 * @author luoxianggan
 */
public class FlexPointManager {
    
    private static volatile FlexPointManager instance;
    private final FlexPoint flexPoint;
    private final OrderService orderService;
    
    private FlexPointManager() {
        // 创建配置
        FlexPointConfig config = createConfig();
        
        // 验证配置
        FlexPointConfigValidator.validateAndProcess(config);
        
        // 创建FlexPoint管理器
        this.flexPoint = FlexPointBuilder.create()
                .withConfig(config)
                .build();

        this.flexPoint.registerSelector(new CustomSelector());
        
        // 注册扩展点
        registerOrderProcessExtensions();
        
        // 创建订单服务
        this.orderService = new OrderService(flexPoint);
        
        System.out.println("FlexPoint管理器初始化完成");
    }
    
    /**
     * 获取单例实例
     */
    public static FlexPointManager getInstance() {
        if (instance == null) {
            synchronized (FlexPointManager.class) {
                if (instance == null) {
                    instance = new FlexPointManager();
                }
            }
        }
        return instance;
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
        config.getMonitor().setLogSelection(true);
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
     * 注册订单处理扩展点
     */
    private void registerOrderProcessExtensions() {
        // 商城订单处理V1版本
        OrderProcessAbility mallOrderProcessorV1 = new MallOrderProcessAbilityV1();
        flexPoint.register(mallOrderProcessorV1);
        
        // 商城订单处理V2版本
        OrderProcessAbility mallOrderProcessorV2 = new MallOrderProcessAbilityV2();
        flexPoint.register(mallOrderProcessorV2);
        
        // 物流订单处理
        OrderProcessAbility logisticsOrderProcessor = new LogisticsOrderProcessAbility();
        flexPoint.register(logisticsOrderProcessor);
        
        System.out.println("订单处理扩展点注册完成");
    }
    
    /**
     * 获取订单服务
     */
    public OrderService getOrderService() {
        return orderService;
    }
}