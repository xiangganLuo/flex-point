package com.flexpoint.core.ext.interceptor;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 默认拦截器注册表：并发安全，按 order 升序返回快照。
 *
 * <p>维护一份按 order 升序的不可变快照，在 register/unregister 时重建，
 * {@link #getInterceptors()} 直接 O(1) 返回，避免每次组链都重新排序。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public class DefaultInterceptorRegistry implements InterceptorRegistry {

    private final List<ExtInvocationInterceptor> interceptors = new CopyOnWriteArrayList<>();

    /** 按 order 升序的不可变快照，register/unregister 时重建 */
    private volatile List<ExtInvocationInterceptor> sortedSnapshot = Collections.emptyList();

    @Override
    public void register(ExtInvocationInterceptor interceptor) {
        if (interceptor == null) {
            return;
        }
        interceptors.add(interceptor);
        rebuildSnapshot();
        log.debug("注册调用拦截器: name={}, order={}, 当前拦截器数={}",
                interceptor.name(), interceptor.order(), interceptors.size());
    }

    @Override
    public void unregister(ExtInvocationInterceptor interceptor) {
        if (interceptor != null && interceptors.remove(interceptor)) {
            rebuildSnapshot();
            log.debug("注销调用拦截器: name={}, 当前拦截器数={}", interceptor.name(), interceptors.size());
        }
    }

    @Override
    public List<ExtInvocationInterceptor> getInterceptors() {
        return sortedSnapshot;
    }

    /** 重建按 order 升序的不可变快照 */
    private void rebuildSnapshot() {
        List<ExtInvocationInterceptor> sorted = new ArrayList<>(interceptors);
        sorted.sort(Comparator.comparingInt(ExtInvocationInterceptor::order));
        this.sortedSnapshot = Collections.unmodifiableList(sorted);
    }
}
