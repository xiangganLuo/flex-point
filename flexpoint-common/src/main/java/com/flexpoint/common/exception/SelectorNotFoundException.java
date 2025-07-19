package com.flexpoint.common.exception;

/**
 * 选择器未找到异常
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class SelectorNotFoundException extends RuntimeException {
    
    public SelectorNotFoundException(String message) {
        super(message);
    }
    
    public SelectorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 