# Flex Point 核心代码 Review（2026-07-16）

> 范围：`flexpoint-core` 为主，涉及 `flexpoint-common` 契约。基线：master @ cda9054，全部单测通过（本次新增后 33/33）。
> 结论：内核整体健壮，Phase A/B 主体已落地。发现 1 处**依赖解析正确性缺陷（已修复）**，1 处**插件生命周期治理缺口（需决策）**，及若干中低优先级项。

---

## 已在本次修复

### [已修复] DependencyResolver：order 全局重排破坏拓扑顺序
- **文件**：`plugin/manage/DependencyResolver.java`
- **问题**：Kahn 拓扑排序后又对整体 `sort(by order)`。当「被依赖者 order > 依赖者 order」时，重排会把依赖方排到被依赖方之前。
  - 复现：`dep(order=100)` 被 `main(order=1, deps=[dep])` 依赖 → 结果 `[main, dep]`，main 先于 dep 装配（错误）。
- **修复**：将 `order` 改为 Kahn 主循环内「就绪集合」的优先级（`PriorityQueue`，order 升序、id 字典序兜底），移除结束后的全局重排。既保证拓扑正确，又保留 order 语义与确定性。
- **回归**：`DependencyAndConflictTest#dependency_order_must_win_over_order_field`。

---

## 需决策项（未改动，等待确认）

### [高] 插件生命周期未被 FlexPoint 持有 → stop/destroy 从不触发
- **文件**：`FlexPointBuilder.build()` / `FlexPoint.java`
- **现象**：`build()` 创建 `DefaultPluginManager`、`installAll()` 后，`FlexPoint` **不持有** manager。因此：
  1. `FlexPoint.shutdown()` 只关事件总线，**从不调用 `pm.stopAll()`** → 插件 `stop()/destroy()` 永不执行；`ObservabilityPlugin` 的 EventBus 订阅与 monitor handler 无法清理。
  2. `PluginLoadReport` / `getPluginStates()` 在 `build()` 后被丢弃，**无法观测**（与蓝图「加载报告为第一等公民」相悖）。
  3. `AsyncExtMonitor` 亦无 shutdown 调用（守护线程，JVM 退出前泄漏）。
- **建议**：`FlexPoint` 持有 `PluginManager`，`shutdown()` 逆序 `stopAll()`；暴露 `getPluginLoadReport()/getPluginStates()`。属对 `FlexPoint` 构造签名的内部改动。

### [中][已修复 2026-07-16] findAbility 事件语义存在重复/矛盾事件
- **文件**：`FlexPoint.findAbility` + `DefaultExtAbilityRegistry.getAllExtAbility`
- **现象**：
  - `getAllExtAbility` 在每次读取时发布 `EXT_FOUND`/`EXT_NOT_FOUND`；`findAbility` 之后可能再次发布 `EXT_NOT_FOUND` → **同一次查找出现 EXT_NOT_FOUND 两次**，或（类型已注册后全部注销、list 空非 null 时）**同时出现 EXT_FOUND + EXT_NOT_FOUND**。
  - `SELECTOR_FOUND` 在校验选择器是否存在**之前**发布，选择器不存在时会紧接 `SELECTOR_NOT_FOUND`。
- **影响**：订阅者/监控口径噪声。
- **修复**：`getAllExtAbility` 改为无副作用纯读取；查找语义由 `FlexPoint` 单一发布；`SELECTOR_FOUND` 移到确认存在之后。回归见 `EventSemanticsTest`。

### [中] EventConfig 未纳入配置校验
- **文件**：`config/FlexPointConfigValidator`
- **现象**：只校验 monitor/registry；`EventConfig`（线程池 core/max/queue）无校验，非法值（如负数）到 `ThreadPoolExecutor` 构造期才抛异常。Phase A「E 组配置治理」尚有此缺口。

---

## 低优先级 / 清理项

- **[低] `AbstractChainExtMonitor.recordInvocation`**：循环内对每个 handler 都调用一次 `getExtMetrics()`（O(N²) 且语义微妙——内置 handler 并不使用该入参，第三方可能使用）。不建议贸然巻き上げ；如需优化应先明确「metrics 入参是否要求为逐 handler 最新快照」的契约。
- **[低] `PluginLoadReport`**：JavaDoc 示例引用了不存在的 `addConflict(...)`；`addMissingDep(...)` 为死方法（无调用点）。建议同步文档并清理或接线。
- **[低] `AbstractChainExtMonitor.getAllExtMetrics` / `getExtMetrics`**：无 MetricsProvider 时返回 `null`（`FlexPoint.getAllExtMetrics` 直接透出 null，调用方有 NPE 风险）。建议返回空集合。
- **[低] 描述符 capabilities 非空**：`TASK_BREAKDOWN` 声称「能力集合非空已校验」，但 `PluginDescriptorValidator` 仅判 `null`，允许空集合。文档与实现不一致——要么强校验，要么订正文档。
- **[低] `DefaultSelectorRegistry.unregister`**：选择器不存在时仍发布 `SELECTOR_UNREGISTERED` 再打 warn（为 no-op 发事件）。
- **[低] `EventPublisherInvocationHandler`**：每次调用 `method.setAccessible(true)`（接口代理，方法本为 public，可省）。

---

## 已确认良好的部分

- 事件链路实例级装配、多实例隔离与引用释放（Phase A A1）✅
- 计数口径统一走 `Registry.getRegisteredCount()`（C1）✅
- 反射异常语义区分 `INVOKE_FAIL`（业务异常，解包 ITE）/`INVOKE_EXCEPTION`（框架/反射错误）（D1）✅
- 注册中心并发：`ConcurrentHashMap + CopyOnWriteArrayList` + 读取快照，已验证并发一致性（B 组，本次新增 `ConcurrentRegistryTest`）✅
- EventBus 线程池可配置化 + 拒绝策略大小写不敏感回退（E 组）✅
- 插件依赖解析（拓扑/环/缺失）、资源级唯一（选择器同名禁止覆盖）、关键/非关键失败差异化 ✅

---

## 本次新增测试（33 项全通过）

- `plugin/DependencyAndConflictTest`（9）：依赖顺序/回归、缺失/循环/重复、资源级唯一、关键失败
- `plugin/PluginLifecycleTest`（2）：状态机流转、安装正序/停止逆序
- `plugin/PluginDescriptorValidationTest`（5）：id/semver 校验
- `plugin/PluginContextBoundaryTest`（1）：上下文能力边界
- `registry/ConcurrentRegistryTest`（1）：并发注册/遍历一致性
