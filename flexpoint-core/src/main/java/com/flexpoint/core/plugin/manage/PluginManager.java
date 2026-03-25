package com.flexpoint.core.plugin.manage;

import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.PluginLoadReport;
import com.flexpoint.core.plugin.PluginState;

import java.util.Map;

/**
 * 插件管理器接口。
 *
 * <p>负责插件注册索引、依赖解析、生命周期编排与运行期治理。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public interface PluginManager {
    /** 注册单个插件 */
    void register(Plugin plugin);
    /** 批量注册插件 */
    void registerAll(Iterable<Plugin> plugins);
    /** 解析依赖与装配顺序 */
    void resolve();
    /** 按顺序安装（init→start） */
    void installAll();
    /** 逆序停止并销毁（stop→destroy） */
    void stopAll();
    /** 运行期启用（预留） */
    void enable(String pluginId);
    /** 运行期停用（预留） */
    void disable(String pluginId);
    /** 获取加载报告 */
    PluginLoadReport getLoadReport();
    /** 获取当前插件状态快照 */
    Map<String, PluginState> getPluginStates();
    /** 根据 pluginId 获取已注册插件（若未注册返回 null） */
    com.flexpoint.core.plugin.Plugin getPlugin(String pluginId);
}
