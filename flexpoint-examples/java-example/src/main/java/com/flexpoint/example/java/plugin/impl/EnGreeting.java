package com.flexpoint.example.java.plugin.impl;

import com.flexpoint.example.java.plugin.GreetingAbility;

/**
 * 英文问候实现（code=en）。
 *
 * @author xiangganluo
 */
public class EnGreeting implements GreetingAbility {
    @Override public String getCode() { return "en"; }
    @Override public String greet() { return "Hello from plugin!"; }
}
