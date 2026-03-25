package com.flexpoint.core.plugin.exception;

/**
 * 能力/ID 冲突异常。
 *
 * <p>当发现相同能力被多个主插件占用或 pluginId 重复时抛出。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public class PluginConflictException extends PluginException {
    public PluginConflictException(String message) { super(message); }
}
