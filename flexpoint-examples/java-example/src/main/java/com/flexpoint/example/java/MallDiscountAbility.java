package com.flexpoint.example.java;

/**
 * 商城折扣实现
 */
public class MallDiscountAbility implements DemoAbility {
    @Override
    public String getCode() {
        return "mall";
    }
    @Override
    public double discount(double price) {
        // 示例：商城打9折
        return price * 0.9;
    }
} 