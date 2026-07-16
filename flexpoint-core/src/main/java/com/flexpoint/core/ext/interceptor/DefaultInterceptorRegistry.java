package com.flexpoint.core.ext.interceptor;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 默认拦截器注册表：并发安全，按 order 升序返回快照。
 *
 * @author xiangganluo
 */
@Slf4j
public class DefaultInterceptorRegistry implements InterceptorRegistry {

    private final List<ExtInvocationInterceptor> interceptors = new CopyOnWriteArrayList<>();

    @Override
    public void register(ExtInvocationInterceptor interceptor) {
        if (interceptor == null) {
            return;
        }
        interceptors.add(interceptor);
        log.debug("注册调用拦截器: name={}, order={}, 当前拦截器数={}",
                interceptor.name(), interceptor.order(), interceptors.size());
    }

    @Override
    public void unregister(ExtInvocationInterceptor interceptor) {
        if (interceptor != null && interceptors.remove(interceptor)) {
            log.debug("注销调用拦截器: name={}, 当前拦截器数={}", interceptor.name(), interceptors.size());
        }
    }

    @Override
    public List<ExtInvocationInterceptor> getInterceptors() {
        List<ExtInvocationInterceptor> snapshot = new ArrayList<>(interceptors);
        snapshot.sort(Comparator.comparingInt(ExtInvocationInterceptor::order));
        return snapshot;
    }
}
