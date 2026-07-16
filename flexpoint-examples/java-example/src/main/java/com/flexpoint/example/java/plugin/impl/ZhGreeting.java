package com.flexpoint.example.java.plugin.impl;

import com.flexpoint.example.java.plugin.GreetingAbility;

/**
 * 中文问候实现（code=zh）。
 *
 * @author xiangganluo
 */
public class ZhGreeting implements GreetingAbility {
    @Override public String getCode() { return "zh"; }
    @Override public String greet() { return "你好，来自插件！"; }
}
