# Flex Point 插件 SPI 一页纸标准

> 面向插件开发者的最小必读。目标：读完即可写出一个可被内核统一装配、治理与观测的插件。

---

## 1. 插件是什么

插件是内核能力的统一接入单元。所有选择器 / 事件订阅 / 监控处理链等扩展，都以「插件」形式声明元数据、实现生命周期，由 `PluginManager` 统一注册、解析依赖、排序、装配与启停。

一个插件 = **一份描述符（`PluginDescriptor`）** + **一套生命周期（`PluginLifecycle`）**。

```
Plugin extends PluginLifecycle
  ├── PluginDescriptor getDescriptor()   // 元数据：id/版本/依赖/顺序/能力/关键性
  ├── void init(PluginContext ctx)       // 读取配置、校验依赖、准备资源
  ├── void start()                       // 注册能力（selector/subscriber/handler…）
  ├── void stop()                        // 反注册能力、停止异步资源
  └── void destroy()                     // 释放资源
```

## 2. 描述符（`PluginDescriptor`）字段

| 字段 | 必填 | 说明 |
|------|------|------|
| `pluginId` | 是 | 全局唯一标识，字符集 `[A-Za-z0-9_.:-]`，建议 `域.能力.名`，如 `core.selector.code` |
| `version` | 是 | 语义化版本 `MAJOR.MINOR.PATCH`，可带 `-alpha` / `+build` 后缀 |
| `apiVersion` | 否 | 兼容的内核插件 API 版本，默认 `1.0` |
| `order` | 否 | 装配顺序权重，越小越先；**仅作为无依赖约束节点之间的排序** |
| `dependencies` | 否 | 依赖的插件 ID 列表（`PluginDependency`），影响装配先后 |
| `capabilities` | 否 | 能力声明（SELECTOR/EVENT/MONITOR/OTHER），当前用于治理与可读性 |
| `critical` | 否 | 关键插件标记；关键插件启动失败会**阻断**整个构建 |

构造时即做基础校验（空/非法 id、非法 semver）。非法描述符无法进入后续流程。

## 3. 生命周期与状态机

```
CREATED ──init──▶ INITIALIZED ──start──▶ STARTED ──stop──▶ STOPPED ──destroy──▶ DESTROYED
                                   │
                                   └── 任一阶段抛异常 ──▶ FAILED（非关键降级）
```

- **安装正序**：按依赖 + `order` 解析出的顺序执行 `init → start`。
- **停止逆序**：`stop → destroy` 按安装顺序逆序执行。
- **失败语义**：
  - `critical=true` 失败 → 回滚已启动插件并**中断构建**（抛 `PluginException`）。
  - `critical=false` 失败 → 标记 `FAILED`、记入加载报告、**继续启动**其他插件。

## 4. 上下文（`PluginContext`）—— 唯一受控入口

插件**不得**持有 `FlexPoint` 全局可变状态；一切通过 `init(ctx)` 拿到的上下文访问：

```java
ctx.extRegistry()      // 扩展点注册中心
ctx.selectorRegistry() // 选择器注册表
ctx.eventBus()         // 实例级事件总线
ctx.monitor()          // 监控器
ctx.config()           // 框架配置
```

## 5. 依赖、顺序与冲突规则

- **pluginId 唯一**：重复 ID 直接失败。
- **依赖解析**：拓扑排序（Kahn），缺失依赖 / 循环依赖在解析期抛 `PluginDependencyException`。
- **顺序确定性**：拓扑约束内按 `order` 升序，`order` 相同按 `pluginId` 字典序兜底；同一插件集合在同一配置下装配顺序稳定可复现。
- **能力并存**：允许多个插件声明相同 capability（不做能力域「单主/冲突」判定）。
- **资源级唯一**：选择器名等「资源名」在各自注册点**禁止同名覆盖**（重复注册直接失败）。

## 6. 装配方式

```java
// 显式装配（纯内核 + 指定插件）
FlexPoint fp = FlexPointBuilder.create()
        .withPlugin(new MyPlugin())
        .withPlugins(OfficialPlugins.recommended(() -> currentCode()))
        .build();

// 不传插件 → 构建纯内核实例，装配交由接入层（如 Spring Boot 自动配置）
FlexPoint core = FlexPointBuilder.create().build();
```

## 7. 官方内置插件（参考实现）

| 插件 | pluginId | 作用 |
|------|----------|------|
| `CodeSelectorPlugin` | `core.selector.code` | 注册 Code 选择器 |
| `CodeVersionSelectorPlugin` | `core.selector.code-version` | 注册 Code+Version 选择器 |
| `ObservabilityPlugin` | `core.observability` | 事件订阅 + 监控处理链（指标/告警）融合 |

`OfficialPlugins` 提供 `recommended(...)` / `minimalWithCodeSelector(...)` 等工厂方法快速组合。

## 8. Checklist（提交前自检）

- [ ] `pluginId` 全局唯一、符合命名规范
- [ ] `version` 为合法 semver
- [ ] `start()` 注册的资源在 `stop()` 中对称反注册
- [ ] `destroy()` 释放全部持有引用（置空/关闭线程池等）
- [ ] 不持有 `FlexPoint` 全局可变状态，只用 `PluginContext`
- [ ] 是否 `critical` 已明确评估（默认 `false`）
- [ ] 提供最小单测：装配生效 + 启停对称

> 模板见 [PLUGIN_TEMPLATE.md](./PLUGIN_TEMPLATE.md)。
