package com.flexpoint.common.exception;

/**
 * 选择器未找到异常
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class ExtensionSelectorNotFoundException extends RuntimeException {
    
    public ExtensionSelectorNotFoundException(String message) {
        super(message);
    }
    
    public ExtensionSelectorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 