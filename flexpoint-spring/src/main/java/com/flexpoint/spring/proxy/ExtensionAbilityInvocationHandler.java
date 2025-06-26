package com.flexpoint.spring.proxy;

import com.flexpoint.common.ExtensionAbility;
import com.flexpoint.common.annotations.ExtensionAbilityReference;
import com.flexpoint.common.constants.FlexPointConstants;
import com.flexpoint.common.utils.ExtensionUtil;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.common.exception.ExtensionAbilityNotFoundException;
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

    private final ExtensionAbilityReference reference;
    private final com.flexpoint.core.ExtensionAbilityFactory ExtensionAbilityFactory;
    private final ExtensionMonitor extensionMonitor;
    private final Class<?> fieldType;

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构造上下文
        Map<String, Object> context = new HashMap<>();
        if (!reference.code().isEmpty()) {
            context.put(FlexPointConstants.CODE, reference.code());
        }
        if (!reference.version().isEmpty()) {
            context.put(FlexPointConstants.VERSION, reference.version());
        }
        
        ExtensionAbility target = ExtensionAbilityFactory.findAbility((Class<ExtensionAbility>) fieldType, context);
        if (target == null && reference.required()) {
            throw new ExtensionAbilityNotFoundException("No ExtensionAbility implementation found for: " + fieldType.getName());
        }
        if (target == null) {
            return getDefaultReturnValue(method.getReturnType());
        }
        
        long startTime = System.currentTimeMillis();
        String extensionId = ExtensionUtil.getExtensionId(target.getClass(), context);
        Object ret;
        try {
            ret = method.invoke(target, args);
            long duration = System.currentTimeMillis() - startTime;
            extensionMonitor.recordInvocation(extensionId, duration, true);
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - startTime;
            extensionMonitor.recordInvocation(extensionId, duration, false);
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