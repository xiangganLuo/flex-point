package com.flexpoint.common.annotations;

import java.lang.annotation.*;

/**
 * 扩展点选择器注解
 * 指定扩展点使用的选择器名称
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FpSelector {
    
    /**
     * 选择器名称
     * 对应Selector.getName()返回的值
     */
    String value();

}