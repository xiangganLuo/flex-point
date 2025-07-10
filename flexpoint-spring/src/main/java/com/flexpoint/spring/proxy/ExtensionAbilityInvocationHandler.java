package com.flexpoint.spring.proxy;

import com.flexpoint.common.annotations.FpExt;
import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.common.exception.ExtensionAbilityNotFoundException;
import com.flexpoint.common.exception.SelectorChinaNotFoundException;
import com.flexpoint.common.utils.ExtensionUtil;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.context.Context;
import com.flexpoint.core.extension.ExtensionAbility;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;

/**
 * 扩展点代理调用处理器
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@RequiredArgsConstructor
public class ExtensionAbilityInvocationHandler implements InvocationHandler {

    private final FpExt reference;
    private final FlexPoint flexPoint;
    private final Class<?> extensionAbilityClass;

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String chainName = null;
        // 1. 优先查找字段上的 FpExt 注解
        if (reference != null && !reference.chainName().isEmpty()) {
            chainName = reference.chainName();
        } else {
            // 2. 查找接口或实现类上的 FpSelector 注解
            FpSelector selectorAnno = extensionAbilityClass.getAnnotation(FpSelector.class);
            if (selectorAnno != null && !selectorAnno.value().isEmpty()) {
                chainName = selectorAnno.value();
            }
        }
        if (chainName == null) {
            throw new SelectorChinaNotFoundException("未指定选择器链名称：请在字段上加@FpExt(chainName=...)或接口/实现类上加@FpSelector('...')");
        }
        Context context = flexPoint.getContextManager().getContext(method, args);
        ExtensionAbility ability = flexPoint.findAbility((Class<ExtensionAbility>) extensionAbilityClass, chainName, context);
        if (ability == null) {
            throw new ExtensionAbilityNotFoundException("No ExtensionAbility implementation found for: " + extensionAbilityClass.getName());
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

}