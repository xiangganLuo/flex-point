package com.flexpoint.common.exception;

/**
 * Flex Point配置异常基类
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class FlexPointConfigException extends RuntimeException {
    
    public FlexPointConfigException(String message) {
        super(message);
    }
    
    public FlexPointConfigException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FlexPointConfigException(Throwable cause) {
        super(cause);
    }
} 