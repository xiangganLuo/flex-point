package com.flexpoint.core.plugin.manage;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.event.EventBus;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.ext.interceptor.DefaultInterceptorRegistry;
import com.flexpoint.core.ext.interceptor.InterceptorRegistry;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.plugin.PluginLoadReport;
import com.flexpoint.core.plugin.PluginState;
import com.flexpoint.core.plugin.exception.PluginException;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * 默认插件管理器实现（极简模型）。
 *
 * <p>插件不再声明依赖/顺序/关键性——<b>装配顺序即注册顺序</b>，
 * 任何插件启动失败一律降级（标记 FAILED + 记入报告 + 继续），不会中断构建。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
@Slf4j
public class DefaultPluginManager implements PluginManager {

    /** 保持注册（插入）顺序 */
    private final Map<String, Plugin> plugins = new LinkedHashMap<>();
    private final Map<String, PluginState> states = new LinkedHashMap<>();
    private final PluginLoadReport report = new PluginLoadReport();

    private final PluginContext context;

    public DefaultPluginManager(ExtAbilityRegistry extRegistry,
                                SelectorRegistry selectorRegistry,
                                EventBus eventBus,
                                ExtMonitor monitor,
                                FlexPointConfig config) {
        this(extRegistry, selectorRegistry, eventBus, monitor, config, new DefaultInterceptorRegistry());
    }

    public DefaultPluginManager(ExtAbilityRegistry extRegistry,
                                SelectorRegistry selectorRegistry,
                                EventBus eventBus,
                                ExtMonitor monitor,
                                FlexPointConfig config,
                                InterceptorRegistry interceptorRegistry) {
        this.context = new DefaultPluginContext(extRegistry, selectorRegistry, eventBus, monitor, config, interceptorRegistry);
    }

    @Override
    public synchronized void register(Plugin plugin) {
        String id = plugin.getId();
        if (id == null || id.trim().isEmpty()) {
            throw new PluginException("pluginId must not be empty");
        }
        if (plugins.containsKey(id)) {
            throw new PluginException("Duplicate pluginId: " + id);
        }
        plugins.put(id, plugin);
        states.put(id, PluginState.CREATED);
        log.debug("插件已注册: id={}, class={}", id, plugin.getClass().getName());
    }

    @Override
    public synchronized void registerAll(Iterable<Plugin> list) {
        if (list == null) return;
        for (Plugin p : list) register(p);
    }

    @Override
    public synchronized void installAll() {
        log.debug("开始按注册顺序装配插件, 数量={}, 顺序={}", plugins.size(), plugins.keySet());
        for (Plugin p : plugins.values()) {
            String id = p.getId();
            report.addOrdered(id);
            try {
                log.debug("装配插件: id={} -> init", id);
                p.init(context); states.put(id, PluginState.INITIALIZED); report.setState(id, PluginState.INITIALIZED);
                log.debug("装配插件: id={} -> start", id);
                p.start();       states.put(id, PluginState.STARTED);     report.setState(id, PluginState.STARTED);
                log.info("Plugin started: {}", id);
            } catch (Exception e) {
                states.put(id, PluginState.FAILED);
                report.setState(id, PluginState.FAILED);
                report.addError(id, e.getMessage());
                log.error("Plugin start failed (degraded): {}", id, e);
            }
        }
    }

    @Override
    public synchronized void stopAll() {
        log.debug("开始逆序停止插件, 数量={}", plugins.size());
        List<Plugin> ordered = new ArrayList<>(plugins.values());
        ListIterator<Plugin> it = ordered.listIterator(ordered.size());
        while (it.hasPrevious()) {
            Plugin p = it.previous();
            String id = p.getId();
            try { p.stop(); states.put(id, PluginState.STOPPED); report.setState(id, PluginState.STOPPED); }
            catch (Exception e) { log.warn("stop error: {}", id, e); }
            try { p.destroy(); states.put(id, PluginState.DESTROYED); report.setState(id, PluginState.DESTROYED); }
            catch (Exception e) { log.warn("destroy error: {}", id, e); }
        }
    }

    /**
     * 运行期启用插件（幂等）。
     * <p>已 STARTED 直接返回；从 CREATED/FAILED/DESTROYED 进入先 init 再 start；
     * 从 STOPPED 进入仅 start。启用失败降级为 FAILED 并记入报告，不抛出。</p>
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
     * 运行期停用插件。
     * <p>仅对 STARTED 生效：stop() 反注册能力后置 STOPPED（不 destroy，便于后续 enable 重启）。</p>
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
    @Override public synchronized Map<String, PluginState> getPluginStates() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(states));
    }
    @Override public synchronized Plugin getPlugin(String pluginId) { return plugins.get(pluginId); }
}
