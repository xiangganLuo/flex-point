package com.flexpoint.common.exception;

/**
 * 扩展点未找到异常
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class ExtensionAbilityNotFoundException extends RuntimeException {
    
    public ExtensionAbilityNotFoundException(String message) {
        super(message);
    }
    
    public ExtensionAbilityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 