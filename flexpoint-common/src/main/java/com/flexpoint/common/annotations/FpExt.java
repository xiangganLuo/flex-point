package com.flexpoint.common.annotations;

import java.lang.annotation.*;

import static com.flexpoint.common.constants.FlexPointConstants.DEFAULT_SELECTOR_CHAIN_NAME;

/**
 * 扩展点引用注解
 * @author luoxianggan
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FpExt {

    /**
     * 选择器链
     */
    String chainName() default DEFAULT_SELECTOR_CHAIN_NAME;

}