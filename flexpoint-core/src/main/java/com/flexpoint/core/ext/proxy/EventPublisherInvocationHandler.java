package com.flexpoint.core.ext.proxy;

import com.flexpoint.core.event.EventDispatcher;
import com.flexpoint.core.ext.ExtAbility;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 扩展点调用事件埋点
 * @author luoxianggan
 */
@Getter
@RequiredArgsConstructor
public class EventPublisherInvocationHandler implements InvocationHandler {

    /**
     * 原始扩展点实例
     */
    private final ExtAbility ability;
    private final EventDispatcher eventDispatcher;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object ret;
        try {
            eventDispatcher.publishInvokeBefore(ability, method.getName(), args);
            ret = method.invoke(ability, args);
            long duration = System.currentTimeMillis() - startTime;
            eventDispatcher.publishInvokeSuccess(ability, method.getName(), args, ret, duration);
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - startTime;
            eventDispatcher.publishInvokeException(ability, method.getName(), args, throwable, duration);
            throw throwable;
        }
        return ret;
    }

}
