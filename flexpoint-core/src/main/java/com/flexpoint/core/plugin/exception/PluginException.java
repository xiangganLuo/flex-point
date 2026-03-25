package com.flexpoint.core.plugin.exception;

/**
 * 基础插件异常。
 *
 * <p>作为插件相关异常的统一父类，便于上层捕获与处理。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public class PluginException extends RuntimeException {
    public PluginException(String message) { super(message); }
    public PluginException(String message, Throwable cause) { super(message, cause); }
}
