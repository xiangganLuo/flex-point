package com.flexpoint.common.annotations;

import java.lang.annotation.*;

/**
 * 扩展点选择器注解
 * 用于指定扩展点选择策略
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Selector {
    
    /**
     * 选择器名称
     * 如果为空，使用默认选择器
     */
    String value() default "";
} 