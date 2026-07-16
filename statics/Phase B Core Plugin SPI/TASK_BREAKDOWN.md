# 第一阶段详细子任务拆解（core 插件 SPI 化）

> 说明：本任务拆解用于研发执行与里程碑跟踪，按“可交付、可验证、可回滚”原则编排。

---

## 0. 里程碑与节奏

- M1（第 1 周）：插件模型与管理器骨架
- M2（第 2 周）：Builder 集成与官方内置插件改造
- M3（第 3 周）：测试补齐、文档与示例收敛

---

## 短期蓝图（6周）

### 第 1–2 周：SPI 硬化 + 冲突策略
- [x] PluginDescriptor 校验器：空/非法 id、非法 semver、能力集合非空（已接入构造时校验）
- [ ] 校验异常文案与示例完善（文档同步）
- [x] 能力并存：移除 capability 层面的唯一性/冲突判定
- [x] 资源级唯一（选择器）：同名禁止覆盖；事件/监控注册点约束规划
- [x] 单测：资源名冲突失败用例（DependencyAndConflictTest#duplicate_selector_name_marks_second_plugin_failed）

### 第 3–4 周：路由闭环 + 决策解释 v1
- [x] 决策解释 v1：候选快照/过滤链路/命中与未命中原因（调试级）（DecisionExplanation + Selector.explain + FlexPoint Debug 输出）
- [x] 官方最小选择器插件样板（Code/CodeVersion），持续集成运行
- [ ] 回退语义说明（code → code+tags）

### 第 5–6 周：质量门槛 + DX
- [x] 并发冒烟：插件启停回滚幂等、事件发布不丢失（ConcurrentRegistryTest + PluginRuntimeToggleTest + ConcurrentPluginEventTest）
- [x] 一页纸标准 + 最小插件模板 + 自测脚手架说明（ONE_PAGER.md / PLUGIN_TEMPLATE.md）
- [x] 文档对齐（FLEXPOINT_PLAN/Phase B 勾选同步）与变更记录（CORE_REVIEW_2026-07-16.md）

### 验收（短期）
- [ ] 构建期冲突/依赖可诊断；非关键失败降级可用
- [ ] 决策解释 v1 生效且可观测
- [ ] 集成测试通过：装配/解释/降级；并发冒烟无泄漏

## 1. 任务包 A：插件模型（P0）

### A1. 定义接口与元数据模型
- [x] 新增 `Plugin` 接口
- [x] 新增 `PluginDescriptor` 模型
- [x] 新增 `PluginLifecycle` 生命周期接口
- [x] 新增 `PluginState` 枚举与状态流转约束

**验收**
- [x] 模型具备完整字段与 JavaDoc
- [x] descriptor 校验器可识别空 id、非法版本、重复能力声明

### A2. 定义插件上下文
- [x] 新增 `PluginContext`（受控注入 registry/selector/event/monitor/config）
- [x] 禁止插件直接操作非公开全局状态

**验收**
- [x] 插件可通过 context 注册自身能力
- [x] 上下文能力边界有单测验证（PluginContextBoundaryTest）

---

## 2. 任务包 B：插件管理器（P0）

### B1. 注册与索引
- [x] 新增 `PluginManager`
- [x] 实现 `register/registerAll/getPlugin` 接口
- [x] 实现 `pluginId` 唯一性约束

### B2. 依赖解析
- [x] 新增 `DependencyResolver`
- [x] 实现拓扑排序
- [x] 实现循环依赖检测
- [x] 实现缺失依赖检测

### B3. 冲突检测
- [x] 新增 `ConflictDetector`
- [ ] 实现 capability 冲突规则（已移除，按新策略不再判定能力域冲突）
  
> 本期变更：移除能力域“单主/冲突”判定；仅保留资源级唯一校验。白名单保留为未来“资源级特殊放行”预留，不在本期启用。

### B4. 生命周期编排
- [x] 实现 `init/start/stop/destroy` 顺序
- [x] 实现失败回滚与逆序停止
- [x] 实现关键插件与非关键插件差异化处理

### B5. core 对齐优化（来自评审，P0）
- [x] Builder 统一完成 EventBus 创建、注入与关闭管理
- [x] Registry 并发一致性优化（容器/快照语义收敛）
- [x] 统一扩展点计数口径（避免 `ExtAbility.class` 路径偏差）
- [x] EventBus 可配置化（线程池+拒绝策略；大小写不敏感回退）
- [x] 清理未使用枚举与静态实现（MonitorType/MonitorEventType），保留 SPI

**验收**
- [x] 依赖与顺序可复现
- [x] 非关键插件失败不阻断启动
- [x] 关键插件失败阻断构建并给出清晰错误

---

## 3. 任务包 C：Builder 集成（P0）

### C1. FlexPointBuilder 扩展
- [x] 增加 `withPlugin(...)`
- [x] 增加 `withPlugins(...)`
- [x] 构建流程接入 PluginManager（resolve + install）

### C2. 启动报告
- [x] 增加插件加载报告对象（顺序、状态、失败原因）
- [x] 在日志输出中打印插件启动摘要

**验收**
- [x] 不传插件时构建纯内核实例（插件装配交由接入层）
- [x] 传插件时能稳定完成装配与生命周期调用

---

## 4. 任务包 D：官方内置插件改造（P1）

### D1. Selector 插件化（P1）
- [x] 将 `CodeSelector` 装配迁移到官方 Selector 插件（包迁移至插件域）
- [x] 将 `CodeVersionSelector` 装配迁移到官方 Selector 插件（包迁移至插件域）

### D2. Event 插件化（P1）
- [x] 将默认事件订阅能力迁移为插件（`MonitorEventSubscriber` 迁至插件域，`EventPlugin` 示例 + `ObservabilityPlugin` 融合）
- [x] 保留 `EventBus` 原有行为与兼容入口（实例级 `EventDispatcher`）

### D3. Monitor 插件化（P1）
- [x] 将 monitor handler/collector 扩展迁移为插件域（`PluginMetricsHandler`/`PluginAlertHandler`/`PluginCollectorHandler`）
- [x] 保留 `MonitorFactory` 的兼容创建路径

**验收**
- [x] 官方选择器/事件/观测插件可独立启停（事件与监控在 ObservabilityPlugin 中可融合）
- [x] 停用某一插件后其他能力可正常运行（可降级）

---

## 5. 任务包 E：测试与质量门槛（P0）

### E1. 单元测试
- [x] 插件模型校验测试（PluginDescriptorValidationTest）
- [x] 依赖解析测试（DependencyAndConflictTest：顺序/缺失/循环/重复）
- [x] 冲突检测测试（资源级唯一：DependencyAndConflictTest）
- [x] 生命周期状态机测试（PluginLifecycleTest + FlexPointPluginLifecycleTest）
  
> 本期补充：
- [ ] 冲突白名单命中/未命中/错误配置用例（P0，白名单本期未启用，暂缓）

### E2. 集成测试
- [x] Builder + PluginManager 集成测试（flexpoint-test/PluginSpiExampleTest）
- [x] 官方插件装配生效测试（OfficialPluginAssemblyTest：ObservabilityPlugin 指标累计 + SelectorPlugin 注册 + 降级）
- [x] 启动失败回滚测试（非关键失败降级验证）
- [x] EventBus 接线有效性测试（沿用 PhaseZeroExecutionTest）
- [x] 扩展点计数口径一致性测试（沿用 PhaseZeroExecutionTest）

### E3. 并发测试
- [x] 并发启停一致性测试（enable/disable 已 synchronized；PluginRuntimeToggleTest 覆盖幂等）
- [x] 并发发布事件下插件稳定性测试（ConcurrentPluginEventTest：8 线程并发调用，指标原子累计正确）

### E4. 回归与兼容
- [ ] 旧用法（无插件）回归
- [ ] 新旧装配方式混用回归

**验收**
- [ ] core 相关测试全部通过
- [ ] 无新增 P0/P1 级别缺陷

---

## 6+. Spring Boot 接入对齐（P1）

### SB1. 观测自动装配
- [x] 新增 `FlexPointObservabilityAutoConfiguration`，`flexpoint.observability.enabled=true` 时自动注册 `ObservabilityPlugin` 并注入 `AlertStrategy`/`MetricsCollector`
- [x] `FlexPointCoreAutoConfiguration` 支持收集容器中的 `Plugin` Bean 并统一装配

### SB2. 旧装配剥离与兼容
- [x] 删除 `FlexPointMonitorAutoConfiguration`，移除默认 Handler Bean；事件侧移除默认订阅副作用
- [ ] 文档声明：接入层默认观测行为变更为“通过属性开关启用 ObservabilityPlugin”

---

## 6. 任务包 F：文档与示例（P1）

### F1. 文档
- [ ] SPI 接口说明
- [ ] 生命周期与状态机说明
- [ ] 冲突与故障排查手册

### F2. 示例
- [ ] `flexpoint-examples` 增加“自定义插件”示例
- [ ] 覆盖插件依赖、顺序、降级场景

**验收**
- [ ] 文档可支持第三方开发一个最小插件
- [ ] 示例可直接运行并验证加载结果

---

## 7. 风险看板（执行期持续维护）

- [ ] 设计风险：SPI 过度抽象导致学习成本上升
- [ ] 进度风险：官方插件改造范围膨胀
- [ ] 质量风险：并发与生命周期边界问题
- [ ] 兼容风险：旧入口行为变化

---

## 8. Definition of Done（阶段完成标准）

- [x] 插件 SPI 与管理器能力合入主干
- [x] Builder 支持插件装配（无插件构建纯内核）；接入层负责默认装配
- [x] 官方 Selector/Event/Monitor（Observability）插件化最小闭环完成
- [ ] 测试矩阵通过并输出阶段总结文档
