package com.flexpoint.spring.proxy;

import com.flexpoint.common.exception.ExtensionAbilityNotFoundException;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.context.Context;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.utils.ExtensionUtil;
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
public class ExtensionAbilityInvocationHandler implements InvocationHandler {

    private final FlexPoint flexPoint;
    private final Class<?> targetClass;

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取上下文
        Context context = flexPoint.getContextManager().getContext(method, args);
        
        // 根据扩展点类型和@FpSelector注解查找实例
        ExtensionAbility ability = flexPoint.findAbility((Class<ExtensionAbility>) targetClass, context);
        if (ability == null) {
            throw ExtensionAbilityNotFoundException.forType(targetClass.getSimpleName());
        }
        
        // 记录调用指标
        long startTime = System.currentTimeMillis();
        String extensionId = ExtensionUtil.getExtensionId(ability.getCode());
        Object ret;
        try {
            ret = method.invoke(ability, args);
            long duration = System.currentTimeMillis() - startTime;
            flexPoint.recordInvocation(extensionId, duration, true);
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - startTime;
            flexPoint.recordInvocation(extensionId, duration, false);
            throw throwable;
        }
        return ret;
    }

}