package com.flexpoint.spring.proxy;

import com.flexpoint.common.annotations.Extension;
import com.flexpoint.common.constants.FlexPointConstants;
import com.flexpoint.common.exception.ExtensionAbilityNotFoundException;
import com.flexpoint.common.utils.ExtensionUtil;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.extension.ExtensionAbility;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 扩展点代理调用处理器
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@RequiredArgsConstructor
public class ExtensionAbilityInvocationHandler implements InvocationHandler {

    private final Extension reference;

    private final FlexPoint flexPoint;

    private final Class<?> extensionAbilityClass;

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构造上下文
        Map<String, Object> context = new HashMap<>();

        // 注解code如果存在，则优先使用注解中的值
        if (!reference.code().isEmpty()) {
            context.put(FlexPointConstants.CODE, reference.code());
        }

        ExtensionAbility ability = flexPoint.findAbility((Class<ExtensionAbility>) extensionAbilityClass);
        if (ability == null && reference.required()) {
            throw new ExtensionAbilityNotFoundException("No ExtensionAbility implementation found for: " + extensionAbilityClass.getName());
        }
        if (ability == null) {
            return getDefaultReturnValue(method.getReturnType());
        }
        
        long startTime = System.currentTimeMillis();
        String extensionId = ExtensionUtil.getExtensionId(ability.getCode(), ability.version());
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

    private Object getDefaultReturnValue(Class<?> returnType) {
        if (returnType.isPrimitive()) {
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == int.class) {
                return 0;
            }
            if (returnType == long.class) {
                return 0L;
            }
            if (returnType == double.class) {
                return 0.0d;
            }
            if (returnType == float.class) {
                return 0.0f;
            }
            if (returnType == short.class) {
                return (short) 0;
            }
            if (returnType == byte.class) {
                return (byte) 0;
            }
            if (returnType == char.class) {
                return '\u0000';
            }
        }
        return null;
    }
} 