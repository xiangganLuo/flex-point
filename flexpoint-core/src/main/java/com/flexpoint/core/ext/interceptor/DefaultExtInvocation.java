package com.flexpoint.core.ext.interceptor;

import com.flexpoint.core.ext.ExtAbility;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 可重入的调用链实现。
 *
 * <p>每次 {@link #proceed()} 都会构造“下一节点”并交给当前拦截器，
 * 因此同一拦截器多次调用 {@code proceed()} 会重新执行其后的链与终端（支持重试）。
 * 不共享可变游标，天然可重入。</p>
 *
 * @author xiangganluo
 */
public final class DefaultExtInvocation implements ExtInvocation {

    private final ExtAbility target;
    private final Method method;
    private final Object[] args;
    private final List<ExtInvocationInterceptor> interceptors;
    private final int index;
    private final ExtInvocationTerminal terminal;

    public DefaultExtInvocation(ExtAbility target,
                                Method method,
                                Object[] args,
                                List<ExtInvocationInterceptor> interceptors,
                                ExtInvocationTerminal terminal) {
        this(target, method, args, interceptors, 0, terminal);
    }

    private DefaultExtInvocation(ExtAbility target,
                                 Method method,
                                 Object[] args,
                                 List<ExtInvocationInterceptor> interceptors,
                                 int index,
                                 ExtInvocationTerminal terminal) {
        this.target = target;
        this.method = method;
        this.args = args;
        this.interceptors = interceptors;
        this.index = index;
        this.terminal = terminal;
    }

    @Override
    public ExtAbility getTarget() {
        return target;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public Object proceed() throws Throwable {
        if (index < interceptors.size()) {
            ExtInvocationInterceptor current = interceptors.get(index);
            ExtInvocation next = new DefaultExtInvocation(target, method, args, interceptors, index + 1, terminal);
            return current.intercept(next);
        }
        return terminal.invoke();
    }
}
