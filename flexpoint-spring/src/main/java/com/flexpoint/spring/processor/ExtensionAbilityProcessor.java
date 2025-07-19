package com.flexpoint.spring.processor;

import com.flexpoint.common.annotations.FpExt;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.spring.proxy.ExtensionAbilityInvocationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Proxy;

import java.lang.reflect.Field;

/**
 * 扩展点引用处理器
 * 处理@FpExt注解的字段注入
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@RequiredArgsConstructor
public class ExtensionAbilityProcessor implements BeanPostProcessor {

    private final FlexPoint flexPoint;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            FpExt reference = field.getAnnotation(FpExt.class);
            if (reference != null) {
                Class<?> abilityClass = field.getType();
                if (ExtensionAbility.class.isAssignableFrom(abilityClass) && abilityClass.isInterface()) {
                    Object proxy = Proxy.newProxyInstance(
                            abilityClass.getClassLoader(),
                            new Class[]{abilityClass},
                            new ExtensionAbilityInvocationHandler(flexPoint, abilityClass)
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