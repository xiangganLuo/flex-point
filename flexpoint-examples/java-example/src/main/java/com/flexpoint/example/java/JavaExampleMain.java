package com.flexpoint.example.java;

import com.flexpoint.core.ExtensionAbilityFactory;
import com.flexpoint.core.registry.DefaultExtensionRegistry;
import com.flexpoint.core.cache.DefaultExtensionCacheManager;
import com.flexpoint.core.monitor.DefaultExtensionMonitor;

public class JavaExampleMain {
    public static void main(String[] args) {
        DefaultExtensionRegistry registry = new DefaultExtensionRegistry();
        DefaultExtensionCacheManager cacheManager = new DefaultExtensionCacheManager();
        DefaultExtensionMonitor monitor = new DefaultExtensionMonitor();
        ExtensionAbilityFactory factory = new ExtensionAbilityFactory(registry, cacheManager, monitor);

        // 注册扩展点实现
        DemoAbility mallDiscount = new MallDiscountAbility();
        registry.register(DemoAbility.class, mallDiscount, null);

        // 查找并调用
        DemoAbility found = factory.findAbility(DemoAbility.class);
        double price = 100.0;
        System.out.println("原价: " + price + ", 折扣价: " + found.discount(price));
    }
} 