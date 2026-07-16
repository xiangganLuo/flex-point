# 插件体系重构说明（2026-07-16）

> 主题：精简插件核心概念 + 官方插件独立成模块 + 扩展点调用代理重构。

## 1. 精简插件核心

移除以下"治理概念"，插件模型收敛为「`getId()` + 生命周期」：
- `dependencies`（依赖）、`order`（顺序）、`version`/`apiVersion`（版本）、`critical`（关键性）、`capabilities`（能力域）。

变更：
- `Plugin` 接口：移除 `getDescriptor()`，改为 `String getId()`。
- 删除：`PluginDescriptor`、`PluginDescriptorValidator`、`PluginCapability`、`PluginDependency`、
  `manage/DependencyResolver`、`exception/PluginDependencyException`、`exception/PluginConflictException`。
- `PluginManager`：移除 `resolve()`；**装配顺序 = 注册顺序**。
- `DefaultPluginManager`：`LinkedHashMap` 保序；**任何插件失败统一降级**（FAILED + 报告 + 继续），删除 critical 分支与 rollback。
- `PluginLoadReport`：移除 `missingDependencies`。
- 保留：`PluginState` 状态机、`enable/disable` 运行期启停、`FlexPoint` 生命周期接管与加载报告、资源级唯一（选择器同名禁止覆盖）。

## 2. 官方插件独立成模块

新增聚合模块 `flexpoint-plugin-all`，**每个插件一个子模块**：

| 模块 | 内容 | 包 |
|------|------|----|
| `flexpoint-plugin-selector-code` | `CodeSelector` + `CodeSelectorPlugin` | `com.flexpoint.plugin.selector.code` |
| `flexpoint-plugin-selector-code-version` | `CodeVersionSelector` + `CodeVersionSelectorPlugin`（依赖 code 模块） | `com.flexpoint.plugin.selector.codeversion` |
| `flexpoint-plugin-observability` | `ObservabilityPlugin` + handlers + alert + enums + `MetricsCollector` + `MonitorEventSubscriber` | `com.flexpoint.plugin.observability` |

- 选择器实现（`CodeSelector`/`CodeVersionSelector`）与 `MetricsCollector` 一并从 core 迁出。
- 删除冗余的 `EventPlugin`、`MonitorPlugin`、`AuditEventSubscriber`、`OfficialPlugins`（能力由 `ObservabilityPlugin` 统一提供）。
- 根 `pom` 与 `flexpoint-dependencies-bom` 已登记新模块。

### 迁移指引（接入方）
- 引用 `com.flexpoint.core.plugin.official.selector.resolves.CodeVersionSelector`
  → 改为 `com.flexpoint.plugin.selector.codeversion.CodeVersionSelector`，并引入 `flexpoint-plugin-selector-code-version` 依赖。
- 观测相关类（`ObservabilityPlugin`/`AlertStrategy`/`MetricsCollector` 等）
  → 包名迁至 `com.flexpoint.plugin.observability.*`，引入 `flexpoint-plugin-observability` 依赖。
- 自定义插件：`getDescriptor()` → 实现 `getId()`；去除 descriptor/capability/order/critical/dependencies。

## 3. 扩展点调用代理重构

`flexpoint-core/.../ext/proxy/EventPublisherInvocationHandler`：
- **短路 `Object` 方法**（toString/hashCode/equals）：直接转发，不再产生埋点噪声事件。
- 收敛计时与异常处理为清晰结构；保留 `INVOKE_BEFORE/SUCCESS/FAIL/EXCEPTION` 语义
  （FAIL=业务异常解包 ITE，EXCEPTION=框架/反射错误）。
- 保留 `setAccessible(true)`：扩展点实现类可能为非 public 类，反射调用 public 接口方法需放开可访问性
  （否则抛 IllegalAccessException 并被代理包装为 UndeclaredThrowableException）。

## 4. 验证
- 全反应堆 `mvn clean test` 通过（48 项）。
- `flexpoint-examples/java-example` 的 `PluginExampleMain` 运行验证：装配顺序=注册顺序、失败降级、路由与运行期启停正常。
