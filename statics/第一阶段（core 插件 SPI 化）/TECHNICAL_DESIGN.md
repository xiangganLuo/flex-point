# 第一阶段技术方案：core 插件 SPI 化

> 阶段目标：在不推翻现有 `flexpoint-core` 的前提下，建立统一插件 SPI、生命周期与装配机制，完成从“可替换组件”到“可治理插件系统”的升级。

---

## 1. 背景与问题定义

当前 core 已有 registry/selector/event/monitor 等可扩展骨架，但扩展入口分散在不同抽象中，缺少统一的插件定义与治理语义，导致：
- 扩展能力可接入但不可统一管理（启停、依赖、冲突、顺序）；
- 生命周期行为缺失统一规范；
- 未来 selector/event/monitor 的生态扩展难以形成一致标准。

因此第一阶段聚焦 **插件协议 + 插件管理器 + 插件装配入口** 三件事。

---

## 2. 目标与非目标

### 2.1 目标（In Scope）
1. 建立统一插件 SPI 抽象：`Plugin` / `PluginDescriptor` / `PluginLifecycle`。
2. 实现插件管理器：注册、解析依赖、排序、安装、启停、卸载、状态观测。
3. 在 `FlexPointBuilder` 中增加插件装配入口。
4. 适配现有内置能力为官方插件（最小集：selector/event/monitor）。
5. 提供冲突检测与失败降级策略（启动期快速失败 + 运行期降级）。

### 2.2 非目标（Out of Scope）
1. 不在此阶段做治理平台 UI。
2. 不在此阶段做多语言 SDK。
3. 不做复杂热更新与远程插件仓库。

---

## 3. 总体架构

```text
FlexPointBuilder
   └── PluginManager
         ├── PluginRegistry（插件元数据）
         ├── DependencyResolver（依赖/顺序/冲突）
         ├── LifecycleCoordinator（init/start/stop/destroy）
         └── PluginContext（对内核能力的受控访问）

官方内置插件：
- SelectorPlugin（封装 selector 注册）
- EventPlugin（封装 EventBus 订阅与路由扩展）
- MonitorPlugin（封装 monitor handler/collector/alert 扩展）
```

### 3.1 核心原则
- **最小侵入**：尽量复用现有 `FlexPointBuilder` 和 core 抽象。
- **显式元数据**：插件能力、版本、依赖、顺序必须可声明。
- **确定性装配**：同一插件集合在同一配置下，装配顺序稳定可重复。
- **安全失败**：插件异常不应直接导致整个内核不可用（除关键插件）。

---

## 4. 关键模型设计

## 4.1 PluginDescriptor（元数据）
建议字段：
- `pluginId`：唯一标识（如 `core.selector.code-version`）
- `version`：语义化版本
- `apiVersion`：兼容的内核插件 API 版本
- `order`：装配顺序（默认 0）
- `dependencies`：依赖插件列表（可含版本约束）
- `capabilities`：能力声明（selector/event/monitor/...）
- `critical`：关键插件标记（关键插件失败可阻断启动）

## 4.2 PluginLifecycle（生命周期）
统一阶段：
1. `init(PluginContext)`：读取配置、校验依赖、准备资源
2. `start()`：注册能力（selector/subscriber/handler 等）
3. `stop()`：反注册能力，停止异步资源
4. `destroy()`：释放资源

状态机：`CREATED -> INITIALIZED -> STARTED -> STOPPED -> DESTROYED`

## 4.3 PluginContext（上下文）
受控暴露：
- `ExtAbilityRegistry`
- `SelectorRegistry`
- `EventBus`
- `ExtMonitor`
- `FlexPointConfig`
- 日志与诊断接口

> 约束：插件不直接持有 `FlexPoint` 全局可变状态引用。

---

## 5. 插件管理器设计

## 5.1 核心职责
- `register(plugin)`：登记插件实例与描述信息。
- `resolve()`：依赖图构建、环检测、顺序计算、冲突检测。
- `installAll()`：按顺序 `init/start`。
- `stopAll()`：逆序 `stop/destroy`。
- `enable/disable(pluginId)`：运行时启停（可选，第一阶段建议实现最小可用）。

## 5.2 依赖与冲突策略
- 拓扑排序解决依赖装配顺序。
- 检测循环依赖并启动失败。
- `pluginId` 冲突直接失败。
- capability 冲突默认“单能力单主插件”，允许通过配置白名单放宽。

## 5.3 异常与降级策略
- `critical=true` 插件启动失败：阻断 `FlexPoint` 构建。
- 非关键插件失败：记录错误 + 标记 `FAILED` + 框架继续启动。
- 运行期异常：插件隔离，不传播到主调用链（除明确配置）。

---

## 6. 与现有 core 的集成方案

## 6.1 FlexPointBuilder 增强
新增能力：
- `withPlugin(Plugin plugin)`
- `withPlugins(List<Plugin> plugins)`
- `withPluginConfig(...)`（可选）

构建流程建议：
1. 创建 registry/monitor/selectorRegistry（现有流程）；
2. 创建 PluginContext；
3. PluginManager `register -> resolve -> installAll`；
4. 返回 `FlexPoint`。

## 6.2 官方内置插件最小改造范围
- `CodeSelector` / `CodeVersionSelector` 注册迁移为 Selector 插件。
- 事件订阅器（如 monitor subscriber）迁移为 Event 插件装配。
- Monitor handler 链通过 Monitor 插件贡献。

## 6.3 兼容策略
- 保留现有直接注册 API（`registerSelector` 等），但标记为“兼容入口”。
- 新能力优先通过插件入口接入。

---

## 7. 可观测与诊断

第一阶段新增最小诊断输出：
- 插件加载顺序
- 插件状态转移日志
- 依赖解析结果
- 失败原因（依赖缺失/冲突/异常栈）

建议提供：
- `PluginManager#getPluginStates()`
- `PluginManager#getLoadReport()`

---

## 8. 测试策略

### 8.1 单元测试
- descriptor 字段校验
- 依赖解析与拓扑排序
- 冲突检测
- 生命周期状态机

### 8.2 集成测试
- 与 `FlexPointBuilder` 集成装配
- 官方插件加载与能力生效
- 非关键插件失败降级验证

### 8.3 并发/稳定性测试
- 并发启停安全性
- 启动失败回滚（部分已启动插件清理）

---

## 9. 交付物清单

1. 插件 SPI 设计文档（本文件）
2. 插件管理器实现
3. Builder 插件装配入口
4. 官方内置插件最小改造
5. 测试与示例（在 `flexpoint-test` / `flexpoint-examples`）

---

## 10. 风险与缓解

- 风险1：改造范围外溢到平台能力
  - 缓解：严格限制第一阶段范围，只做 core 插件基础设施。
- 风险2：兼容性破坏
  - 缓解：保留原入口 + 提供兼容层 + 回归测试。
- 风险3：插件顺序不确定导致行为漂移
  - 缓解：显式 order + 依赖图 + 加载报告。

---

## 11. 验收标准

- 任意新选择器能力可通过插件接入，无需修改 `FlexPoint` 主流程。
- 插件依赖冲突可在启动期识别并给出可诊断错误。
- 至少 1 个非关键插件失败时框架仍能启动并运行核心能力。
- 提供完整测试覆盖（单测 + 集成 + 并发关键路径）。
