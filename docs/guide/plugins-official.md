# 官方插件模块

官方插件位于聚合模块 `flexpoint-plugin-all` 下，**每个插件一个子模块**（`flexpoint-plugin-*`）。按需引入对应依赖即可。

## 两种接入方式

1. **属性装配（推荐，9 个插件）** —— 引入插件模块依赖 + 在 `application.yml` 设置 `flexpoint.plugins.<name>.enabled=true`，Spring Boot 自动创建并装配该插件 Bean，无需手写代码。适用于：`weight`、`audit`、`slowcall`、`metrics`、`retry`、`resilience`、`observability`、`code`、`code-version`。默认均为 `enabled=false`，需显式开启。

   其中三个插件在 `enabled=true` 之外仍有**前置条件**，不满足则跳过装配（不会因为开了开关就一定生效）：
   - `code` —— 还需容器中提供一个 `CodeSelector.CodeResolver` Bean（`@ConditionalOnBean`）；
   - `code-version` —— 还需 `CodeVersionSelector.CodeVersionResolver` Bean；注意属性前缀是 kebab 的 `flexpoint.plugins.code-version`；
   - `observability` —— 会经 `ObjectProvider` 收集容器中的 `AlertStrategy` / `MetricsCollector`（都可无，无自定义时仅默认指标处理链）。
2. **显式 `@Bean` 装配（`tag` / `gray` / `ab` / `tenant` / `cache`）** —— 这些选择器需要业务方提供运行期数据（`tag`/`gray`/`ab`/`tenant` 需各自的 Resolver，`cache` 需被包装的 `delegate`），**无法零编码**，故**不纳入属性装配**，请自行声明对应 `*SelectorPlugin` 的 `@Bean`（构造时传入你的 Resolver / delegate；或在非 Spring 环境用 `FlexPointBuilder.withPlugin(...)`）。任何你声明的 `Plugin` Bean 都会被自动收集装配。这四个选择器的 `flexpoint.plugins.<name>.*` 属性开关已不存在。

::: tip 装配机制
Spring Boot 下 `FlexPoint` 由自动配置构建，会收集容器中**所有** `Plugin` 类型 Bean 并按收集顺序装配（见 [Spring Boot 接入](/guide/springboot#插件装配)）。属性装配的 9 个插件由 `flexpoint.plugins.<name>.enabled=true` + classpath 上存在对应类（`@ConditionalOnClass`）触发；你自己声明的同类型 `@Bean` 会覆盖默认（`@ConditionalOnMissingBean`）。
:::

## 插件全景

| 分类 | 模块 | pluginId | 选择器名 / 关键类 | 装配方式 |
|------|------|----------|-------------------|----------|
| 选择器 | `flexpoint-plugin-selector-code` | `core.selector.code` | `codeSelector` | `flexpoint.plugins.code`（需 `CodeResolver` Bean） |
| 选择器 | `flexpoint-plugin-selector-code-version` | `core.selector.code-version` | `codeVersionSelector` | `flexpoint.plugins.code-version`（需 `CodeVersionResolver` Bean） |
| 选择器 | `flexpoint-plugin-selector-tag` | `selector.tag` | `tagSelector` | 显式 `@Bean`（需实现 `LabelResolver`） |
| 选择器 | `flexpoint-plugin-selector-gray` | `selector.gray` | `graySelector` | 显式 `@Bean`（需实现 `GrayKeyResolver`） |
| 选择器 | `flexpoint-plugin-selector-ab` | `selector.ab` | `abSelector` | 显式 `@Bean`（需实现 `AbKeyResolver`） |
| 选择器 | `flexpoint-plugin-selector-weight` | `selector.weight` | `weightSelector` | `flexpoint.plugins.weight` |
| 选择器 | `flexpoint-plugin-selector-tenant` | `selector.tenant` | `tenantSelector` | 显式 `@Bean`（需实现 `TenantResolver`） |
| 选择器 | `flexpoint-plugin-selector-cache` | `selector.cache` | `CachingSelector`（装饰器） | 显式 `@Bean`（需 `delegate`） |
| 观测/治理 | `flexpoint-plugin-observability` | `core.observability` | `ObservabilityPlugin` | `flexpoint.plugins.observability` |
| 观测/治理 | `flexpoint-plugin-audit` | `observe.audit` | `AuditLogSubscriber` | `flexpoint.plugins.audit` |
| 观测/治理 | `flexpoint-plugin-slowcall` | `observe.slowcall` | `SlowCallSubscriber` | `flexpoint.plugins.slowcall` |
| 观测/治理 | `flexpoint-plugin-metrics` | `observe.metrics` | `MetricsSummaryPlugin` | `flexpoint.plugins.metrics` |
| 行为增强 | `flexpoint-plugin-retry` | `resilience.retry` | `RetryInterceptor`（order 300） | `flexpoint.plugins.retry` |
| 行为增强 | `flexpoint-plugin-resilience` | `resilience.guard` | `CircuitBreakerInterceptor`（100）+ `TimeoutInterceptor`（400） | `flexpoint.plugins.resilience` |

要点：**选择器插件的运行期路由依据由业务方实现对应 Resolver 提供**（`tag`/`gray`/`ab`/`tenant` 分别实现 `LabelResolver`/`GrayKeyResolver`/`AbKeyResolver`/`TenantResolver` 提供 labels / key / tenantId；`weight` 只读候选标签，不需任何 Resolver；`code`/`code-version` 用各自的 `CodeResolver`/`CodeVersionResolver`）。框架不再内置任何请求上下文。**行为增强插件基于 [拦截器 SPI](/guide/ext#调用管线与拦截器)**。

---

## 选择器插件

### Code 选择器

- 模块 `flexpoint-plugin-selector-code`，pluginId `core.selector.code`，选择器名 `codeSelector`。
- 关键类：`CodeSelectorPlugin`、`CodeSelector`（继承 `AbstractSelector`）、`CodeSelector.CodeResolver { String resolveCode(); }`。
- 构造：`CodeSelectorPlugin(CodeResolver resolver)`（resolver 必填）。按 `resolveCode()` 解析出的 code 过滤候选（`ext.getCode()` 相等者通过）。
- **装配**：`flexpoint.plugins.code.enabled=true` 开启，但**还需**容器中提供一个 `CodeSelector.CodeResolver` Bean（`@ConditionalOnBean`）——没有 resolver 则跳过装配。

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>flexpoint-plugin-selector-code</artifactId>
    <version>2.0.0</version>
</dependency>
```

```yaml
flexpoint:
  plugins:
    code:
      enabled: true
```

```java
// 提供 code 解析来源（值来自业务方自有来源：请求头 / 自定义请求持有者 / 线程变量等）
@Bean
public CodeSelector.CodeResolver codeResolver(AppRequestHolder holder) {
    return holder::getAppCode;
}
```

| 配置项 | 类型 | 默认 | 说明 |
|--------|------|------|------|
| `flexpoint.plugins.code.enabled` | boolean | `false` | 开启后装配 `codeSelector`（仍需容器提供 `CodeResolver` Bean） |

> 也可跳过属性开关，直接声明 `@Bean CodeSelectorPlugin`（构造时自带 resolver）——同类型 Bean 会覆盖属性装配。

扩展点接口用 `@FpSelector("codeSelector")` 绑定。

### Code + Version 选择器

- 模块 `flexpoint-plugin-selector-code-version`，pluginId `core.selector.code-version`，选择器名 `codeVersionSelector`。**依赖 `flexpoint-plugin-selector-code`**（`CodeVersionSelector extends CodeSelector`），引入本模块无需再单独引入 code 模块。
- 关键类：`CodeVersionSelectorPlugin`、`CodeVersionSelector`、`CodeVersionSelector.CodeVersionResolver`（继承 `CodeResolver`，新增 `default String resolveVersion()`，默认 `"1.0.0"`）。
- 构造：`CodeVersionSelectorPlugin(CodeVersionResolver resolver)`。先按 code 过滤，再按 `version` 标签（`ext.getTags().getString("version", "1.0.0")`）过滤。
- **装配**：`flexpoint.plugins.code-version.enabled=true` 开启（属性前缀是 kebab 的 `code-version`），**还需**容器中提供一个 `CodeVersionSelector.CodeVersionResolver` Bean（`@ConditionalOnBean`）——没有则跳过装配。

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>flexpoint-plugin-selector-code-version</artifactId>
    <version>2.0.0</version>
</dependency>
```

```yaml
flexpoint:
  plugins:
    code-version:
      enabled: true
```

```java
// 提供 code + version 解析来源（值来自业务方自有来源，如请求头 / 自定义请求持有者）
@Bean
public CodeVersionSelector.CodeVersionResolver codeVersionResolver(AppRequestHolder holder) {
    return new CodeVersionSelector.CodeVersionResolver() {
        @Override public String resolveCode() { return holder.getAppCode(); }
        @Override public String resolveVersion() { return holder.getAppVersion(); }
    };
}
```

| 配置项 | 类型 | 默认 | 说明 |
|--------|------|------|------|
| `flexpoint.plugins.code-version.enabled` | boolean | `false` | 开启后装配 `codeVersionSelector`（仍需容器提供 `CodeVersionResolver` Bean） |

> 也可直接声明 `@Bean CodeVersionSelectorPlugin`（构造时自带 resolver）覆盖属性装配。

### 标签选择器 tag

- 模块 `flexpoint-plugin-selector-tag`，pluginId `selector.tag`，选择器名 `tagSelector`。
- 行为：从业务方实现的 `LabelResolver` 取得 labels，与候选 `getTags()` 做 **AND 匹配**——每个 label 键值都需在候选标签中存在；labels 为空则 MISS。
- 关键类：`TagSelectorPlugin`、`TagSelector`、`TagSelector.LabelResolver { Map<String,String> resolveLabels(); }`。
- 构造：`TagSelectorPlugin(LabelResolver resolver)`（resolver 必填）。
- **装配**：需业务运行期数据，**不纳入「配置即装配」**，请显式声明 `@Bean`（无对应属性开关）。

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>flexpoint-plugin-selector-tag</artifactId>
    <version>2.0.0</version>
</dependency>
```

```java
// labels 来自业务方自有来源（如请求头 / 自定义请求持有者）
@Bean
public TagSelectorPlugin tagSelectorPlugin(AppRequestHolder holder) {
    return new TagSelectorPlugin(holder::resolveLabels);
}
```

### 灰度选择器 gray

- 模块 `flexpoint-plugin-selector-gray`，pluginId `selector.gray`，选择器名 `graySelector`。
- 行为：对灰度键哈希取模 100，`< percentage` 命中灰度候选（标签 `gray==true` 或 `group=="gray"`），否则命中非灰度候选。灰度键由业务方实现的 `GrayKeyResolver` 提供。
- 关键类：`GraySelectorPlugin`、`GraySelector`、`GraySelector.GrayKeyResolver { String resolveKey(); }`。
- 构造：`GraySelectorPlugin(int percentage, GrayKeyResolver resolver)`。
- **装配**：需业务运行期数据，**不纳入「配置即装配」**，请显式声明 `@Bean`（无对应属性开关）。

```java
// 灰度键（如 uid/deviceId）来自业务方自有来源；此处 20% 走灰度
@Bean
public GraySelectorPlugin graySelectorPlugin(AppRequestHolder holder) {
    return new GraySelectorPlugin(20, holder::getUid);
}
```

### A/B 选择器 ab

- 模块 `flexpoint-plugin-selector-ab`，pluginId `selector.ab`，选择器名 `abSelector`。
- 行为：按权重分桶。对切分键哈希取模总权重、在权重排序的桶上定位一个 `bucket`，命中标签 `bucket` 等于该值的候选。切分键由业务方实现的 `AbKeyResolver` 提供。
- 关键类：`AbTestSelectorPlugin`、`AbTestSelector`、`AbTestSelector.AbKeyResolver { String resolveKey(); }`。
- 构造：`AbTestSelectorPlugin(Map<String,Integer> buckets, AbKeyResolver resolver)`。
- **装配**：需业务运行期数据，**不纳入「配置即装配」**，请显式声明 `@Bean`（无对应属性开关）。

```java
// 切分键来自业务方自有来源；A/B 各 50%
@Bean
public AbTestSelectorPlugin abSelectorPlugin(AppRequestHolder holder) {
    Map<String, Integer> buckets = new HashMap<>();
    buckets.put("A", 50);
    buckets.put("B", 50);
    return new AbTestSelectorPlugin(buckets, holder::getUid);
}
```

### 权重选择器 weight

- 模块 `flexpoint-plugin-selector-weight`，pluginId `selector.weight`，选择器名 `weightSelector`。
- 行为：对候选按标签 `weight`（默认 1，负数按 0）做**加权随机**，命中其一（直接实现 `Selector`，因此多候选不会 `AMBIGUOUS`）。**不读上下文**。
- 构造：`WeightSelectorPlugin()` / `WeightSelectorPlugin(long seed)` / `WeightSelectorPlugin(Random random)`。

```yaml
flexpoint:
  plugins:
    weight:
      enabled: true
```

| 配置项 | 类型 | 默认 | 说明 |
|--------|------|------|------|
| `flexpoint.plugins.weight.enabled` | boolean | `false` | 开启后装配 `weightSelector`（无参构造） |

### 租户选择器 tenant

- 模块 `flexpoint-plugin-selector-tenant`，pluginId `selector.tenant`，选择器名 `tenantSelector`。
- 行为：从业务方实现的 `TenantResolver` 取得 tenantId，命中 `ext.getCode()==tenantId` 或标签 `tenant==tenantId` 的候选。`fallback=true` 时，无匹配（或无 tenantId）回退到「默认」候选（code=="default" 或标签 `tenant` 缺省/"default"）。
- 关键类：`TenantSelectorPlugin`、`TenantSelector`、`TenantSelector.TenantResolver { String resolveTenantId(); }`。
- 构造：`TenantSelectorPlugin(TenantResolver resolver)`（fallback=false）/ `TenantSelectorPlugin(boolean fallback, TenantResolver resolver)`。
- **装配**：需业务运行期数据，**不纳入「配置即装配」**，请显式声明 `@Bean`（无对应属性开关）。

```java
// tenantId 来自业务方自有来源；开启无匹配回退到默认候选
@Bean
public TenantSelectorPlugin tenantSelectorPlugin(AppRequestHolder holder) {
    return new TenantSelectorPlugin(true, holder::getTenantId);
}
```

### 缓存选择器 cache

- 模块 `flexpoint-plugin-selector-cache`，pluginId `selector.cache`。
- 关键类：`CachingSelectorPlugin`、`CachingSelector`（**装饰器**，包裹另一个 `Selector delegate`）、可选 `CachingSelector.CacheKeyResolver { String resolveKey(); }`。缓存键**默认由排序后的候选 extId 组成**；如路由随业务维度（租户/灰度键等）变化，可传入 `CacheKeyResolver` 把该维度并入键，避免不同请求命中同一缓存项。缓存 delegate 的 `SelectionResult`；`ttlMillis <= 0` 表示永不过期。提供 `invalidate()` / `cacheSize()`。
- **必须显式 `@Bean` 装配，不纳入「配置即装配」**。原因：缓存选择器是装饰器，默认沿用 delegate 的同名注册；而 `FlexPointSpringSelectorRegister` 会把容器中所有 `Selector` Bean 自动注册进 `SelectorRegistry`——若走属性装配，装饰器与被包装的 delegate 同名，必然触发「选择器名重复」冲突（[资源级唯一](/guide/selector)）；且内置选择器均由插件内部创建、并非 `Selector` Bean，无法直接充当 delegate。因此请用显式 `@Bean` 自行控制 delegate 与名称，**建议用 3 参构造指定独立 `name`**，声明后会被上述注册器自动纳入。

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>flexpoint-plugin-selector-cache</artifactId>
    <version>2.0.0</version>
</dependency>
```

```java
@Bean
public CachingSelectorPlugin cachingSelectorPlugin(TenantSelector delegate) {
    // 为某个选择器套一层缓存，TTL 5s；用独立 name 避免与被包装 delegate 同名冲突
    return new CachingSelectorPlugin(delegate, 5000L, "cachedTenantSelector");
}
```

构造：`CachingSelectorPlugin(Selector delegate)` / `(Selector delegate, long ttlMillis)` / `(Selector delegate, long ttlMillis, String name)`（不传 name 则用 `delegate.getName()`——此时须避免 delegate 也作为 `Selector` Bean 暴露，否则同名冲突）。使用独立 `name` 时，扩展点接口用 `@FpSelector("cachedTenantSelector")` 绑定该缓存选择器。

---

## 观测 / 治理插件

### 可观测 observability

- 模块 `flexpoint-plugin-observability`，pluginId `core.observability`。启用调用监控的推荐方式。
- 行为：`start()` 向 `ExtMonitor` 注入处理链（`PluginMetricsHandler` → 可选 `PluginCollectorHandler` → `PluginAlertHandler`），并订阅 `MonitorEventSubscriber`（优先级 200），把 `INVOKE_SUCCESS`/`INVOKE_FAIL` 转为 `recordInvocation`、`INVOKE_EXCEPTION` 转为 `recordException`。
- 扩展点：实现 `AlertStrategy`（告警）/ `MetricsCollector`（采集）注入自定义策略。
- **装配**：`flexpoint.plugins.observability.enabled=true` 开启。自动配置经 `ObjectProvider` 收集容器中的 `AlertStrategy` / `MetricsCollector`（**都可无**；二者皆无时用无参构造，仅默认指标处理链；有则注入自定义），无需手写插件 `@Bean`。

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>flexpoint-plugin-observability</artifactId>
    <version>2.0.0</version>
</dependency>
```

```yaml
flexpoint:
  plugins:
    observability:
      enabled: true
```

```java
// 可选：声明自定义告警策略 / 指标采集器，启用后会被自动收集注入
@Bean
public AlertStrategy myAlertStrategy() { return /* ... */; }

@Bean
public MetricsCollector myMetricsCollector() { return /* ... */; }
```

| 配置项 | 类型 | 默认 | 说明 |
|--------|------|------|------|
| `flexpoint.plugins.observability.enabled` | boolean | `false` | 开启后装配 `ObservabilityPlugin`（自动收集容器内 `AlertStrategy` / `MetricsCollector`） |

指标读取与 `flexpoint.monitor.*` 配置见 [可观测](/guide/observability)。

### 审计 audit

- 模块 `flexpoint-plugin-audit`，pluginId `observe.audit`。
- 行为：订阅 `AuditLogSubscriber`（优先级 100），输出结构化单行 INFO 日志——`category=SELECT`（`EXT_SELECTED`/`EXT_SELECTION_FAILED`，含决策解释摘要）、`category=INVOKE`（`INVOKE_SUCCESS`/`INVOKE_FAIL`/`INVOKE_EXCEPTION`）。
- 构造：`AuditLogPlugin()` / `AuditLogPlugin(boolean logSelection, boolean logInvocation)`。

```yaml
flexpoint:
  plugins:
    audit:
      enabled: true
      log-selection: true
      log-invocation: true
```

| 配置项 | 类型 | 默认 | 说明 |
|--------|------|------|------|
| `flexpoint.plugins.audit.enabled` | boolean | `false` | 是否装配 |
| `flexpoint.plugins.audit.log-selection` | boolean | `true` | 记录选择审计 |
| `flexpoint.plugins.audit.log-invocation` | boolean | `true` | 记录调用审计 |

### 慢调用 slowcall

- 模块 `flexpoint-plugin-slowcall`，pluginId `observe.slowcall`。
- 行为：订阅 `SlowCallSubscriber`（优先级 100），监听 `INVOKE_SUCCESS`/`INVOKE_FAIL`，当 `duration > thresholdMs` 输出 `SLOW_CALL ...` WARN 并回调可选 `SlowCallListener`。默认阈值 200ms。
- 构造：`SlowCallPlugin()` / `SlowCallPlugin(long thresholdMs)` / `SlowCallPlugin(long thresholdMs, SlowCallListener listener)`。

```yaml
flexpoint:
  plugins:
    slowcall:
      enabled: true
      threshold-ms: 300
```

| 配置项 | 类型 | 默认 | 说明 |
|--------|------|------|------|
| `flexpoint.plugins.slowcall.enabled` | boolean | `false` | 是否装配 |
| `flexpoint.plugins.slowcall.threshold-ms` | long | `200` | 慢调用阈值（毫秒） |

### 指标汇总 metrics

- 模块 `flexpoint-plugin-metrics`，pluginId `observe.metrics`。
- 行为：`MetricsSummaryPlugin` 同时是 `MonitorHandler`，`start()` 时挂到 `ExtMonitor` 并启动守护线程定期（默认 60s）输出 `METRICS_SUMMARY ...` INFO；按 extId 累计总数/成功/失败/异常/平均耗时。`getSnapshot()` 返回 `Map<String, ExtStatSnapshot>`。
- 构造：`MetricsSummaryPlugin()` / `MetricsSummaryPlugin(int intervalSeconds)`（非正数按 60）。

```yaml
flexpoint:
  plugins:
    metrics:
      enabled: true
      interval-seconds: 60
```

| 配置项 | 类型 | 默认 | 说明 |
|--------|------|------|------|
| `flexpoint.plugins.metrics.enabled` | boolean | `false` | 是否装配 |
| `flexpoint.plugins.metrics.interval-seconds` | int | `60` | 汇总输出周期（秒） |

---

## 行为增强插件

行为增强插件通过 [拦截器 SPI](/guide/ext#调用管线与拦截器) 介入调用管线。多个拦截器按 `order()` 升序编排（越小越外层）。当同时启用时，典型顺序为：熔断(100) → 重试(300) → 超时(400) → 真实调用。

### 重试 retry

- 模块 `flexpoint-plugin-retry`，pluginId `resilience.retry`。关键类 `RetryInterceptor`（`order` 默认 300）。
- 行为：最多 `maxAttempts` 次调用 `proceed()`；`Error` 从不重试；`backoffMs > 0` 时重试间隔休眠；重试耗尽后**重抛原始业务异常**（不包装自定义异常）。可传 `Predicate<Throwable>` 定制「哪些异常才重试」。
- 构造：`RetryPlugin()` / `RetryPlugin(int maxAttempts, long backoffMs)` / `(+ Predicate<Throwable> retryOn)` / `RetryPlugin(RetryInterceptor interceptor)`。

```yaml
flexpoint:
  plugins:
    retry:
      enabled: true
      max-attempts: 3
      backoff-ms: 100
```

| 配置项 | 类型 | 默认 | 说明 |
|--------|------|------|------|
| `flexpoint.plugins.retry.enabled` | boolean | `false` | 是否装配 |
| `flexpoint.plugins.retry.max-attempts` | int | `3` | 最大尝试次数 |
| `flexpoint.plugins.retry.backoff-ms` | long | `0` | 重试间隔（毫秒） |

### 弹性 resilience

- 模块 `flexpoint-plugin-resilience`，pluginId `resilience.guard`。一次装配两个拦截器：
  - `CircuitBreakerInterceptor`（order 100，最外层）—— `CLOSED / OPEN / HALF_OPEN` 三态；窗口内失败率达阈值则 `OPEN` 并快速失败（抛 `CircuitOpenException`，不调用 `proceed()`）；`openMillis` 后进入 `HALF_OPEN` 放行一次探测。
  - `TimeoutInterceptor`（order 400，最内层）—— 将 `proceed()` 提交线程池并 `get(timeoutMs)` 等待，超时抛 `CallTimeoutException`；`timeoutMs <= 0` 表示不启用超时（同步执行）。
- 构造：`ResiliencePlugin(long timeoutMs)`（熔断用默认 0.5/20/5000）/ `ResiliencePlugin(long timeoutMs, double failureRateThreshold, int minimumCalls, long openMillis)` / `(+ ExecutorService executor)`。

```yaml
flexpoint:
  plugins:
    resilience:
      enabled: true
      timeout-ms: 2000
      failure-rate-threshold: 0.5
      minimum-calls: 20
      open-millis: 5000
```

| 配置项 | 类型 | 默认 | 说明 |
|--------|------|------|------|
| `flexpoint.plugins.resilience.enabled` | boolean | `false` | 是否装配 |
| `flexpoint.plugins.resilience.timeout-ms` | long | `0` | 调用超时（毫秒），`0`=不启用超时 |
| `flexpoint.plugins.resilience.failure-rate-threshold` | double | `0.5` | 熔断失败率阈值（0–1） |
| `flexpoint.plugins.resilience.minimum-calls` | int | `20` | 熔断统计的最小样本数 |
| `flexpoint.plugins.resilience.open-millis` | long | `5000` | 熔断打开持续时间（毫秒） |

---

## 装配顺序建议

官方插件之间无硬性依赖，但注册顺序即装配顺序。一般建议：先注册选择器插件，再注册行为增强插件，最后注册可观测/审计插件。Spring Boot 下把它们声明为 `@Bean` 或用 `flexpoint.plugins.<name>.enabled=true` 开启即可被自动收集装配，见 [Spring Boot 接入](/guide/springboot)。
