package com.flexpoint.core.plugin;

/**
 * 插件能力类型。
 *
 * <p>用于声明插件贡献的能力维度，便于冲突检测与治理。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public enum PluginCapability {
    /** 选择器能力 */
    SELECTOR,
    /** 事件能力（路由、订阅等） */
    EVENT,
    /** 监控能力（handler/collector 等） */
    MONITOR,
    /** 其他能力（保留） */
    OTHER
}
