package com.flexpoint.spring.processor;

import com.flexpoint.common.annotations.FpExt;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.spring.proxy.ExtAbilityInvocationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class ExtAbilityProcessor implements BeanPostProcessor {

    private final FlexPoint flexPoint;

    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            FpExt reference = field.getAnnotation(FpExt.class);
            if (reference != null) {
                Class<?> abilityClass = field.getType();
                if (ExtAbility.class.isAssignableFrom(abilityClass) && abilityClass.isInterface()) {
                    log.debug("为 Bean[{}] 字段[{}] 注入扩展点代理: type={}",
                            beanName, field.getName(), abilityClass.getSimpleName());
                    Object proxy = Proxy.newProxyInstance(
                            abilityClass.getClassLoader(),
                            new Class[]{abilityClass},
                            new ExtAbilityInvocationHandler(flexPoint, (Class<ExtAbility>) abilityClass)
                    );
                    field.setAccessible(true);
                    try {
                        field.set(bean, proxy);
                    } catch (IllegalAccessException e) {
                        throw new BeansException("Failed to inject ExtAbility proxy", e) {};
                    }
                } else {
                    log.warn("@FpExt 字段[{}]类型[{}]不是 ExtAbility 接口，已跳过注入", field.getName(), abilityClass.getName());
                }
            }
        }
        return bean;
    }
} 