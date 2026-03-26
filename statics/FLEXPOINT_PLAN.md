# Flex Point 思想级项目蓝图（技术底座）

> 更新时间：2026-03-25
> 定位：只描述“思想与技术底座”，不落到实现细节与产品形态。

---

## 1. 设计哲学（Principles）
- 标准优先：平台团队制定“标准接口/语义/指标/策略字典”，业务线只实现标准；框架兜底注册、路由、监控、治理。
- 中台输出，商户定制：中台以“扩展点标准”输出公共能力；SaaS/多租户侧按标准提供实现；路由以“租户/商户/版本/环境/特性”等维度选择实现。
- 运行时可治理：所有接入通过统一 SPI，具备依赖/冲突/顺序/状态观测；失败可降级，行为可追溯。
- 渐进演进：默认零侵入接入；新能力先旁路叠加，稳定后再收敛为默认。

---

## 2. 分层与边界（思想层）
- 标准层（Contracts）：扩展点接口规范、选择语义、路由策略词汇表、能力标签字典、指标与事件模型。
- 编排层（Runtime Orchestration）：上下文采集、策略组合、选择决策、调用包装（监控/告警/超时/隔离/重试的“最小必要”）。
- 插件层（Plugin SPI）：统一生命周期/依赖/冲突/顺序/关键性；官方能力以“官方插件”提供（选择/事件/监控等），业务与三方按相同契约接入。
- 观测层（Observability）：事件与指标标准化（加载/决策/调用/异常）；“决策解释”与“加载报告”为第一等公民。
- 接入层（Adapters）：Spring/Spring Boot 等生态对接，原则是“默认即用，细节可配”。

---

## 3. 标准与契约（Contracts）
- 扩展点标准：明确输入/输出/错误语义/幂等/超时边界；禁止隐式全局状态。
- 策略标准：路由策略以“声明式策略单元 + 组合器”表达（租户/商户/版本/AB/灰度/环境/标签），可并/可串/可短路。
- 元数据标准：能力标签（tags）、版本（semver）、能力域（capability）、关键性（critical）、兼容 API 版本（apiVersion）。
- 事件与指标字典：选择命中/未命中、决策失败原因、加载顺序、启停状态、调用成功/失败/异常、性能分位。

---

## 4. 运行时编排与路由（思想）
- 输入：请求上下文（tenantId/appCode/env/version/labels）、平台配置（灰度规则/开关）、能力元数据。
- 流程：上下文解析 → 策略组合求值（可短路） → 候选集筛选 → 决策解释产出（为何命中/未命中） → 结果包装（监控/告警/限流等按需）。
- 决策确定性：同一上下文与配置下结果稳定；顺序与冲突规则显式可见。

---

## 5. 插件化治理（思想）
- 生命周期：created → initialized → started → stopped → destroyed；关键失败阻断，非关键降级并记录。
- 依赖/冲突：拓扑排序+环检测；能力域默认“单主插件”，可经“白名单豁免”放宽；顺序由 order 与依赖共同确定。
- 隔离：插件只见受控上下文（registry/selector/event/monitor/config），不可持有全局可变体。

---

## 6. 多租户/商户模型（思想）
- 上下文与命名：tenantId/appCode/env/version/labels 为标准字段；注册与路由带命名域，避免串扰。
- 覆盖与回退：租户优先于平台默认；找不到实现时按“租户→应用→平台默认”回退链可配置。
- 版本与灰度：租户内支持版本编排与灰度（比例/名单/规则）；与 AB/Feature Flag 可组合但需统一优先级。

---

## 7. 版本/灰度/变更安全（思想）
- 版本治理：扩展实现与策略版本采用 semver；配置/策略变更具备“干跑（dry-run）”对比（思想留白）。
- 灰度约束：灰度只影响路由，不改变接口语义；灰度可回滚，规则变更可审计。
- 兼容边界：新策略/新插件旁路叠加，达标后切主；保留兼容入口避免一次性迁移。

---

## 8. 可观测与诊断（思想）
- 两类关键对象：加载报告（顺序/状态/错误/冲突/缺依赖）与决策解释（候选/过滤路径/命中/未命中原因）。
- 指标最小集：加载成功率、关键插件失败阻断率、选择成功率/未命中率、调用成功率、P95/P99、异常分类。
- 事件分级：同步影响路径的事件最少化，其他走异步；异常不放大传播，告警与重试解耦。

---

## 9. 安全与信任（思想）
- 插件来源与完整性：为将来预留“签名校验/来源白名单/版本准入”；默认本地白名单与受控仓库。
- 资源配额（远期）：线程与内存使用软/硬限思想预留，不在当前阶段实现。

---

## 10. 开发者体验（DX）
- “一页纸”标准：扩展点/策略/指标/事件的单页面标准；配套最小示例模板（接口、策略、测试脚手架）。
- 自测脚手架：命名/版本/能力校验、契约测试（入参/出参/异常），以构建配置提供。
- 决策可解释性默认开启用于本地与预发，生产按级别采样。

---

## 11. 非目标（半年内不做）
- 治理 UI 与多语言 SDK（只定义接口与落地路径，不做实现）。
- 强制热更新（优先滚动/重启生效；为未来热更预留接口）。
- 深度安全沙箱（仅思想留白，未来引入）。

---

## 12. 半年里程碑（思想级，不含 UI）
- M1 基线（第 4–6 周）：标准字典（扩展点/策略/事件/指标）与“决策解释/加载报告”；插件白名单豁免策略。
- M2 多租户与版本编排（第 8–12 周）：上下文标准化、覆盖/回退策略、版本与灰度优先级模型；旁路运行与回滚流程。
- M3 可观测与变更安全（第 14–18 周）：采样与分级、干跑对比、告警推荐阈值；契约与回归模板；DX 文档/模板齐备。
- M4 生态与扩展（第 20–24 周）：远程插件/策略来源约定（签名/来源白名单思想），示例仓库与发布规范；生产最佳实践手册。

---

## 13. 附录：现状与蓝图的一致性
- 现状：已具备可用内核与插件 SPI 雏形；Builder 接入与基本观测点到位。
- 蓝图：不强制产品化落地，聚焦“标准与治理”底座，确保业务方仅需实现标准即可运行。

---

## 14. 短期蓝图（6周，内核优先，插件化扩展）

- 范围（不含平台化）：
  - 内核 SPI 稳态化：描述符校验、加载报告可诊断、注释与异常文案统一。
  - 插件隔离 + 能力并存：允许多个插件声明相同 capability；不再做 capability 层面的唯一约束。
  - 资源级唯一：选择器名/事件路由器名/监控处理链名等“资源名”禁止同名覆盖（在各注册点校验）。
  - 选择/路由最小闭环：以租户/应用/版本（Code/CodeVersion）为主，灰度/AB 仅接口占位。
  - 决策解释 v1 与最小官方选择器插件样板（不改变默认行为）。
  - 测试矩阵：单元/集成/并发冒烟。

- 节奏与交付：
  - 第 1–2 周（SPI 硬化 + 注册约束）：
    - 完成 PluginDescriptor 校验（id/semver/capabilities 非空）与错误文案。
    - 能力并存：移除 capability 层面的唯一/冲突判定，改为资源级唯一约束。
    - 资源级唯一：为选择器注册点落实“同名禁止覆盖”；对事件/监控等注册点制定同名约束计划。
    - 加载报告字段稳定：顺序/状态/错误/缺依赖/（保留 whitelistApplied 字段，默认不使用）。
  - 第 3–4 周（路由闭环 + 决策解释 v1）：
    - 决策解释 v1：候选快照、过滤链路、命中/未命中原因（日志/结构对象），默认调试级别输出。
    - 官方最小选择器插件样板（Code/CodeVersion）用于教学与回归。
    - 回退语义说明：code → code+tags 基础回退。
  - 第 5–6 周（质量门槛 + DX）：
    - 并发冒烟：插件启停与事件发布回滚幂等校验。
    - DX：一页纸标准 + 最小插件模板 + 自测脚手架指引。
    - 文档对齐与变更记录：Phase B 勾选同步、兼容性声明（无平台化内容）。

- 验收（短期）：
  - 无插件/有插件均可稳定构建；冲突/依赖错误可诊断；非关键失败降级可用。
  - 决策解释 v1 生效；官方样板运行通过集成测试。
  - 测试矩阵通过：解析/编排/报告相关行覆盖达标，并发冒烟稳定。

### 14.1 注册与冲突策略（短期）

- pluginId 唯一：重复 ID 直接失败。
- 能力并存：多个插件可声明相同 capability（SELECTOR/EVENT/MONITOR 等），框架以隔离与编排保证安全。
- 资源级唯一：选择器名/事件路由器名/监控处理链名等资源在注册点禁止同名覆盖（直接失败）。
---

# Flex Point V1 Execution Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 以现有蓝图为基线，按阶段落地：第0阶段（内核优化）→ 第一阶段（core 插件 SPI 化）→ 官方内置插件与“决策解释 v1”，并以 Phare 目录作为每阶段 Gate 管理与证据收敛入口。

**Architecture:** 保持现有多模块结构（common/core/spring/springboot/test/examples）。在 `FlexPointBuilder` 中集中装配事件总线与插件管理；以 `PluginManager + PluginContext` 统一插件生命周期，所有选择/事件/监控扩展均通过插件接入；测试矩阵覆盖单测、集成与并发冒烟。

**Tech Stack:** Java 8+/11+、Maven、JUnit 5、Spring Boot（示例）、AssertJ、Mockito（可选）。

---

## Phase A（第0阶段）：现有内核优化（对应 statics/Phase A Core Optimize）

- Phare 入口：`statics/Phare/Phase A Core Optimize/PHARE.md`
- 设计/背景：`statics/Phase A Core Optimize/*.md`

### Task A1: Builder 接线闭环（EventBus 装配/注入/关闭）

**Files:**
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/FlexPointBuilder.java`
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/event/DefaultEventBus.java`
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/event/EventDispatcher.java`
- Test: `flexpoint-test/src/test/java/com/flexpoint/test/PhaseZeroExecutionTest.java`

- [ ] Step 1: 为 Builder 增加实例级 `EventBus + EventDispatcher` 装配点（无全局静态）。
- [ ] Step 2: 在 `shutdown()` 中关闭当前实例总线，验证不影响其他实例。
- [ ] Step 3: 运行测试

Run: `mvn -q -Dtest=PhaseZeroExecutionTest#eventBusWiringWorks test`
Expected: PASS

- [ ] Step 4: 补充文档

Edit: `statics/Phase A Core Optimize/TECHNICAL_DESIGN.md`

- [ ] Step 5: Commit

```bash
git add flexpoint-core/src/main/java/com/flexpoint/core/event/* \
        flexpoint-core/src/main/java/com/flexpoint/core/FlexPointBuilder.java \
        statics/Phase\ A\ Core\ Optimize/TECHNICAL_DESIGN.md
git commit -m "core: instance-scoped EventBus wiring + graceful shutdown"
```

### Task B1: 注册中心并发一致性（容器语义/快照读取）

**Files:**
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/ext/DefaultExtAbilityRegistry.java`
- Test: `flexpoint-test/src/test/java/com/flexpoint/test/IntegrationTest.java`
- Test: `flexpoint-test/src/test/java/com/flexpoint/test/complex/ABRuleTest.java`

- [ ] Step 1: 将并发容器替换为并发友好结构或返回不可变快照。
- [ ] Step 2: 明确 `getAllExtAbility` 的快照一致性语义并实现。
- [ ] Step 3: 新增并发读写冒烟用例

Create: `flexpoint-test/src/test/java/com/flexpoint/test/registry/ConcurrentRegistryTest.java`

```java
@Test
void concurrent_register_and_iterate_are_consistent() { /* 竞态断言 */ }
```

- [ ] Step 4: 运行测试

Run: `mvn -q -Dtest=ConcurrentRegistryTest test`
Expected: PASS

- [ ] Step 5: Commit

```bash
git add flexpoint-core/src/main/java/com/flexpoint/core/ext/DefaultExtAbilityRegistry.java \
        flexpoint-test/src/test/java/com/flexpoint/test/registry/ConcurrentRegistryTest.java
git commit -m "core: registry snapshot consistency + concurrency smoke tests"
```

### Task C1: 扩展点计数口径统一

**Files:**
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/FlexPoint.java`
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/ext/ExtAbilityRegistry.java`
- Test: `flexpoint-test/src/test/java/com/flexpoint/test/PhaseZeroExecutionTest.java`

- [ ] Step 1: 统一通过 Registry 提供的 `getRegisteredCount()` 计算总数。
- [ ] Step 2: 运行回归测试

Run: `mvn -q -Dtest=PhaseZeroExecutionTest#metricsCountConsistent test`
Expected: PASS

- [ ] Step 3: Commit

```bash
git add flexpoint-core/src/main/java/com/flexpoint/core/FlexPoint.java
git commit -m "core: unify registered count via registry"
```

### Task D1: 调用异常语义与事件语义收敛

**Files:**
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/ext/proxy/EventPublisherInvocationHandler.java`
- Test: `flexpoint-test/src/test/java/com/flexpoint/test/IntegrationTest.java`

- [ ] Step 1: 统一反射异常透传/解包策略；区分 INVOKE_FAIL 与 INVOKE_EXCEPTION。
- [ ] Step 2: 契约测试补齐

Create: `flexpoint-test/src/test/java/com/flexpoint/test/contracts/InvocationContractTest.java`

```java
@Test
void unwrapsInvocationTargetException_and_emits_exception_event() { /* 断言 */ }
```

- [ ] Step 3: 运行测试

Run: `mvn -q -Dtest=InvocationContractTest test`
Expected: PASS

- [ ] Step 4: Commit

```bash
git add flexpoint-core/src/main/java/com/flexpoint/core/ext/proxy/EventPublisherInvocationHandler.java \
        flexpoint-test/src/test/java/com/flexpoint/test/contracts/InvocationContractTest.java
git commit -m "core: unify exception semantics + contract tests"
```

### Task E1: 事件总线鲁棒性与配置化

**Files:**
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/config/FlexPointConfig.java`
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/event/DefaultEventBus.java`
- Test: `flexpoint-test/src/test/java/com/flexpoint/test/IntegrationTest.java`

- [ ] Step 1: 将线程池参数与开关接入 `FlexPointConfig`，提供默认与覆盖策略。
- [ ] Step 2: 边界输入/关闭态测试覆盖。
- [ ] Step 3: 运行测试

Run: `mvn -q -Dtest=IntegrationTest#eventBusBoundaryRobustness test`
Expected: PASS

- [ ] Step 4: Commit

```bash
git add flexpoint-core/src/main/java/com/flexpoint/core/config/FlexPointConfig.java \
        flexpoint-core/src/main/java/com/flexpoint/core/event/DefaultEventBus.java
git commit -m "core: event bus robustness + config wiring"
```

---

## Phase B（第一阶段）：core 插件 SPI 化（对应 statics/Phase B Core Plugin SPI）

- Phare 入口：`statics/Phare/Phase B Core Plugin SPI/PHARE.md`
- 设计/任务：`statics/Phase B Core Plugin SPI/*.md`

### Task P1: SPI 模型补齐与校验增强

**Files:**
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/plugin/PluginDescriptor.java`
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/plugin/PluginState.java`
- Test: `flexpoint-test/src/test/java/com/flexpoint/test/PluginSpiExampleTest.java`

- [ ] Step 1: Descriptor 字段校验补强（空/非法 id、semver、capabilities 非空）。
- [ ] Step 2: 单测覆盖异常文案与边界。
- [ ] Step 3: 运行测试

Run: `mvn -q -Dtest=PluginSpiExampleTest test`
Expected: PASS

- [ ] Step 4: Commit

```bash
git add flexpoint-core/src/main/java/com/flexpoint/core/plugin/PluginDescriptor.java
git commit -m "plugin: descriptor validations tightened"
```

### Task P2: 依赖解析/顺序/冲突（资源级唯一）单测补齐

**Files:**
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/plugin/manage/DependencyResolver.java`
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/plugin/manage/DefaultPluginManager.java`
- Test: `flexpoint-test/src/test/java/com/flexpoint/test/PluginSpiExampleTest.java`
- Test: Create `flexpoint-test/src/test/java/com/flexpoint/test/plugin/DependencyAndConflictTest.java`

- [ ] Step 1: 为选择器/事件/监控注册点添加“资源名同名禁止覆盖”的断言用例。
- [ ] Step 2: 循环依赖/缺依赖/顺序稳定性用例补齐。
- [ ] Step 3: 运行测试

Run: `mvn -q -Dtest=DependencyAndConflictTest test`
Expected: PASS

- [ ] Step 4: Commit

```bash
git add flexpoint-test/src/test/java/com/flexpoint/test/plugin/DependencyAndConflictTest.java
git commit -m "plugin: dependency graph + resource-unique tests"
```

### Task P3: 官方最小插件（Selector/Event/Monitor）样板

**Files:**
- Create: `flexpoint-core/src/main/java/com/flexpoint/core/plugin/official/selector/CodeSelectorPlugin.java`
- Create: `flexpoint-core/src/main/java/com/flexpoint/core/plugin/official/selector/CodeVersionSelectorPlugin.java`
- Create: `flexpoint-core/src/main/java/com/flexpoint/core/plugin/official/event/EventPlugin.java`
- Create: `flexpoint-core/src/main/java/com/flexpoint/core/plugin/official/monitor/MonitorPlugin.java`
- Test: `flexpoint-test/src/test/java/com/flexpoint/test/PluginSpiExampleTest.java`

- [ ] Step 1: 在 `PluginContext` 中以受控方式注册选择器/订阅器/监控处理链。
- [ ] Step 2: Builder 装配生效回归（无插件与有插件两条路径）。
- [ ] Step 3: 运行测试

Run: `mvn -q -Dtest=PluginSpiExampleTest test`
Expected: PASS

- [ ] Step 4: Commit

```bash
git add flexpoint-core/src/main/java/com/flexpoint/core/plugin/official/**
git commit -m "plugin: official minimal selector/event/monitor plugins"
```

### Task P4: 决策解释 v1（调试级）

**Files:**
- Create: `flexpoint-core/src/main/java/com/flexpoint/core/selector/DecisionExplanation.java`
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/selector/Selector.java`
- Modify: `flexpoint-core/src/main/java/com/flexpoint/core/selector/DefaultSelectorRegistry.java`
- Test: Create `flexpoint-test/src/test/java/com/flexpoint/test/selector/DecisionExplanationTest.java`

- [ ] Step 1: 产出候选快照/过滤链路/命中与未命中原因（对象+日志），默认 Debug 级输出。
- [ ] Step 2: 新增测试覆盖解释对象结构与日志样例。
- [ ] Step 3: 运行测试

Run: `mvn -q -Dtest=DecisionExplanationTest test`
Expected: PASS

- [ ] Step 4: Commit

```bash
git add flexpoint-core/src/main/java/com/flexpoint/core/selector/* \
        flexpoint-test/src/test/java/com/flexpoint/test/selector/DecisionExplanationTest.java
git commit -m "selector: decision explanation v1 (debug)"
```

---

## 文档与 DX（持续任务）

### Task DX1: 一页纸标准与最小插件模板

**Files:**
- Create: `statics/Phase B Core Plugin SPI/PLUGIN_TEMPLATE.md`
- Create: `statics/Phase B Core Plugin SPI/ONE_PAGER.md`

- [ ] Step 1: 输出插件 SPI 一页纸与模板。
- [ ] Step 2: 链接示例与测试。

Run: `rg "PluginDescriptor|PluginLifecycle" -n`
Expected: References found

- [ ] Step 3: Commit

```bash
git add statics/Phase\ B\ Core\ Plugin\ SPI/PLUGIN_TEMPLATE.md \
        statics/Phase\ B\ Core\ Plugin\ SPI/ONE_PAGER.md
git commit -m "docs: SPI one-pager + minimal plugin template"
```

---

## 验收与 Gate 管理

- 按阶段在 `statics/Phare/<Phase>/PHARE.md` 维护 Entry/Exit Gate 与证据链接。
- 每完成一个任务包，在对应 Phase 的 PHARE 列表中勾选“证据”（测试报告、文档、提交记录）。
- 每周在根计划 `statics/FLEXPOINT_PLAN.md` 更新“进度小结”。

---

## 执行与验证命令速记

- 运行全部测试：`mvn -q -DskipITs=false test`
- 只跑某个用例：`mvn -q -Dtest=ClassName#methodName test`
- 检索关键接口：`rg "interface Plugin|class FlexPointBuilder|@FpSelector" -n`

---

更新时间：2026-03-26
