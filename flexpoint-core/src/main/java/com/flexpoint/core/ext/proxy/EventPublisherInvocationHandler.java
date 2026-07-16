package com.flexpoint.core.ext.proxy;

import com.flexpoint.core.event.EventDispatcher;
import com.flexpoint.core.ext.ExtAbility;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 扩展点调用事件埋点代理处理器。
 *
 * <p>包裹被选中的扩展点实例，在方法调用前后发布调用事件（供监控/审计等订阅方消费）：
 * <ul>
 *   <li>{@code INVOKE_BEFORE}：调用前；</li>
 *   <li>{@code INVOKE_SUCCESS}：正常返回；</li>
 *   <li>{@code INVOKE_FAIL}：目标业务异常（解包 {@link InvocationTargetException} 后透出原始异常）；</li>
 *   <li>{@code INVOKE_EXCEPTION}：框架/反射层异常（如非法访问等）。</li>
 * </ul>
 *
 * <p>{@link Object} 的方法（toString/hashCode/equals）直接转发到目标实例，不做埋点，
 * 避免产生噪声事件。</p>
 *
 * @author luoxianggan
 */
@Getter
@RequiredArgsConstructor
public class EventPublisherInvocationHandler implements InvocationHandler {

    /** 原始扩展点实例 */
    private final ExtAbility ability;
    private final EventDispatcher eventDispatcher;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 扩展点实现类可能是非 public 类（如包级/内部类），需放开可访问性，
        // 否则对 public 接口方法反射调用会抛 IllegalAccessException。
        method.setAccessible(true);

        // Object 自带方法（toString/hashCode/equals 等）直接转发，不埋点
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(ability, args);
        }

        String methodName = method.getName();
        long startTime = System.currentTimeMillis();
        eventDispatcher.publishInvokeBefore(ability, methodName, args);
        try {
            Object result = method.invoke(ability, args);
            eventDispatcher.publishInvokeSuccess(ability, methodName, args, result, elapsed(startTime));
            return result;
        } catch (InvocationTargetException invocationException) {
            // 目标方法内部业务异常：发布 INVOKE_FAIL 并透出原始异常
            Throwable target = invocationException.getTargetException();
            eventDispatcher.publishInvokeFail(ability, methodName, args, invocationException, elapsed(startTime));
            throw target != null ? target : invocationException;
        } catch (Throwable throwable) {
            // 框架/反射层异常：发布 INVOKE_EXCEPTION
            eventDispatcher.publishInvokeException(ability, methodName, args, throwable, elapsed(startTime));
            throw throwable;
        }
    }

    private static long elapsed(long startTime) {
        return System.currentTimeMillis() - startTime;
    }
}
