package com.flexpoint.core.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件加载报告。
 *
 * <p>汇总装配顺序、状态与错误信息，便于诊断。</p>
 *
 * <p>用例示意：
 * <pre>
 *   report.addOrdered("a");
 *   report.setState("a", PluginState.STARTED);
 *   report.addError("b", "start failed: ...");
 * </pre>
 * </p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public final class PluginLoadReport {
    private final List<String> orderedPluginIds = new ArrayList<>();
    private final Map<String, PluginState> states = new LinkedHashMap<>();
    private final Map<String, String> errors = new LinkedHashMap<>();

    /** 记录装配顺序中的一个 pluginId */
    public void addOrdered(String pluginId) { orderedPluginIds.add(pluginId); }
    /** 设置某插件当前状态 */
    public void setState(String pluginId, PluginState state) { states.put(pluginId, state); }
    /** 记录某插件的错误信息 */
    public void addError(String pluginId, String error) { errors.put(pluginId, error); }

    public List<String> getOrderedPluginIds() { return Collections.unmodifiableList(new ArrayList<>(orderedPluginIds)); }
    public Map<String, PluginState> getStates() { return Collections.unmodifiableMap(new LinkedHashMap<>(states)); }
    public Map<String, String> getErrors() { return Collections.unmodifiableMap(new LinkedHashMap<>(errors)); }

    /**
     * 返回一份独立的不可变快照副本，切断与内部可变状态的联系。
     * <p>供 {@code getLoadReport()} 在持锁时返回，避免调用方读到后续被并发修改的活对象。</p>
     */
    public PluginLoadReport snapshot() {
        PluginLoadReport copy = new PluginLoadReport();
        copy.orderedPluginIds.addAll(this.orderedPluginIds);
        copy.states.putAll(this.states);
        copy.errors.putAll(this.errors);
        return copy;
    }
}
