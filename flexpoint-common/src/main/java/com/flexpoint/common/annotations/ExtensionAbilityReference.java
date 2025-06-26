package com.flexpoint.common.annotations;

import java.lang.annotation.*;

/**
 * 扩展点引用注解
 * 用于在字段上标记扩展点引用，支持自动注入和代理调用
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExtensionAbilityReference {
    
    /**
     * 业务标识
     * 用于指定特定的扩展点实现
     */
    String code() default "";
    
    /**
     * 版本号
     * 用于指定扩展点版本
     */
    String version() default "";

    /**
     * 是否必需
     * 如果为true，找不到实现类时会抛出异常
     */
    boolean required() default false;
} 