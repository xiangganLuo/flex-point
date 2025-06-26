package com.flexpoint.common.exception;

/**
 * 解析器未找到异常
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class ExtensionResolverNotFoundException extends RuntimeException {
    
    public ExtensionResolverNotFoundException(String message) {
        super(message);
    }
    
    public ExtensionResolverNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 