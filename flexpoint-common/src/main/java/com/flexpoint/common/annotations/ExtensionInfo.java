package com.flexpoint.common.annotations;

import java.lang.annotation.*;

/**
 * 扩展点信息注解
 * 用于标记扩展点实现类，提供元数据信息
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExtensionInfo {
    
    /**
     * 扩展点ID
     */
    String id() default "";
    
    /**
     * 版本号
     */
    String version() default "";
    
    /**
     * 优先级
     * 数值越小优先级越高
     */
    int priority() default 100;
    
    /**
     * 描述信息
     */
    String description() default "";
    
    /**
     * 是否启用
     */
    boolean enabled() default true;
} 