package com.flexpoint.example.java;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;

public class JavaExampleMain {
    public static void main(String[] args) {
        // 使用建造者模式创建FlexPoint实例
        FlexPoint flexPoint = FlexPointBuilder.create().build();

        // 注册解析器
        CustomExtensionResolutionStrategy customExtensionResolutionStrategy = new CustomExtensionResolutionStrategy();
        flexPoint.registerResolver(customExtensionResolutionStrategy);

        // 注册扩展点实现
        DemoAbility mallDiscount = new MallDiscountAbility();
        flexPoint.register(DemoAbility.class, mallDiscount);

        // 查找并调用
        DemoAbility found = flexPoint.findAbility(DemoAbility.class);
        double price = 100.0;
        System.out.println("原价: " + price + ", 折扣价: " + found.discount(price));
        
        // 获取缓存统计
        var stats = flexPoint.getCacheStatistics();
        System.out.println("缓存命中率: " + stats.getHitRate());
    }
} 