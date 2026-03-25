package com.flexpoint.core.plugin.exception;

/**
 * 依赖解析/缺失/环异常。
 *
 * <p>在依赖构建、缺失检测或环路检测失败时抛出。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public class PluginDependencyException extends PluginException {
    public PluginDependencyException(String message) { super(message); }
}
