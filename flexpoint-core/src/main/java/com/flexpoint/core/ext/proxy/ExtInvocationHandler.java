package com.flexpoint.core.ext.proxy;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.interceptor.DefaultExtInvocation;
import com.flexpoint.core.ext.interceptor.ExtInvocationInterceptor;
import com.flexpoint.core.ext.interceptor.ExtInvocationTerminal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 扩展点调用管线的代理处理器（通用驱动）。
 *
 * <p>职责单一：驱动「around 拦截链 → 终端(真正的反射调用)」。事件埋点已抽为核心内置拦截器
 * {@link EventPublishingInterceptor}（最内层），因此本处理器不含任何具体行为，只负责编排。</p>
 *
 * <p>{@link Object} 的方法（toString/hashCode/equals）直接转发，不进入拦截链。</p>
 *
 * @author xiangganluo
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class ExtInvocationHandler implements InvocationHandler {

    /** 原始扩展点实例 */
    private final ExtAbility ability;
    /** 有序拦截器（外→内），末位通常为事件埋点拦截器；由 FlexPoint 组装 */
    private final List<ExtInvocationInterceptor> interceptors;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 扩展点实现类可能为非 public 类，需放开可访问性，否则反射调用 public 接口方法会抛 IllegalAccessException
        method.setAccessible(true);

        // Object 自带方法直接转发，不埋点/不拦截
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(ability, args);
        }

        // 终端：真正的反射调用（业务异常包装为 InvocationTargetException，由事件拦截器解包）
        ExtInvocationTerminal terminal = () -> method.invoke(ability, args);

        if (interceptors == null || interceptors.isEmpty()) {
            return terminal.invoke();
        }
        if (log.isDebugEnabled()) {
            log.debug("扩展点调用进入拦截链: extId={}, method={}, 拦截器数={}",
                    ability.getExtId(), method.getName(), interceptors.size());
        }
        return new DefaultExtInvocation(ability, method, args, interceptors, terminal).proceed();
    }
}
