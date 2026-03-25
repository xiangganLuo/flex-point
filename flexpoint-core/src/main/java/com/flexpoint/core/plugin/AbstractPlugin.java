package com.flexpoint.core.plugin;

/**
 * 可选的便捷基类，提供空实现。
 *
 * <p>业务插件可继承本类按需覆写生命周期方法，
 * 无需全部实现。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public abstract class AbstractPlugin implements Plugin {
    @Override public void init(PluginContext context) throws Exception {}
    @Override public void start() throws Exception {}
    @Override public void stop() throws Exception {}
    @Override public void destroy() throws Exception {}
}
