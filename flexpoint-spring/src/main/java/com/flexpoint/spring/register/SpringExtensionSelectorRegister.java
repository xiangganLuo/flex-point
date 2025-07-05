package com.flexpoint.spring.register;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.selector.ExtensionSelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Spring扩展点选择器自动注册器
 * 自动扫描并注册带有@Component注解的ExtensionSelector实现
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringExtensionSelectorRegister implements BeanPostProcessor {

    private final FlexPoint flexPoint;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ExtensionSelector) {
            ExtensionSelector extensionSelector = (ExtensionSelector) bean;
            flexPoint.registerSelector(extensionSelector);
            log.info("自动注册扩展点选择器: {}", extensionSelector.getName());
        }
        return bean;
    }
} 