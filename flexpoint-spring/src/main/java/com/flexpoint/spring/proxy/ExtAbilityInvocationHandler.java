package com.flexpoint.spring.proxy;

import com.flexpoint.common.exception.ExtNotFoundException;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.ext.ExtAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;

/**
 * 扩展点代理调用处理器
 * 基于@FpSelector注解查找并调用扩展点
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class ExtAbilityInvocationHandler implements InvocationHandler {

    private final FlexPoint flexPoint;

    private final Class<ExtAbility> targetClass;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.debug("@FpExt 代理调用: type={}, method={}", targetClass.getSimpleName(), method.getName());
        // 根据扩展点类型和@FpSelector注解查找实例
        ExtAbility ability = flexPoint.findAbility(targetClass);
        if (ability == null) {
            log.debug("@FpExt 代理未找到扩展点实例: type={}", targetClass.getSimpleName());
            throw ExtNotFoundException.forType(targetClass.getSimpleName());
        }
        return method.invoke(ability, args);
    }

}