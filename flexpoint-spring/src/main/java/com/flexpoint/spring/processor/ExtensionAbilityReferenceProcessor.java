package com.flexpoint.spring.processor;

import com.flexpoint.common.ExtensionAbility;
import com.flexpoint.common.annotations.ExtensionAbilityReference;
import com.flexpoint.core.ExtensionAbilityFactory;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.spring.proxy.ExtensionAbilityInvocationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 扩展点引用处理器
 * 处理@ExtensionAbilityReference注解的字段注入
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ExtensionAbilityReferenceProcessor implements BeanPostProcessor {

    private final ExtensionAbilityFactory extensionAbilityFactory;
    private final ExtensionMonitor extensionMonitor;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            ExtensionAbilityReference reference = field.getAnnotation(ExtensionAbilityReference.class);
            if (reference != null) {
                Class<?> fieldType = field.getType();
                if (ExtensionAbility.class.isAssignableFrom(fieldType) && fieldType.isInterface()) {
                    Object proxy = Proxy.newProxyInstance(
                            fieldType.getClassLoader(),
                            new Class[]{fieldType},
                            new ExtensionAbilityInvocationHandler(reference, extensionAbilityFactory, extensionMonitor, fieldType)
                    );
                    field.setAccessible(true);
                    try {
                        field.set(bean, proxy);
                    } catch (IllegalAccessException e) {
                        throw new BeansException("Failed to inject ExtensionAbility proxy", e) {};
                    }
                }
            }
        }
        return bean;
    }
} 