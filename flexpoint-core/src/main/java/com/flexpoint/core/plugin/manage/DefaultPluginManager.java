package com.flexpoint.core.plugin.manage;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.event.EventBus;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.plugin.PluginLoadReport;
import com.flexpoint.core.plugin.PluginState;
import com.flexpoint.core.plugin.exception.PluginException;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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

    /** 受控上下文（运行期启停复用同一实例） */
    private final PluginContext context;

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
        this.context = new DefaultPluginContext(extRegistry, selectorRegistry, eventBus, monitor, config);
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
        // 依赖解析
        resolvedOrder = DependencyResolver.resolveOrder(values);
        for (Plugin p : resolvedOrder) {
            report.addOrdered(p.getDescriptor().getPluginId());
        }
    }

    @Override
    public void installAll() {
        for (Plugin p : resolvedOrder) {
            String id = p.getDescriptor().getPluginId();
            try {
                p.init(context); states.put(id, PluginState.INITIALIZED); report.setState(id, PluginState.INITIALIZED);
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

    /**
     * 运行期启用插件（最小可用）。
     * <p>幂等：已 STARTED 直接返回；从 CREATED/FAILED/DESTROYED 进入时会先 init 再 start；
     * 从 STOPPED（曾被 disable）进入时仅 start（资源未销毁，无需重复 init）。
     * 启用失败降级为 FAILED 并记入报告，不抛出以避免影响调用方。</p>
     */
    @Override
    public synchronized void enable(String pluginId) {
        Plugin p = plugins.get(pluginId);
        if (p == null) {
            log.warn("enable: 未知插件 {}", pluginId);
            return;
        }
        PluginState state = states.get(pluginId);
        if (state == PluginState.STARTED) {
            return;
        }
        try {
            if (state == PluginState.CREATED || state == PluginState.FAILED || state == PluginState.DESTROYED) {
                p.init(context);
                states.put(pluginId, PluginState.INITIALIZED);
                report.setState(pluginId, PluginState.INITIALIZED);
            }
            p.start();
            states.put(pluginId, PluginState.STARTED);
            report.setState(pluginId, PluginState.STARTED);
            log.info("Plugin enabled: {}", pluginId);
        } catch (Exception e) {
            states.put(pluginId, PluginState.FAILED);
            report.setState(pluginId, PluginState.FAILED);
            report.addError(pluginId, e.getMessage());
            log.error("Plugin enable failed: {}", pluginId, e);
        }
    }

    /**
     * 运行期停用插件（最小可用）。
     * <p>仅对 STARTED 生效：调用 stop() 反注册能力后置为 STOPPED（不 destroy，便于后续 enable 重启）。
     * 其他状态忽略。stop 异常记录日志但仍置为 STOPPED。</p>
     */
    @Override
    public synchronized void disable(String pluginId) {
        Plugin p = plugins.get(pluginId);
        if (p == null) {
            log.warn("disable: 未知插件 {}", pluginId);
            return;
        }
        PluginState state = states.get(pluginId);
        if (state != PluginState.STARTED) {
            log.debug("disable: 插件 {} 非运行态({})，忽略", pluginId, state);
            return;
        }
        try {
            p.stop();
        } catch (Exception e) {
            log.warn("disable: 插件 {} stop 异常", pluginId, e);
        }
        states.put(pluginId, PluginState.STOPPED);
        report.setState(pluginId, PluginState.STOPPED);
        log.info("Plugin disabled: {}", pluginId);
    }

    @Override public PluginLoadReport getLoadReport() { return report; }
    @Override public Map<String, PluginState> getPluginStates() { return Collections.unmodifiableMap(states); }
    @Override public Plugin getPlugin(String pluginId) { return plugins.get(pluginId); }
}
