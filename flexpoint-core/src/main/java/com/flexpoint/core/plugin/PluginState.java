package com.flexpoint.core.plugin;

/**
 * 插件状态。
 *
 * <p>定义插件在框架生命周期中的关键状态节点，
 * 有助于可观测与问题定位。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public enum PluginState {
    /** 刚注册但未进行任何生命周期动作 */
    CREATED,
    /** 已完成 init 阶段，资源/依赖检查就绪 */
    INITIALIZED,
    /** 已完成 start 阶段，能力已对外暴露 */
    STARTED,
    /** 已调用 stop，能力对外关闭 */
    STOPPED,
    /** 生命周期异常失败（对非关键插件为降级状态） */
    FAILED,
    /** 已完成 destroy，资源释放完成 */
    DESTROYED
}
