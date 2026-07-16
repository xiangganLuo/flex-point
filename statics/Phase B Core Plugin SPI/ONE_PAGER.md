# Flex Point 插件 SPI 一页纸标准（极简模型）

> 面向插件开发者的最小必读。目标：读完即可写出一个可被内核统一装配、启停与观测的插件。

---

## 1. 插件是什么

插件是内核能力的统一接入单元。选择器 / 事件订阅 / 监控处理链等扩展，都以「插件」形式实现，
由 `PluginManager` 统一注册、装配（init/start）、启停（stop/destroy）与运行期治理（enable/disable）。

插件模型极简：**只有一个标识 `getId()` + 一套生命周期**，不再承载依赖/顺序/版本/能力/关键性等概念。

```
Plugin extends PluginLifecycle
  ├── String getId()               // 全局唯一标识
  ├── void init(PluginContext ctx) // 读取配置、准备资源
  ├── void start()                 // 注册能力（selector/subscriber/handler…）
  ├── void stop()                  // 反注册能力、停止异步资源
  └── void destroy()               // 释放资源
```

> 便捷基类 `AbstractPlugin` 提供四个生命周期方法的空实现，按需覆写；`getId()` 需自行实现。

## 2. 装配与顺序

- **装配顺序 = 注册顺序**：`PluginManager` 按注册先后依次 `init → start`；关闭时逆序 `stop → destroy`。
- **无依赖/顺序声明**：若插件间存在先后要求，由接入方控制注册顺序。
- **pluginId 唯一**：重复 ID 在注册期直接失败。

## 3. 生命周期与状态机

```
CREATED ──init──▶ INITIALIZED ──start──▶ STARTED ──stop──▶ STOPPED ──destroy──▶ DESTROYED
                                   │
                                   └── 任一阶段抛异常 ──▶ FAILED（统一降级）
```

- **统一降级**：任何插件启动失败都不会中断构建——标记 `FAILED`、记入加载报告、继续装配其它插件。
- **运行期启停**：`enable(id)` / `disable(id)`（`FlexPoint.enablePlugin/disablePlugin`）。
  disable 仅对 STARTED 生效（stop 后置 STOPPED，可再 enable）。

## 4. 上下文（`PluginContext`）—— 唯一受控入口

插件**不得**持有 `FlexPoint` 全局可变状态；一切通过 `init(ctx)` 拿到的上下文访问：

```java
ctx.extRegistry()      // 扩展点注册中心
ctx.selectorRegistry() // 选择器注册表
ctx.eventBus()         // 实例级事件总线
ctx.monitor()          // 监控器
ctx.config()           // 框架配置
```

## 5. 资源级唯一

- 选择器名等「资源名」在各自注册点**禁止同名覆盖**（重复注册直接失败）。
- 因此两个插件若都注册同名选择器，后者会在 start 抛错 → 被降级为 `FAILED`（不影响其它插件）。

## 6. 装配方式

```java
// 显式装配（纯内核 + 指定插件），装配顺序即传入顺序
FlexPoint fp = FlexPointBuilder.create()
        .withPlugin(new BasePlugin())
        .withPlugin(new MySelectorPlugin())
        .build();

// 不传插件 → 构建纯内核实例，装配交由接入层（如 Spring Boot 自动配置）
FlexPoint core = FlexPointBuilder.create().build();
```

## 7. 官方插件（独立模块）

官方插件已从 core 拆分为独立模块，位于 `flexpoint-plugin-all` 下，**每个插件一个子模块**：

| 模块 | pluginId | 作用 |
|------|----------|------|
| `flexpoint-plugin-selector-code` | `core.selector.code` | 注册 Code 选择器 |
| `flexpoint-plugin-selector-code-version` | `core.selector.code-version` | 注册 Code+Version 选择器（依赖 code 模块） |
| `flexpoint-plugin-observability` | `core.observability` | 事件订阅 + 监控处理链（指标/告警/采集） |

按需在业务工程引入对应模块依赖即可。

## 8. 可观测

- `FlexPoint.getPluginLoadReport()`：装配顺序 / 各插件状态 / 失败原因。
- `FlexPoint.getPluginStates()`：当前状态快照。

## 9. Checklist（提交前自检）

- [ ] `getId()` 返回全局唯一、稳定的标识
- [ ] `start()` 注册的资源在 `stop()` 中对称反注册
- [ ] `destroy()` 释放全部持有引用（置空/关闭线程池等）
- [ ] 不持有 `FlexPoint` 全局可变状态，只用 `PluginContext`
- [ ] 提供最小单测：装配生效 + 启停对称

> 模板见 [PLUGIN_TEMPLATE.md](./PLUGIN_TEMPLATE.md)。
