package com.flexpoint.core;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.common.constants.FlexPointConstants;
import com.flexpoint.common.exception.MultipleExtMatchedException;
import com.flexpoint.common.exception.SelectorNotFoundException;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.event.EventBus;
import com.flexpoint.core.event.EventDispatcher;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.ext.interceptor.DefaultInterceptorRegistry;
import com.flexpoint.core.ext.interceptor.ExtInvocationInterceptor;
import com.flexpoint.core.ext.interceptor.InterceptorRegistry;
import com.flexpoint.core.ext.proxy.EventPublishingInterceptor;
import com.flexpoint.core.ext.proxy.ExtInvocationHandler;
import com.flexpoint.core.monitor.ExtMetrics;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.plugin.PluginLoadReport;
import com.flexpoint.core.plugin.PluginState;
import com.flexpoint.core.plugin.manage.PluginManager;
import com.flexpoint.core.selector.DecisionExplanation;
import com.flexpoint.core.selector.SelectionResult;
import com.flexpoint.core.selector.Selector;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 扩展点管理器
 * 负责扩展点注册、查找、监控，选择器通过名称管理
 * @author xiangganluo
 */
@Slf4j
public class FlexPoint {

    @Getter
    private final ExtAbilityRegistry extAbilityRegistry;
    @Getter
    private final ExtMonitor extMonitor;
    @Getter
    private final SelectorRegistry selectorRegistry;
    private final EventDispatcher eventDispatcher;

    @Getter
    private final FlexPointConfig flexPointConfig;

    /**
     * 插件管理器（可为 null，表示以纯内核方式构建、未装配任何插件）。
     * 持有它是为了在 {@link #shutdown()} 时逆序停止插件，并对外暴露加载报告与状态。
     */
    private final PluginManager pluginManager;

    /**
     * 调用拦截器注册表（around 增强）。core 只提供 SPI 与默认注册表，具体拦截器由行为插件注册。
     */
    private final InterceptorRegistry interceptorRegistry;

    /**
     * 核心内置事件埋点拦截器（最内层，始终生效）。
     */
    private final ExtInvocationInterceptor eventPublishingInterceptor;

    /**
     * 扩展点代理缓存：同一 {@code (extType, ability)} 只建一次代理，避免每次查找都新建 Proxy。
     * <p>键基于身份（{@code ==}）比较，避免业务类重写 equals 造成的误共享；
     * 代理不冻结拦截链，链在每次 invoke 时从 {@link #interceptorRegistry} 惰性读取，
     * 因此缓存的代理仍能反映运行期动态增删的拦截器。</p>
     */
    private final Map<IdentityKey, Object> proxyCache = new ConcurrentHashMap<>();

    public FlexPoint(ExtAbilityRegistry extAbilityRegistry,
                     ExtMonitor extMonitor,
                     SelectorRegistry selectorRegistry,
                     EventDispatcher eventDispatcher,
                     FlexPointConfig flexPointConfig
    ) {
        this(extAbilityRegistry, extMonitor, selectorRegistry, eventDispatcher, flexPointConfig, null);
    }

    public FlexPoint(ExtAbilityRegistry extAbilityRegistry,
                     ExtMonitor extMonitor,
                     SelectorRegistry selectorRegistry,
                     EventDispatcher eventDispatcher,
                     FlexPointConfig flexPointConfig,
                     PluginManager pluginManager
    ) {
        this(extAbilityRegistry, extMonitor, selectorRegistry, eventDispatcher, flexPointConfig, pluginManager, null);
    }

    public FlexPoint(ExtAbilityRegistry extAbilityRegistry,
                     ExtMonitor extMonitor,
                     SelectorRegistry selectorRegistry,
                     EventDispatcher eventDispatcher,
                     FlexPointConfig flexPointConfig,
                     PluginManager pluginManager,
                     InterceptorRegistry interceptorRegistry
    ) {
        this.extAbilityRegistry = extAbilityRegistry;
        this.extMonitor = extMonitor;
        this.selectorRegistry = selectorRegistry;
        this.eventDispatcher = eventDispatcher;
        this.flexPointConfig = flexPointConfig;
        this.pluginManager = pluginManager;
        this.interceptorRegistry = interceptorRegistry != null ? interceptorRegistry : new DefaultInterceptorRegistry();
        this.eventPublishingInterceptor = new EventPublishingInterceptor(eventDispatcher);
    }

    public EventBus getEventBus() {
        return eventDispatcher.getEventBus();
    }

    /**
     * ==================ext==================
     */
    
    /**
     * 查找扩展点
     * 通过选择器查找并返回匹配的扩展点实例
     */
    public <T extends ExtAbility> T findAbility(Class<T> extType) {
        String typeName = extType.getSimpleName();
        log.debug("开始查找扩展点: type={}", typeName);

        // 从扩展点接口的@FpSelector注解获取选择器名称
        FpSelector selectorAnno = extType.getAnnotation(FpSelector.class);
        if (selectorAnno == null) {
            log.warn("扩展点类型[{}]缺少@FpSelector注解", typeName);
            return null;
        }

        String selectorName = selectorAnno.value();

        Selector selector = selectorRegistry.getSelector(selectorName);
        if (selector == null) {
            log.warn("未找到名称为[{}]的选择器", selectorName);
            // 选择器不存在：仅发布"未找到"事件
            eventDispatcher.publishSelectorNotFound(selectorName);
            throw new SelectorNotFoundException(selectorName, typeName);
        }
        // 选择器确实存在后再发布"找到"事件，避免与"未找到"矛盾
        eventDispatcher.publishSelectorFound(selectorName);

        List<T> exts = extAbilityRegistry.getAllExtAbility(extType);
        if (exts.isEmpty()) {
            log.warn("未找到扩展点实现: type={}", typeName);
            // 无候选：发布"未找到"事件（单一来源）
            eventDispatcher.publishExtNotFound(extType);
            return null;
        }
        // 存在候选：发布"找到"事件（单一来源）
        eventDispatcher.publishExtFound(extType);

        // 一次选择，同时产出命中/解释/结论（不再 select+explain 双跑）
        SelectionResult<T> result = selector.select(exts);
        DecisionExplanation explanation = result.getExplanation();
        // 决策解释：默认 Debug 级输出候选/过滤/命中原因，便于排查路由问题
        if (log.isDebugEnabled()) {
            log.debug("扩展点选择决策: type={}, {}", typeName, explanation);
        }

        switch (result.getOutcome()) {
            case HIT: {
                T ability = result.getSelected();
                // 发布扩展点选择事件（携带决策解释）
                eventDispatcher.publishExtSelected(ability, selectorName, explanation);
                if (log.isDebugEnabled()) {
                    log.debug("成功获取扩展点: type={}, code={}, selector={}, class={}",
                            typeName, ability.getCode(), selectorName, ability.getClass().getName());
                }
                return getProxy(extType, ability);
            }
            case AMBIGUOUS: {
                int matched = explanation.getFilteredExtIds().size();
                log.warn("选择器[{}]命中多个候选({}): type={}", selectorName, matched, typeName);
                eventDispatcher.publishExtSelectionFailed(extType, selectorName, "命中多个候选", explanation);
                throw new MultipleExtMatchedException(selectorName, matched);
            }
            case MISS:
            default: {
                log.warn("选择器[{}]未找到匹配的扩展点: type={}", selectorName, typeName);
                eventDispatcher.publishExtSelectionFailed(extType, selectorName, "选择器未找到匹配的扩展点", explanation);
                return null;
            }
        }
    }

    /**
     * 根据扩展点类型和code查找匹配的扩展点列表
     *
     * @param extType 扩展点类型
     * @param code 业务标识
     * @param <T> 扩展点类型
     * @return 匹配的扩展点列表
     */
    public <T extends ExtAbility> List<T> findAbilitysByCode(Class<T> extType, String code) {
        List<T> exts = extAbilityRegistry.getAllExtAbility(extType);
        if (exts.isEmpty()) {
            // 发布扩展点未找到事件
            eventDispatcher.publishExtNotFound(extType);
            return Collections.emptyList();
        }
        // 存在候选：发布"找到"事件（单一来源）
        eventDispatcher.publishExtFound(extType);

        List<T> matched = exts.stream()
                .filter(ext -> Objects.equals(code, ext.getCode()))
                .map(ext -> getProxy(extType, ext))
                .collect(Collectors.toList());

        if (!matched.isEmpty()) {
            log.debug("按 code 匹配到扩展点: type={}, code={}, count={}", extType.getSimpleName(), code, matched.size());
            // 发布扩展点选择事件
            matched.forEach(ext -> eventDispatcher.publishExtSelected(ext, FlexPointConstants.CODE_SELECTOR_NAME));
        } else {
            log.debug("按 code 未匹配到扩展点: type={}, code={}", extType.getSimpleName(), code);
            // 发布扩展点选择失败事件
            eventDispatcher.publishExtSelectionFailed(extType, FlexPointConstants.CODE_SELECTOR_NAME, "未找到匹配的扩展点");
        }

        return matched;
    }

    /**
     * 根据扩展点类型、code查找匹配的扩展点
     *
     * @param extType 扩展点类型
     * @param code 业务标识
     * @param <T> 扩展点类型
     * @return 匹配的扩展点
     */
    public <T extends ExtAbility> T findAbilityByCode(Class<T> extType, String code) {
        List<T> matched = findAbilitysByCode(extType, code);
        return matched.isEmpty() ? null : matched.get(0);
    }

    /**
     * 根据扩展点类型、code和标签查找匹配的扩展点列表
     *
     * @param extType 扩展点类型
     * @param code 业务标识
     * @param tagsKeyValue 标签键值对
     * @param <T> 扩展点类型
     * @return 匹配的扩展点列表
     */
    public <T extends ExtAbility> List<T> findAbilitysByCodeAndTags(Class<T> extType, String code, Object... tagsKeyValue) {
        List<T> exts = extAbilityRegistry.getAllExtAbility(extType);
        if (exts.isEmpty()) {
            // 发布扩展点未找到事件
            eventDispatcher.publishExtNotFound(extType);
            return Collections.emptyList();
        }
        // 存在候选：发布"找到"事件（单一来源）
        eventDispatcher.publishExtFound(extType);

        // 构建标签映射（跳过 key 为 null 的键值对，避免 toString NPE）
        Map<String, Object> tagMap = new HashMap<>();
        for (int i = 0; i < tagsKeyValue.length; i += 2) {
            if (i + 1 < tagsKeyValue.length && tagsKeyValue[i] != null) {
                tagMap.put(tagsKeyValue[i].toString(), tagsKeyValue[i + 1]);
            }
        }

        List<T> matched = exts.stream()
                .filter(ext -> Objects.equals(code, ext.getCode()))
                .filter(ext -> {
                    // 标签匹配逻辑
                    return tagMap.entrySet().stream()
                            .allMatch(entry -> {
                                Object extValue = ext.getTags().get(entry.getKey());
                                return Objects.equals(entry.getValue(), extValue);
                            });
                })
                .map(ext -> getProxy(extType, ext))
                .collect(Collectors.toList());

        if (!matched.isEmpty()) {
            log.debug("按 code+tags 匹配到扩展点: type={}, code={}, tagCount={}, count={}",
                    extType.getSimpleName(), code, tagMap.size(), matched.size());
            // 发布扩展点选择事件
            matched.forEach(ext -> eventDispatcher.publishExtSelected(ext, FlexPointConstants.CODE_TAGS_SELECTOR_NAME));
        } else {
            log.debug("按 code+tags 未匹配到扩展点: type={}, code={}, tagCount={}",
                    extType.getSimpleName(), code, tagMap.size());
            // 发布扩展点选择失败事件
            eventDispatcher.publishExtSelectionFailed(extType, FlexPointConstants.CODE_TAGS_SELECTOR_NAME, "未找到匹配的扩展点");
        }
        return matched;
    }

    /**
     * 根据扩展点类型、code和标签查找匹配的扩展点
     *
     * @param extType 扩展点类型
     * @param code 业务标识
     * @param tagsKeyValue 标签键值对
     * @param <T> 扩展点类型
     * @return 匹配的扩展点
     */
    public <T extends ExtAbility> T findAbilityByCodeAndTags(Class<T> extType, String code, Object... tagsKeyValue) {
        List<T> matched = findAbilitysByCodeAndTags(extType, code, tagsKeyValue);
        return matched.isEmpty() ? null : matched.get(0);
    }

    /**
     * 按 code + tags 精确匹配，未命中时回退到 code-only 匹配（路由回退语义：specific → general）。
     *
     * <p>优先返回同时满足 code 与全部 tags 的实现；若不存在此类实现，则回退为仅按 code 匹配的实现；
     * 两级均未命中返回 null。适用于"标签精细化路由，缺省回落到基础 code 路由"的场景。</p>
     *
     * @param extType 扩展点类型
     * @param code 业务标识
     * @param tagsKeyValue 标签键值对
     * @param <T> 扩展点类型
     * @return 命中的扩展点；两级均未命中返回 null
     */
    public <T extends ExtAbility> T findAbilityByCodeAndTagsOrFallback(Class<T> extType, String code, Object... tagsKeyValue) {
        T byTags = findAbilityByCodeAndTags(extType, code, tagsKeyValue);
        if (byTags != null) {
            return byTags;
        }
        if (log.isDebugEnabled()) {
            log.debug("code+tags 未命中，回退到 code-only 匹配: type={}, code={}", extType.getSimpleName(), code);
        }
        return findAbilityByCode(extType, code);
    }

    /**
     * 获取指定类型的所有扩展点
     *
     * @param extType 扩展点类型
     * @param <T> 扩展点类型
     * @return 扩展点列表
     */
    public <T extends ExtAbility> List<T> getAllExt(Class<T> extType) {
        return extAbilityRegistry.getAllExtAbility(extType);
    }

    /**
     * 获取注册的扩展点总数
     */
    public int getExtCount() {
        return extAbilityRegistry.getRegisteredCount();
    }

    /**
     * 关闭 FlexPoint 相关资源。
     * <p>顺序：先逆序停止插件（插件 stop 可能需要反注册订阅/处理链，依赖事件总线仍在线），
     * 再关闭监控器异步资源，最后关闭事件总线。</p>
     */
    public void shutdown() {
        log.debug("FlexPoint 开始关闭: hasPluginManager={}", pluginManager != null);
        if (pluginManager != null) {
            try {
                log.debug("逆序停止插件...");
                pluginManager.stopAll();
            } catch (Exception e) {
                log.warn("插件停止过程中出现异常", e);
            }
        }
        if (extMonitor != null) {
            try {
                log.debug("关闭监控器异步资源...");
                extMonitor.shutdown();
            } catch (Exception e) {
                log.warn("监控器关闭过程中出现异常", e);
            }
        }
        log.debug("关闭事件总线...");
        eventDispatcher.shutdown();
        log.debug("FlexPoint 关闭完成");
    }

    /**
     * 获取插件加载报告（顺序/状态/错误）。未装配插件时返回 null。
     */
    public PluginLoadReport getPluginLoadReport() {
        return pluginManager != null ? pluginManager.getLoadReport() : null;
    }

    /**
     * 获取当前插件状态快照。未装配插件时返回空 Map。
     */
    public Map<String, PluginState> getPluginStates() {
        return pluginManager != null ? pluginManager.getPluginStates() : Collections.emptyMap();
    }

    /**
     * 运行期启用指定插件（未装配插件时为空操作）。
     */
    public void enablePlugin(String pluginId) {
        if (pluginManager != null) {
            pluginManager.enable(pluginId);
        }
    }

    /**
     * 运行期停用指定插件（未装配插件时为空操作）。
     */
    public void disablePlugin(String pluginId) {
        if (pluginManager != null) {
            pluginManager.disable(pluginId);
        }
    }

    /**
     * 注册扩展点
     */
    public void register(ExtAbility ext) {
        extAbilityRegistry.register(ext);
    }
    
    /**
     * 注销扩展点
     */
    public void unregister(ExtAbility ext) {
        extAbilityRegistry.unregister(ext);
    }

    /**
     * ==================selector==================
     */
    
    /**
     * 注册选择器
     */
    public void registerSelector(Selector selector) {
        selectorRegistry.register(selector);
    }

    /**
     * 注销选择器
     */
    public void unregisterSelector(String selectorName) {
        selectorRegistry.unregister(selectorName);
    }

    /**
     * 检查是否有指定名称的选择器
     */
    public boolean hasSelector(String selectorName) {
        return selectorRegistry.has(selectorName);
    }

    /**
     * ==================monitor==================
     */
    
    /**
     * 获取扩展点调用统计
     */
    public ExtMetrics getExtMetrics(ExtAbility extAbility) {
        return extMonitor.getExtMetrics(extAbility);
    }

    /**
     * 获取所有扩展点调用统计
     */
    public Map<String, ExtMetrics> getAllExtMetrics() {
        return extMonitor.getAllExtMetrics();
    }

    /**
     * 创建（或复用缓存的）扩展点代理，集成监控和事件发布功能。
     *
     * <p>同一 {@code (extType, ability)} 只建一次代理并缓存；拦截链不在建代理时冻结，
     * 而是由 {@code chainSupplier} 在每次 invoke 时从注册表读取最新快照，
     * 因此运行期通过插件 enable/disable 增删的拦截器对已缓存代理立即可见。</p>
     */
    @SuppressWarnings("unchecked")
    private <T extends ExtAbility> T getProxy(Class<T> extType, T ability) {
        IdentityKey key = new IdentityKey(extType, ability);
        return (T) proxyCache.computeIfAbsent(key, k -> {
            // 每次 invoke 时组装调用链：注册表拦截器（外→内，已按 order 排序）+ 事件埋点拦截器（最内层，始终生效）
            java.util.function.Supplier<List<ExtInvocationInterceptor>> chainSupplier = () -> {
                List<ExtInvocationInterceptor> chain = new ArrayList<>(interceptorRegistry.getInterceptors());
                chain.add(eventPublishingInterceptor);
                return chain;
            };
            if (log.isDebugEnabled()) {
                log.debug("创建扩展点代理: type={}, target={}", extType.getSimpleName(), ability.getClass().getName());
            }
            return Proxy.newProxyInstance(
                    ability.getClass().getClassLoader(),
                    new Class[]{extType},
                    new ExtInvocationHandler(ability, chainSupplier)
            );
        });
    }

    /**
     * 代理缓存键：基于身份比较（{@code extType} 相等 + {@code ability} 引用相同），
     * 避免业务扩展点类重写 equals/hashCode 导致不同实例被误判为同一键而共享代理。
     */
    private static final class IdentityKey {
        private final Class<?> extType;
        private final Object ability;
        private final int hash;

        IdentityKey(Class<?> extType, Object ability) {
            this.extType = extType;
            this.ability = ability;
            this.hash = 31 * extType.hashCode() + System.identityHashCode(ability);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof IdentityKey)) {
                return false;
            }
            IdentityKey other = (IdentityKey) o;
            // ability 用 == 比较；extType 为 Class，用 equals（等价于 ==）
            return this.ability == other.ability && Objects.equals(this.extType, other.extType);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

}
