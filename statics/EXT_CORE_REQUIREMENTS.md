# 扩展点核心模块（ext）需求与设计文档

> 状态：待评审（draft）
> 定位：`flexpoint-core` 的 `com.flexpoint.core.ext` 是整个框架的核心。本文沉淀其职责、领域模型、调用管线（含拦截器 SPI）与接线要求，作为后续实现与并行开发的对齐基线。
> 关联：标准上下文（土台 B）、配置即装配（土台 A）、行为增强插件（依赖本文的拦截器 SPI）。

---

## 1. 背景与定位

`ext` 承载扩展点的“**定义 → 注册 → 查找 → 调用**”全生命周期，是 selector（选谁）、event（观测）、monitor（消费观测）、plugin（治理装配）等模块共同围绕的中心。

当前 `ext` 已具备：能力契约（`ExtAbility`）、元数据（`ExtTags`）、注册中心（`ExtAbilityRegistry`/`DefaultExtAbilityRegistry`）、调用代理（`ext.proxy.EventPublisherInvocationHandler`，仅做事件埋点）。

**缺口**：调用管线只能“观测”，不能“干预行为”。要支持重试/超时/熔断/限流/缓存等**实用能力**，需要在调用管线引入统一的 **around 拦截 SPI**。这是本次核心扩展。

---

## 2. 目标 / 非目标

### 目标（In Scope）
1. 明确 `ext` 模块职责与包结构边界。
2. 固化领域模型：`ExtAbility` / `ExtTags` / `extId` 语义。
3. 固化注册中心语义：并发一致性、快照读取、计数口径、资源级唯一。
4. **新增调用拦截器 SPI（around）**：可在调用前后插入行为，且可重入（支持重试）。
5. 明确调用管线：`拦截链 → 事件埋点终端 → 反射调用`，无拦截器时零额外开销。
6. 明确接线：`FlexPoint`/`FlexPointBuilder`/`PluginContext` 如何提供拦截器注册与装配。
7. 明确与标准上下文（土台 B）、配置装配（土台 A）的关系。

### 非目标（Out of Scope）
- 不在本期做“按扩展点类型/注解精细化拦截”（v1 全局拦截，v2 再演进）。
- 不在本期做分布式/远程调用治理。
- 具体行为插件（Retry/Resilience 等）的实现细节不在本文，另见插件模块设计。

---

## 3. 术语
| 术语 | 含义 |
|---|---|
| 扩展点（ExtAbility） | 一类可被多实现替换的业务能力接口 |
| 扩展点实现 | 实现了某扩展点接口的具体类实例 |
| code | 业务标识，用于路由区分不同实现 |
| tags | 扩展点元数据（版本、标签等），完全抽象的键值 |
| extId | 扩展点实现的唯一标识：`接口简名#实现全限定名` |
| 选择器（Selector） | 从候选实现中选出目标的策略 |
| 拦截器（Interceptor） | 环绕扩展点方法调用的 around 增强 |

---

## 4. 职责与边界

```
                       ┌──────────── selector（选谁）
                       │
   register/find ──► ext（定义·注册·调用管线）
                       │
       调用管线 ──────► event（发事件/观测出口）──► monitor（指标/告警，消费端）
                       │
       读取上下文 ◄──── context（FlexPointContext，土台B）
                       │
       拦截/装配 ◄──── plugin（PluginContext 提供注册入口）
```

- **ext 只负责**：能力定义、注册/查找、调用管线（代理 + 拦截 + 事件埋点终端）。
- selector 的“选择结果”交给 ext 包装为代理；event 是 ext 调用管线的观测出口；monitor 不属于 ext；context 是 ext 调用/路由读取的标准入参；plugin 通过 `PluginContext` 向 ext 注册拦截器/能力。

---

## 5. 领域模型需求

### 5.1 `ExtAbility`
- 必含 `String getCode()`（业务标识）。
- `default ExtTags getTags()`（元数据，默认空）。
- `default String getExtId()`：`接口简名#实现全限定名`，作为监控/日志的稳定标识。
- 约束：实现类可为非 public（内部/包级）——调用管线须能反射调用其 public 接口方法（`setAccessible(true)`）。

### 5.2 `ExtTags`
- 抽象键值元数据（无业务概念），支持 string/int/bool/set/list 读取与默认值。
- 版本等语义由上层（如 CodeVersion 选择器）以约定 tag key 表达。

---

## 6. 注册中心需求（`ExtAbilityRegistry`）

- `register/unregister/getAllExtAbility/getRegisteredCount`。
- 一个 `code` 可对应多个实现（按 tags 区分）。
- **并发一致性**：类型→实现列表使用并发友好结构；`getAllExtAbility` 返回**快照**，读取期间不受并发写影响。
- **纯读取无副作用**：`getAllExtAbility` 不发布事件；查找语义（找到/未找到）由 `FlexPoint` 在正确节点单一发布。
- **计数口径统一**：总数由注册中心 `getRegisteredCount()` 提供。
- 生命周期事件：注册/注销发布 `EXT_REGISTERED`/`EXT_UNREGISTERED`（异步）。

---

## 7. 调用管线需求（核心）

### 7.1 管线结构
```
findAbility() 选中实现
      │
      ▼
代理创建（JDK 动态代理）
      │  proxy.method(...)
      ▼
┌────────────────────────────────────────────────┐
│ around 拦截链（按 order 升序，越小越外层）           │
│   [限流] → [熔断] → [重试] → [超时] → ...            │
│                      │ proceed()                   │
│                      ▼                             │
│   终端 ExtInvocationTerminal：                      │
│     INVOKE_BEFORE → method.invoke                  │
│       → INVOKE_SUCCESS / INVOKE_FAIL / EXCEPTION   │
└────────────────────────────────────────────────┘
```

### 7.2 关键要求
- **零开销回退**：无拦截器时直连终端，行为与现状完全一致。
- **可重入**：`proceed()` 可被同一拦截器多次调用（重试），每次重跑其后链与终端；实现上不共享可变游标（每次构造下游节点）。
- **Object 方法短路**：`toString/hashCode/equals` 直接转发，不埋点、不拦截。
- **事件在终端**（决策项 D1）：每次真正的方法调用都发一组 `INVOKE_*` 事件，重试会产生多组事件（可观测重试过程）。

### 7.3 事件语义
| 事件 | 触发 |
|---|---|
| `INVOKE_BEFORE` | 进入终端、反射调用前 |
| `INVOKE_SUCCESS` | 正常返回（携带耗时/结果） |
| `INVOKE_FAIL` | 目标业务异常（解包 `InvocationTargetException`，透出原始异常） |
| `INVOKE_EXCEPTION` | 框架/反射层异常（如非法访问） |

---

## 8. 拦截器 SPI 契约（新增）

包：`com.flexpoint.core.ext.interceptor`

```java
/** 可推进的调用上下文（链节点） */
public interface ExtInvocation {
    ExtAbility getTarget();
    Method getMethod();
    Object[] getArgs();
    Object proceed() throws Throwable;   // 推进其后链与终端；可多次调用（重试）
}

/** around 拦截器 */
public interface ExtInvocationInterceptor {
    Object intercept(ExtInvocation invocation) throws Throwable;
    default int order() { return 0; }    // 越小越外层
    default String name() { return getClass().getSimpleName(); }
}

/** 终端：真正的方法调用（事件埋点 + 反射） */
@FunctionalInterface
public interface ExtInvocationTerminal { Object invoke() throws Throwable; }

/** 拦截器注册表 */
public interface InterceptorRegistry {
    void register(ExtInvocationInterceptor interceptor);
    void unregister(ExtInvocationInterceptor interceptor);
    List<ExtInvocationInterceptor> getInterceptors();  // 按 order 升序快照
}
```

- 默认实现 `DefaultInterceptorRegistry`（并发安全，`CopyOnWriteArrayList` + 排序快照）。
- 链实现 `DefaultExtInvocation`（可重入）。

### 8.1 顺序约定（建议默认值）
| 拦截器 | order | 说明 |
|---|---|---|
| 限流 RateLimit | 100 | 最外层，超阈直接拒绝 |
| 熔断 CircuitBreaker | 200 | 熔断打开则快速失败/降级 |
| 重试 Retry | 300 | 包裹超时，逐次重试 |
| 超时 Timeout | 400 | 每次尝试独立超时 |

---

## 9. 标准上下文（土台 B）与 ext 的关系

- 新增 `com.flexpoint.core.context.FlexPointContext`（ThreadLocal：`tenantId/appCode/version/uid/labels/attributes`）。
- 路由选择器读取该上下文即可路由，**无需业务方编写 Resolver**（配合接入层 Filter 在入口填充）。
- 拦截器亦可读取上下文做条件判断（如按租户开启熔断）。
- 请求结束须 `clear()`，避免线程池串扰（接入层负责）。

---

## 10. 接线要求（最小侵入）

- `FlexPoint` 持有 `InterceptorRegistry`；`getProxy` 从其取有序拦截器传给代理处理器。
- `FlexPointBuilder.build()` 创建唯一 `DefaultInterceptorRegistry`，同时注入 `FlexPoint` 与 `DefaultPluginManager`（进而进入 `PluginContext`），确保“插件注册的拦截器”与“FlexPoint 读取的拦截器”是同一份。
- `PluginContext` 新增 `interceptorRegistry()`；行为插件在 `start()` 内 `register(...)`，`stop()` 内 `unregister(...)`。
- 构造签名新增参数，保留旧构造重载（默认空注册表），兼容既有直接构造方（如测试）。
- 建议：将 `FlexPoint.getProxy(...)` 抽到 `ext.proxy.ExtAbilityProxyFactory`，使代理装配内聚于 ext（决策项 D4）。

---

## 11. 配置即装配（土台 A）关系

- Spring Boot 层新增 `flexpoint.plugins.*` 属性与 `FlexPointPluginsAutoConfiguration`，按开关把官方插件（含行为增强类，内部注册拦截器）自动注册为 `Plugin` Bean。
- 使用方仅通过配置启用能力，无需编码。ext 本身不感知 Spring。

---

## 12. 非功能需求
- **性能**：无拦截器零额外开销；拦截链为浅层方法调用；日志走 SLF4J 参数化 / `isDebugEnabled` 守护。
- **线程安全**：注册表并发安全；上下文线程隔离。
- **可观测**：装配/调用/拦截关键节点有 debug 日志；调用事件语义稳定。
- **兼容**：保留旧构造与旧行为；无拦截器/无上下文时与现状一致。

---

## 13. 兼容性与迁移
- `EventPublisherInvocationHandler` 构造新增 `interceptors` 参数（`FlexPoint.getProxy` 负责传入；无则空列表）。
- 直接使用旧构造的调用方通过重载保持兼容。
- 现有测试（无拦截器路径）应零改动通过。

---

## 14. 测试要求
- 空链回归：行为与现状一致（既有用例全绿）。
- 单/多拦截器 around 顺序正确。
- 重试：拦截器多次 `proceed()` 重跑终端，事件多组。
- 异常透传：业务异常（FAIL）与框架异常（EXCEPTION）语义不变。
- Object 方法短路：不触发事件/拦截。
- 上下文：选择器读取 `FlexPointContext` 路由；`clear()` 生效。
- 拦截器注册/注销：经 `PluginContext` 生效与撤销。

---

## 15. 待评审的关键决策（请 review 时确认）
| 编号 | 决策 | 建议 |
|---|---|---|
| D1 | 事件在**终端**（每次实际调用发事件，重试可见）还是最外层（每逻辑调用一组） | 终端（可观测重试） |
| D2 | 拦截作用域：v1 **全局** 还是按类型/注解精细化 | v1 全局，v2 精细化 |
| D3 | 顺序约定（限流100/熔断200/重试300/超时400） | 采纳，由 `order()` 可覆盖 |
| D4 | 是否抽取 `ext.proxy.ExtAbilityProxyFactory` | 抽取，代理装配内聚 |
| D5 | 上下文是否用 TransmittableThreadLocal 支持父子线程传递 | v1 用普通 ThreadLocal，异步传递后续再评估 |

---

## 16. 里程碑
1. **土台（本文范围，lead 单人完成，不可并行）**：`FlexPointContext`、拦截器 SPI + 接线、`ext.proxy` 内聚。
2. **并行插件开发（3 路后台 agent 异步）**：路由（读上下文）/ 观测（事件·监控）/ 行为（注册拦截器）。
3. **集成**：`FlexPointPluginsAutoConfiguration` 配置装配 + 全量测试 + 文档。

---

## 附：当前实现进度（截至本稿）
- ✅ `FlexPointContext`（土台 B）已落地。
- 🚧 拦截器 SPI：`ExtInvocation`/`ExtInvocationInterceptor`/`ExtInvocationTerminal`/`InterceptorRegistry`/`DefaultInterceptorRegistry`/`DefaultExtInvocation` 已创建；代理终端化改造已完成；**FlexPoint/Builder/PluginContext 接线进行中（未编译通过，未提交）**。
- ⏸ 评审通过后继续接线并恢复三路并行。
