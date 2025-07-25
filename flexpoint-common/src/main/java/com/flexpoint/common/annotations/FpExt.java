package com.flexpoint.common.annotations;

import java.lang.annotation.*;

/**
 * 扩展点引用注解
 * @author xiangganluo
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FpExt {

}