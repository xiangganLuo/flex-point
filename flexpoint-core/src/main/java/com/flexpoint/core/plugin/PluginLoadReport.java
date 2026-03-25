package com.flexpoint.core.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件加载报告（最小实现，便于后续扩展）。
 *
 * <p>汇总装配顺序、状态与错误信息，便于诊断。</p>
 *
 * <p>用例示意：
 * <pre>
 *   report.addOrdered("a");
 *   report.setState("a", PluginState.STARTED);
 *   report.addError("b", "Missing dependency x");
 *   report.addConflict("SELECTOR occupied by p1 & p2");
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
    private final List<String> conflicts = new ArrayList<>();
    private final List<String> missingDependencies = new ArrayList<>();

    /** 记录解析出的顺序中的一个 pluginId */
    public void addOrdered(String pluginId) { orderedPluginIds.add(pluginId); }
    /** 设置某插件当前状态 */
    public void setState(String pluginId, PluginState state) { states.put(pluginId, state); }
    /** 记录某插件的错误信息 */
    public void addError(String pluginId, String error) { errors.put(pluginId, error); }
    /** 记录一次能力冲突说明 */
    public void addConflict(String conflict) { conflicts.add(conflict); }
    /** 记录一次缺失依赖说明 */
    public void addMissingDep(String dep) { missingDependencies.add(dep); }

    public List<String> getOrderedPluginIds() { return Collections.unmodifiableList(new ArrayList<>(orderedPluginIds)); }
    public Map<String, PluginState> getStates() { return Collections.unmodifiableMap(new LinkedHashMap<>(states)); }
    public Map<String, String> getErrors() { return Collections.unmodifiableMap(new LinkedHashMap<>(errors)); }
    public List<String> getConflicts() { return Collections.unmodifiableList(new ArrayList<>(conflicts)); }
    public List<String> getMissingDependencies() { return Collections.unmodifiableList(new ArrayList<>(missingDependencies)); }
}
