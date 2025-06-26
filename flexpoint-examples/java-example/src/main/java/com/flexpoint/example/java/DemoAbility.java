package com.flexpoint.example.java;


import com.flexpoint.common.ExtensionAbility;

/**
 * 商品折扣计算扩展点
 */
public interface DemoAbility extends ExtensionAbility {
    /**
     * 计算商品折扣价
     * @param price 原价
     * @return 折扣价
     */
    double discount(double price);
} 