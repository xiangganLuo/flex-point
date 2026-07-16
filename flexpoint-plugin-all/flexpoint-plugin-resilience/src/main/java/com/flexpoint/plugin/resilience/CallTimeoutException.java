package com.flexpoint.plugin.resilience;

/**
 * 扩展点调用超时异常。
 *
 * <p>当 {@link TimeoutInterceptor} 在配置的时限内未获得调用结果时抛出，
 * 其 {@code cause} 通常为底层 {@link java.util.concurrent.TimeoutException}。</p>
 *
 * @author xiangganluo
 */
public class CallTimeoutException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CallTimeoutException(String message) {
        super(message);
    }

    public CallTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
