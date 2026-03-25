package com.flexpoint.core.plugin;

/**
 * 插件生命周期接口。
 *
 * <p>统一约束插件在框架中的生命周期阶段与行为，
 * 以保证装配、启停、回收的一致性与可治理性。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public interface PluginLifecycle {

    /**
     * 初始化阶段。
     *
     * @param context 插件可访问的受控上下文
     * @throws Exception 当初始化失败时抛出异常
     */
    void init(PluginContext context) throws Exception;

    /**
     * 启动阶段。
     *
     * @throws Exception 当启动失败时抛出异常
     */
    void start() throws Exception;

    /**
     * 停止阶段（与启动对称）。
     *
     * @throws Exception 当停止流程出现问题时抛出异常
     */
    void stop() throws Exception;

    /**
     * 销毁阶段，用于释放资源（与初始化对称）。
     *
     * @throws Exception 当销毁流程出现问题时抛出异常
     */
    void destroy() throws Exception;
}
