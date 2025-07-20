package com.flexpoint.spring.proxy;

import com.flexpoint.common.exception.ExtNotFoundException;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.ext.ExtAbility;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;

/**
 * 扩展点代理调用处理器
 * 基于@FpSelector注解查找并调用扩展点
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@RequiredArgsConstructor
public class ExtAbilityInvocationHandler implements InvocationHandler {

    private final FlexPoint flexPoint;
    private final Class<?> targetClass;

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 根据扩展点类型和@FpSelector注解查找实例
        ExtAbility ability = flexPoint.findAbility((Class<ExtAbility>) targetClass);
        if (ability == null) {
            throw ExtNotFoundException.forType(targetClass.getSimpleName());
        }
        
        // 记录调用指标
        long startTime = System.currentTimeMillis();
        Object ret;
        try {
            ret = method.invoke(ability, args);
            long duration = System.currentTimeMillis() - startTime;
            flexPoint.recordInvocation(ability, duration, true);
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - startTime;
            flexPoint.recordInvocation(ability, duration, false);
            throw throwable;
        }
        return ret;
    }

}