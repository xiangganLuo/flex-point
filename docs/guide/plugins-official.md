# 官方插件模块

官方插件已从 core 拆分为独立模块，位于聚合模块 `flexpoint-plugin-all` 下，**每个插件一个子模块**。按需在业务工程引入对应依赖即可。

## 插件全景

| 分类 | 模块 | 作用 |
|------|------|------|
| 选择器 | `flexpoint-plugin-selector-code` | 按 `code` 路由 |
| 选择器 | `flexpoint-plugin-selector-code-version` | 按 `code` + `version` 路由 |
| 选择器 | `flexpoint-plugin-selector-tag` | 按标签路由 |
| 选择器 | `flexpoint-plugin-selector-gray` | 灰度路由 |
| 选择器 | `flexpoint-plugin-selector-ab` | A/B 实验路由 |
| 选择器 | `flexpoint-plugin-selector-weight` | 按权重路由 |
| 选择器 | `flexpoint-plugin-selector-tenant` | 按租户路由 |
| 选择器 | `flexpoint-plugin-selector-cache` | 选择结果缓存 |
| 观测/治理 | `flexpoint-plugin-observability` | 事件订阅 + 监控处理链（指标 / 告警 / 采集） |
| 观测/治理 | `flexpoint-plugin-audit` | 调用审计日志 |
| 观测/治理 | `flexpoint-plugin-slowcall` | 慢调用检测 |
| 观测/治理 | `flexpoint-plugin-metrics` | 指标汇总 |
| 行为增强 | `flexpoint-plugin-retry` | 调用重试 |
| 行为增强 | `flexpoint-plugin-resilience` | 超时 / 熔断 |

所有插件遵循统一装配模型：声明为 `@Bean` 即被自动收集装配（见 [插件体系](/guide/plugin)）。下面详述最常用的三个。

## Code 选择器

`flexpoint-plugin-selector-code`（pluginId `core.selector.code`）提供按 `code` 路由的选择器 `codeSelector`。业务方实现 `CodeResolver` 提供「如何从上下文解析 code」：

```xml
<dependency>
    <groupId>com.flexpoint</groupId>
    <artifactId>flexpoint-plugin-selector-code</artifactId>
    <version>2.0.0</version>
</dependency>
```

```java
@Bean
public CodeSelectorPlugin codeSelectorPlugin() {
    return new CodeSelectorPlugin(() -> FlexPointContext.current().getAppCode());
}
```

`CodeSelector` 继承 `AbstractSelector`：按 `resolveCode()` 解析出的 code 过滤候选（`ext.getCode()` 相等者通过），再由模板方法归结为 HIT / MISS / AMBIGUOUS。

扩展点接口通过 `@FpSelector("codeSelector")` 使用它。

## Code + Version 选择器

`flexpoint-plugin-selector-code-version`（pluginId `core.selector.code-version`）在 code 基础上再按 `version` 标签过滤，选择器名 `codeVersionSelector`：

```xml
<dependency>
    <groupId>com.flexpoint</groupId>
    <artifactId>flexpoint-plugin-selector-code-version</artifactId>
    <version>2.0.0</version>
</dependency>
```

```java
@Bean
public CodeVersionSelectorPlugin codeVersionSelectorPlugin() {
    return new CodeVersionSelectorPlugin(new CodeVersionSelector.CodeVersionResolver() {
        @Override public String resolveCode() { return FlexPointContext.current().getAppCode(); }
        @Override public String resolveVersion() { return FlexPointContext.current().getVersion(); }
    });
}
```

- 先按 code 过滤，再按 `version` 标签（`ExtTags` 中的 `version`）过滤。
- `resolveVersion()` 可选，默认 `1.0.0`；扩展点实现未声明 `version` 标签时同样按默认值参与匹配。

::: info 依赖关系
该模块依赖 `flexpoint-plugin-selector-code`（`CodeVersionSelector extends CodeSelector`），引入后无需再单独引入 code 模块。
:::

## 可观测插件

`flexpoint-plugin-observability`（pluginId `core.observability`）融合「事件订阅 + 监控处理链」，是启用调用监控的推荐方式：

```xml
<dependency>
    <groupId>com.flexpoint</groupId>
    <artifactId>flexpoint-plugin-observability</artifactId>
    <version>2.0.0</version>
</dependency>
```

```java
// 无参：仅启用默认指标处理链
@Bean
public ObservabilityPlugin observabilityPlugin() {
    return new ObservabilityPlugin();
}

// 带参：注入自定义告警策略与指标采集器
@Bean
public ObservabilityPlugin observabilityPlugin(List<AlertStrategy> alerts,
                                               List<MetricsCollector> collectors) {
    return new ObservabilityPlugin(alerts, collectors);
}
```

插件行为：

- **`init`**：从 `PluginContext` 取得 `ExtMonitor` 与 `EventBus`，准备处理器。
- **`start`**：向 `ExtMonitor` 注入处理链（`MetricsHandler` → 可选 `CollectorHandler` → `AlertHandler`），并向事件总线订阅 `MonitorEventSubscriber`，将调用与异常事件转发给监控器。
- **`stop`**：反订阅事件总线并移除处理链（与 start 对称）。

指标读取与监控配置见 [监控与可观测](/guide/monitor)。

## 装配顺序建议

官方插件之间无硬性依赖，但注册顺序即装配顺序。一般建议：先注册选择器插件，再注册可观测插件。Spring Boot 环境下把它们都声明为 `@Bean` 即可被自动收集装配，见 [Spring Boot 接入](/guide/springboot)。
