package com.flexpoint.core.plugin.manage;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.event.EventBus;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.plugin.*;
import com.flexpoint.core.plugin.exception.PluginException;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认插件管理器实现（第一阶段：最小可用）。
 */
@Slf4j
public class DefaultPluginManager implements PluginManager {
    private final Map<String, Plugin> plugins = new ConcurrentHashMap<>();
    private final Map<String, PluginState> states = new ConcurrentHashMap<>();
    private final PluginLoadReport report = new PluginLoadReport();

    private final ExtAbilityRegistry extRegistry;
    private final SelectorRegistry selectorRegistry;
    private final EventBus eventBus;
    private final ExtMonitor monitor;
    private final FlexPointConfig config;

    private List<Plugin> resolvedOrder = new ArrayList<>();

    public DefaultPluginManager(ExtAbilityRegistry extRegistry,
                                SelectorRegistry selectorRegistry,
                                EventBus eventBus,
                                ExtMonitor monitor,
                                FlexPointConfig config) {
        this.extRegistry = extRegistry;
        this.selectorRegistry = selectorRegistry;
        this.eventBus = eventBus;
        this.monitor = monitor;
        this.config = config;
    }

    @Override
    public void register(Plugin plugin) {
        String id = plugin.getDescriptor().getPluginId();
        if (plugins.putIfAbsent(id, plugin) != null) {
            throw new PluginException("Duplicate pluginId: " + id);
        }
        states.put(id, PluginState.CREATED);
    }

    @Override
    public void registerAll(Iterable<Plugin> list) {
        if (list == null) return;
        for (Plugin p : list) register(p);
    }

    @Override
    public void resolve() {
        Collection<Plugin> values = plugins.values();
        // 冲突检测（默认 SELECTOR、EVENT、MONITOR 为单例能力）
        EnumSet<PluginCapability> singletons = EnumSet.of(PluginCapability.SELECTOR, PluginCapability.EVENT, PluginCapability.MONITOR);
        ConflictDetector.detect(values, singletons);
        // 依赖解析
        resolvedOrder = DependencyResolver.resolveOrder(values);
        for (Plugin p : resolvedOrder) {
            report.addOrdered(p.getDescriptor().getPluginId());
        }
    }

    @Override
    public void installAll() {
        PluginContext ctx = new DefaultPluginContext(extRegistry, selectorRegistry, eventBus, monitor, config);
        for (Plugin p : resolvedOrder) {
            String id = p.getDescriptor().getPluginId();
            try {
                p.init(ctx); states.put(id, PluginState.INITIALIZED); report.setState(id, PluginState.INITIALIZED);
                p.start();   states.put(id, PluginState.STARTED);     report.setState(id, PluginState.STARTED);
                log.info("Plugin started: {}", id);
            } catch (Exception e) {
                states.put(id, PluginState.FAILED); report.setState(id, PluginState.FAILED); report.addError(id, e.getMessage());
                log.error("Plugin start failed: {}", id, e);
                if (p.getDescriptor().isCritical()) {
                    // 回滚已启动插件
                    rollback();
                    throw new PluginException("Critical plugin failed: " + id, e);
                }
            }
        }
    }

    private void rollback() {
        ListIterator<Plugin> it = resolvedOrder.listIterator(resolvedOrder.size());
        while (it.hasPrevious()) {
            Plugin p = it.previous();
            String id = p.getDescriptor().getPluginId();
            if (states.get(id) == PluginState.STARTED || states.get(id) == PluginState.INITIALIZED) {
                try { p.stop(); states.put(id, PluginState.STOPPED); }
                catch (Exception ignore) {}
                try { p.destroy(); states.put(id, PluginState.DESTROYED); }
                catch (Exception ignore) {}
            }
        }
    }

    @Override
    public void stopAll() {
        ListIterator<Plugin> it = resolvedOrder.listIterator(resolvedOrder.size());
        while (it.hasPrevious()) {
            Plugin p = it.previous();
            String id = p.getDescriptor().getPluginId();
            try { p.stop(); states.put(id, PluginState.STOPPED); report.setState(id, PluginState.STOPPED);} catch (Exception e) { log.warn("stop error: {}", id, e);} 
            try { p.destroy(); states.put(id, PluginState.DESTROYED); report.setState(id, PluginState.DESTROYED);} catch (Exception e) { log.warn("destroy error: {}", id, e);} 
        }
    }

    @Override public void enable(String pluginId) { /* P1: 运行期启停 */ }
    @Override public void disable(String pluginId) { /* P1: 运行期启停 */ }

    @Override public PluginLoadReport getLoadReport() { return report; }
    @Override public Map<String, PluginState> getPluginStates() { return Collections.unmodifiableMap(states); }
    @Override public Plugin getPlugin(String pluginId) { return plugins.get(pluginId); }
}
