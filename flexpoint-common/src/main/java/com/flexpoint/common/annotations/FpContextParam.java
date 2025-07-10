package com.flexpoint.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 上下文参数注解
 * 用于标记方法参数中的上下文信息，支持参数名映射
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface FpContextParam {

    /**
     * 上下文属性名
     * 如果不指定，则使用参数名作为属性名
     */
    String value() default "";

    /**
     * 是否必需
     * 如果为true，当参数为null时会记录警告
     */
    boolean required() default false;

} 