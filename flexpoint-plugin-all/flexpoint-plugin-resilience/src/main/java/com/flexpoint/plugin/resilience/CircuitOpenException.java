package com.flexpoint.plugin.resilience;

/**
 * 熔断器打开（OPEN）状态下的快速失败异常。
 *
 * <p>当 {@link CircuitBreakerInterceptor} 处于 OPEN 状态时，调用被直接拒绝、
 * 不再推进 {@code proceed()}，转而抛出本异常。</p>
 *
 * @author xiangganluo
 */
public class CircuitOpenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CircuitOpenException(String message) {
        super(message);
    }
}
