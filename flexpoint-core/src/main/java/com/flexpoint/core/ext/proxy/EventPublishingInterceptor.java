package com.flexpoint.core.ext.proxy;

import com.flexpoint.core.event.EventDispatcher;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.interceptor.ExtInvocation;
import com.flexpoint.core.ext.interceptor.ExtInvocationInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

/**
 * 核心内置拦截器：扩展点调用事件埋点。
 *
 * <p>事件发布本身以「最新的拦截器实现」承载，保留在 core（非插件），并作为**最内层**拦截器
 * （{@link #order()} 取最大值），紧贴真正的方法调用——因此重试/超时等外层行为拦截器可观测到
 * 每一次实际调用的事件。</p>
 *
 * <p>事件语义：
 * <ul>
 *   <li>{@code INVOKE_BEFORE}：调用前；</li>
 *   <li>{@code INVOKE_SUCCESS}：正常返回；</li>
 *   <li>{@code INVOKE_FAIL}：目标业务异常（解包 {@link InvocationTargetException} 透出原始异常）；</li>
 *   <li>{@code INVOKE_EXCEPTION}：框架/反射层异常。</li>
 * </ul></p>
 *
 * @author xiangganluo
 */
@Slf4j
public class EventPublishingInterceptor implements ExtInvocationInterceptor {

    private final EventDispatcher eventDispatcher;

    public EventPublishingInterceptor(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    /** 最内层：紧贴真正调用，保证外层行为拦截器可见每次调用的事件。 */
    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String name() {
        return "core.event-publishing";
    }

    @Override
    public Object intercept(ExtInvocation invocation) throws Throwable {
        ExtAbility ability = invocation.getTarget();
        String methodName = invocation.getMethod().getName();
        Object[] args = invocation.getArgs();
        long startTime = System.currentTimeMillis();

        log.debug("扩展点调用开始: extId={}, method={}", ability.getExtId(), methodName);
        eventDispatcher.publishInvokeBefore(ability, methodName, args);
        try {
            Object result = invocation.proceed();
            long cost = System.currentTimeMillis() - startTime;
            log.debug("扩展点调用成功: extId={}, method={}, cost={}ms", ability.getExtId(), methodName, cost);
            eventDispatcher.publishInvokeSuccess(ability, methodName, args, result, cost);
            return result;
        } catch (InvocationTargetException invocationException) {
            // 目标方法内部业务异常：发布 INVOKE_FAIL 并透出原始异常
            Throwable target = invocationException.getTargetException();
            log.debug("扩展点调用业务异常(INVOKE_FAIL): extId={}, method={}, ex={}",
                    ability.getExtId(), methodName, target != null ? target.getClass().getSimpleName() : "ITE");
            eventDispatcher.publishInvokeFail(ability, methodName, args, invocationException, System.currentTimeMillis() - startTime);
            throw target != null ? target : invocationException;
        } catch (Throwable throwable) {
            // 框架/反射层异常：发布 INVOKE_EXCEPTION
            log.debug("扩展点调用框架异常(INVOKE_EXCEPTION): extId={}, method={}, ex={}",
                    ability.getExtId(), methodName, throwable.getClass().getSimpleName());
            eventDispatcher.publishInvokeException(ability, methodName, args, throwable, System.currentTimeMillis() - startTime);
            throw throwable;
        }
    }
}
