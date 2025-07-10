package com.flexpoint.common.exception;

/**
 * 选择器未找到异常
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class SelectorChinaNotFoundException extends RuntimeException {
    
    public SelectorChinaNotFoundException(String message) {
        super(message);
    }
    
    public SelectorChinaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 