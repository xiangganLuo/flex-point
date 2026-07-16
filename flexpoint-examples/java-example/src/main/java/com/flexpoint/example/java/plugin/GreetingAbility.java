package com.flexpoint.example.java.plugin;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.ext.ExtAbility;

/**
 * 自定义插件示例：问候扩展点。
 * <p>由自定义插件 {@code GreetingSelectorPlugin} 注册的选择器进行路由。</p>
 *
 * @author xiangganluo
 */
@FpSelector("greetingSelector")
public interface GreetingAbility extends ExtAbility {
    /** 返回一句问候语 */
    String greet();
}
