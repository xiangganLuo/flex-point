package com.flexpoint.common.annotations;

import java.lang.annotation.*;

/**
 * 使用解析器注解
 * 用于指定扩展点解析策略
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExtensionResolver {
    
    /**
     * 解析器名称
     * 如果为空，使用默认解析器
     */
    String value() default "";
} 