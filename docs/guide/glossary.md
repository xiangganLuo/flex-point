# 术语表

Flex Point 常用术语的简明定义，便于统一理解。更完整的说明见对应章节链接。

## 扩展点与实现

- **扩展点 ExtAbility** —— 一项「可扩展能力」的接口抽象；同一扩展点可有多套实现，运行期由选择器选中其一。见 [扩展点](/guide/ext#扩展点-extability)。
- **扩展点实现** —— 实现某个扩展点接口的类。Spring 环境下标注 `@Component` 即被自动注册。
- **code** —— `ExtAbility.getCode()` 返回的业务标识，区分不同实现（如 `mall` / `logistics`），最常用的路由依据。
- **tags（ExtTags）** —— 扩展点的键值元数据（不可变），承载版本、区域、权重等任意信息；版本号约定放在 `version` 标签。见 [标签 ExtTags](/guide/ext#标签-exttags)。
- **extId** —— 扩展点实现的全局唯一标识，默认格式「扩展点接口简单名#实现全限定名」，用于日志、监控与决策解释。

## 选择器与决策

- **选择器 Selector** —— 从候选实现中选出目标的路由决策器，`getName()` 全局唯一，`select(candidates)` 返回 `SelectionResult`。见 [选择器](/guide/selector)。
- **AbstractSelector** —— 选择器模板基类；实现 `filter()` 即可，模板方法按「空→MISS、唯一→HIT、多个→AMBIGUOUS」归结结论。
- **SelectionResult** —— 一次选择的结果，含命中实现、决策解释与结论。
- **结论 Outcome（HIT / MISS / AMBIGUOUS）** —— `HIT` 命中唯一候选；`MISS` 无候选通过过滤；`AMBIGUOUS` 多个候选未收敛。
- **决策解释 DecisionExplanation** —— 不可变的选择快照：选择器名、候选/过滤后 extId 列表、命中 extId、结论、原因；用于排查路由问题。
- **@FpSelector** —— 标注在扩展点接口上的注解，其 `value` 与选择器 `getName()` 对应，声明该扩展点使用哪个选择器。

## 插件

- **插件 Plugin** —— 以统一生命周期接入框架的扩展单元（选择器、拦截器、事件订阅、监控等都以插件形式实现）。模型极简：`getId()` + 生命周期。见 [插件体系](/guide/plugin)。
- **PluginLifecycle** —— 插件生命周期：`init → start → stop → destroy`。
- **PluginState** —— 插件状态：`CREATED / INITIALIZED / STARTED / STOPPED / FAILED / DESTROYED`。
- **PluginManager** —— 插件的注册、装配、启停与状态维护者。
- **PluginContext** —— 插件访问内核的唯一受控入口：`extRegistry / selectorRegistry / eventBus / monitor / config / interceptorRegistry`。
- **PluginLoadReport** —— 加载报告：装配顺序、各插件状态、失败原因。
- **pluginId** —— 插件全局唯一标识（`getId()`），重复注册直接失败。

## 调用与拦截

- **调用管线** —— 扩展点方法调用经动态代理进入的一条拦截器链，终点是真实实现方法。见 [调用管线与拦截器](/guide/ext#调用管线与拦截器)。
- **拦截器 ExtInvocationInterceptor** —— around 语义的调用拦截器；`order()` 越小越靠外，`intercept()` 内通过 `proceed()` 推进。
- **ExtInvocation** —— 可推进的调用上下文：`getTarget / getMethod / getArgs / proceed`；`proceed()` 可重入（支持重试）。
- **InterceptorRegistry** —— 拦截器注册表，`getInterceptors()` 返回按 `order` 升序的快照。
- **EventPublishingInterceptor** —— core 内置、始终最内层（`order = Integer.MAX_VALUE`）的事件埋点拦截器，围绕真实调用发布 `INVOKE_*` 事件。

## 事件与监控

- **事件 EventContext / EventType** —— `EventType` 为 14 个事件枚举（扩展点生命周期 / 调用 / 选择器三类）；`EventContext` 承载事件信息。见 [可观测](/guide/observability#事件体系)。
- **事件总线 EventBus** —— 实例级事件分发器，支持同步 / 异步、订阅、路由与过滤。
- **EventSubscriber** —— 事件订阅者，可声明 `getPriority()` / `isAsync()` / `getEventFilter()`。
- **监控 ExtMonitor** —— 记录调用与异常、产出指标的监控门面，内部为处理器责任链。
- **ExtMetrics** —— 单个扩展点的调用指标：次数、成功率、平均/最大/最小耗时、异常数、QPS 等。

## 上下文与约束

- **标准上下文 FlexPointContext** —— 承载请求维度路由依据（`tenantId / appCode / version / uid / labels / attributes`）的线程级上下文；基于普通 `ThreadLocal`，请求结束须 `clear()`。见 [标准上下文](/guide/ext#标准上下文-flexpointcontext)。
- **资源级唯一** —— 选择器名等「资源名」在注册表内禁止同名覆盖，重复注册失败（区别于扩展点「允许多实现」）。
- **装配顺序 = 注册顺序** —— 插件按注册先后 `init → start`，关闭时逆序 `stop → destroy`；Spring Boot 下即容器中 `Plugin` Bean 的收集顺序。
- **统一降级** —— 任一插件启动失败只标记 `FAILED` 并记入报告，不中断整体构建。
