package com.flexpoint.core.plugin.official;

import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.official.event.EventPlugin;
import com.flexpoint.core.plugin.official.monitor.MonitorPlugin;
import com.flexpoint.core.plugin.official.observability.alert.AlertStrategy;
import com.flexpoint.core.plugin.official.selector.CodeSelectorPlugin;
import com.flexpoint.core.plugin.official.selector.CodeVersionSelectorPlugin;
import com.flexpoint.core.plugin.official.selector.resolves.CodeSelector;
import com.flexpoint.core.plugin.official.selector.resolves.CodeVersionSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * 官方内置插件工厂方法集合（便于业务快速装配）。
 * @author xiangganluo
 */
public final class OfficialPlugins {
    private OfficialPlugins() {}

    public static EventPlugin eventPlugin() { return new EventPlugin(); }
    public static MonitorPlugin monitorPlugin() { return new MonitorPlugin(); }

    public static CodeSelectorPlugin codeSelectorPlugin(CodeSelector.CodeResolver r) {
        return new CodeSelectorPlugin(r);
    }

    public static CodeVersionSelectorPlugin codeVersionSelectorPlugin(CodeVersionSelector.CodeVersionResolver r) {
        return new CodeVersionSelectorPlugin(r);
    }

    /**
     * 最小推荐集：事件转监控 + 本地指标/告警 + Code 选择器。
     */
    public static List<Plugin> minimalWithCodeSelector(CodeSelector.CodeResolver r) {
        List<Plugin> list = new ArrayList<>();
        list.add(new com.flexpoint.core.plugin.official.observability.ObservabilityPlugin());
        list.add(codeSelectorPlugin(r));
        return list;
    }

    /**
     * 推荐组合（方案B 默认理念）：
     * - 一个观测插件（融合事件+监控）
     * - 一个选择器插件（Code 或 CodeVersion）
     */
    public static List<Plugin> recommended(CodeSelector.CodeResolver r) {
        return minimalWithCodeSelector(r);
    }

    public static List<Plugin> recommended(CodeVersionSelector.CodeVersionResolver r) {
        List<Plugin> list = new ArrayList<>();
        list.add(new com.flexpoint.core.plugin.official.observability.ObservabilityPlugin());
        list.add(codeVersionSelectorPlugin(r));
        return list;
    }

    /**
     * 推荐组合（可注入策略与采集器）。
     */
    public static List<Plugin> recommended(
            CodeSelector.CodeResolver r,
            List<AlertStrategy> alertStrategies,
            List<com.flexpoint.core.monitor.metrics.MetricsCollector> collectors) {
        List<Plugin> list = new ArrayList<>();
        list.add(new com.flexpoint.core.plugin.official.observability.ObservabilityPlugin(alertStrategies, collectors));
        list.add(codeSelectorPlugin(r));
        return list;
    }

    public static List<Plugin> recommended(
            CodeVersionSelector.CodeVersionResolver r,
            List<AlertStrategy> alertStrategies,
            List<com.flexpoint.core.monitor.metrics.MetricsCollector> collectors) {
        List<Plugin> list = new ArrayList<>();
        list.add(new com.flexpoint.core.plugin.official.observability.ObservabilityPlugin(alertStrategies, collectors));
        list.add(codeVersionSelectorPlugin(r));
        return list;
    }
}
