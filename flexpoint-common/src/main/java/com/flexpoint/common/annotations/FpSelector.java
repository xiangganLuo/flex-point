package com.flexpoint.common.annotations;

import cn.hutool.core.annotation.AliasFor;

import java.lang.annotation.*;

import static com.flexpoint.common.constants.FlexPointConstants.DEFAULT_SELECTOR_CHAIN_NAME;

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
public @interface FpSelector {
    
    /**
     * 选择器名称
     * 如果为空，使用默认选择器
     */
    String value() default DEFAULT_SELECTOR_CHAIN_NAME;

    /**
     * 选择链名
     */
    @AliasFor(attribute = "value")
    String chainName() default DEFAULT_SELECTOR_CHAIN_NAME;

}