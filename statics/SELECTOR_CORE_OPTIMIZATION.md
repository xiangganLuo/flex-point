# 选择器（selector）内核可优化点梳理

> 状态：待评审（draft）
> 范围：`flexpoint-core` 的 `com.flexpoint.core.selector`（`Selector` / `AbstractSelector` / `SelectorRegistry` / `DefaultSelectorRegistry` / `DecisionExplanation`）。
> 原则：**core 只保留干净、可组合的选择 SPI；具体路由策略（Code/Tag/Gray/AB/Weight/Tenant…）全部下沉为插件。** 本文只梳理“内核 SPI”层面的可优化点，不含具体策略实现。

---

## 1. 现状结构

| 文件 | 职责 | 现状要点 |
|---|---|---|
| `Selector` | 选择 SPI | `T select(List<T>)` + `getName()` + `DecisionExplanation explain(List<T>)` |
| `AbstractSelector` | 模板基类 | `select` 走 `filter`：空→null，唯一→命中，多个→**抛 `MultipleExtMatchedException`**；`explain` 亦走 `filter` |
| `SelectorRegistry` | 注册表 SPI | `register/getSelector/unregister/has`（无遍历） |
| `DefaultSelectorRegistry` | 默认实现 | Map + **资源级唯一**（同名禁止覆盖）；`getSelector` miss 返回 null |
| `DecisionExplanation` | 决策解释 | 候选/过滤/命中原因，HIT/MISS/AMBIGUOUS |

---

## 2. 可优化内核点

### P1【高】`select` 与 `explain` 重复过滤
- **现状**：`AbstractSelector.select()` 与 `explain()` 各自独立调用 `filter()`；`FlexPoint.findAbility` 在 Debug 下先 `explain(exts)` 再 `select(exts)` → **filter 跑两遍**。
- **问题**：热路径重复计算；两次结果理论上可能不一致（依赖 ambient 上下文时）。
- **建议**：统一为“一次选择、同时产出结果与解释”。引入 `SelectionResult<T>{ T selected; Outcome outcome; DecisionExplanation explanation; }`，`select` 返回它；`explain` 由结果携带，避免二次过滤。
- **影响**：破坏性（`select` 返回类型变更），需迁移调用方与所有选择器；建议配合 P6 一并做。

### P2【高】`AbstractSelector` 强制“唯一否则抛异常”，不适配多候选择一
- **现状**：`filter` 命中多个即抛 `MultipleExtMatchedException`。
- **问题**：**Weight/Gray/AB/加权轮询**等策略天然“多候选选其一”，当前基类会误判为歧义抛异常，迫使这些选择器绕开 `AbstractSelector`（重复造轮子）。
- **建议**：把“多命中如何收敛”做成可覆写的 **决胜钩子** `protected T pickOne(List<T> filtered)`：
  - 默认实现：严格模式（>1 抛异常，保持现有语义）；
  - 子类可覆写为 first/random/weight/hash 等。
  这样 `filter` 只做“候选筛选”，`pickOne` 做“最终收敛”，两阶段清晰，路由插件全部可复用基类。
- **影响**：向后兼容（默认严格）；新增 protected 钩子，不破坏现有子类。

### P3【中·架构】缺少标准 `SelectionContext` 显式入参
- **现状**：选择所需上下文靠 ambient `FlexPointContext`（土台 B）或选择器自带 `Resolver`（如 `CodeResolver`）。`select` 签名不含上下文。
- **问题**：隐式上下文不利于**单测**（要设 ThreadLocal）与**组合**（组合器无法透传/改写上下文）；不同选择器获取上下文方式不统一。
- **建议**：显式传入 `SelectionContext`（封装 `FlexPointContext` 快照 + 目标扩展点类型 + selectorName）：`SelectionResult<T> select(SelectionContext ctx, List<T> candidates)`。ambient 上下文仍可作为默认来源，但 API 显式化。
- **影响**：破坏性（签名变更）；与 P1 合并一次性做，减少反复。

### P4【中】选择器不可组合（一扩展点一选择器）
- **现状**：`@FpSelector("name")` 绑定单个选择器；`code→code+tags` 回退逻辑硬编码在 `FlexPoint.findAbilityByCodeAndTagsOrFallback`。
- **问题**：无法声明式组合“先 A 再回退 B”“A 且 B”“短路”；蓝图的“策略组合器”缺内核支撑。
- **建议**：新增内核 `CompositeSelector`（责任链/回退链）SPI：按顺序尝试子选择器，命中即返回、未命中回退下一个（可短路）。把 code→tags 回退下沉为组合的一个实例。
- **影响**：新增能力，不破坏现有；依赖 P1/P6 的结果语义。

### P5【低】`SelectorRegistry` 缺遍历/可观测能力
- **现状**：只有 `register/getSelector/unregister/has`；`getSelector` miss 返回 null。
- **建议**：新增 `Collection<String> names()` 或 `Map<String,Selector> getAll()`（诊断/加载报告用）；可选 `Optional<Selector> find(name)`；注册时校验名称字符集/非空（已判空）。
- **影响**：加法式，兼容。

### P6【中】选择结果语义不统一（null / 异常）
- **现状**：`null`=未命中，`MultipleExtMatchedException`=歧义，正常=命中；语义分散在返回值与异常两条通道。
- **问题**：调用方要同时处理 null 与 catch 异常；不利于组合与统一可观测。
- **建议**：用 `SelectionResult` 的 `Outcome{HIT,MISS,AMBIGUOUS}` 统一承载（与 `DecisionExplanation.Outcome` 对齐/复用）。是否仍对 AMBIGUOUS 抛异常由上层策略决定（默认可保持抛，兼容）。
- **影响**：与 P1 同批。

### P7【中】决策解释仅 Debug 日志，未事件化
- **现状**：`DecisionExplanation` 仅在 `FlexPoint.findAbility` Debug 日志输出；`EXT_SELECTED/EXT_SELECTION_FAILED` 事件不含解释详情。
- **建议**：将决策解释作为 attribute 附到选择相关事件（`EXT_SELECTED`/`EXT_SELECTION_FAILED`），让 monitor/审计插件可消费；生产按采样级别输出（蓝图“决策解释为一等公民”）。
- **影响**：加法式，兼容。

### P8【低·谨慎】选择结果缓存
- **现状**：每次 `findAbility` 都实时选择。
- **建议**：仅在“上下文键可归一化且候选稳定”的受限场景做缓存，默认关闭；否则易出错。**列为观察项，暂不做。**

---

## 3. 目标 SPI 形态（P1+P2+P3+P6 合并后的建议）

```java
public interface Selector {
    String getName();
    <T extends ExtAbility> SelectionResult<T> select(SelectionContext ctx, List<T> candidates);
}

// 结果对象：一次选择同时给出命中与解释
public final class SelectionResult<T> {
    Outcome outcome;              // HIT / MISS / AMBIGUOUS
    T selected;                  // HIT 时非空
    DecisionExplanation explanation;
}

public abstract class AbstractSelector implements Selector {
    public final <T> SelectionResult<T> select(SelectionContext ctx, List<T> candidates) {
        List<T> filtered = filter(ctx, candidates);   // 只筛选
        // 空→MISS；唯一→HIT；多个→pickOne 收敛（默认严格=AMBIGUOUS）
    }
    protected abstract <T> List<T> filter(SelectionContext ctx, List<T> candidates);
    protected <T> T pickOne(List<T> filtered) { /* 默认严格：>1 视为歧义 */ }
}
```
> 该形态让路由插件全部复用基类，`filter` 管筛选、`pickOne` 管收敛、结果对象统一承载命中+解释，组合器可透传 `SelectionContext`。

---

## 4. 兼容性与迁移
- P1/P3/P6（改 `select` 签名与返回）为**破坏性**：影响 `Selector` 所有实现与 `FlexPoint`/`AbstractSelector`；建议**一次性合批**并同步更新全部选择器插件与测试（编译器兜底找齐）。
- P2/P4/P5/P7 为**加法式**，可增量、独立推进，风险低。
- 迁移顺序建议：P5→P7（低风险先行）→ P2（决胜钩子，兼容）→ P1+P3+P6（合批破坏性重构）→ P4（组合器，依赖前者）。

---

## 5. 与插件的关系（core vs plugin 边界）
- **core 保留**：`Selector`/`SelectionResult`/`SelectionContext`/`AbstractSelector`(filter+pickOne)/`CompositeSelector`/`SelectorRegistry`/`DecisionExplanation`——即**选择 SPI 与编排骨架**。
- **插件承载**：Code/CodeVersion/Tag/Gray/AB/Weight/Tenant 等**具体策略**，均以 `filter`(+`pickOne`) 复用基类，读 `SelectionContext`，配置即装配。

---

## 6. 待评审的关键决策
| 编号 | 决策 | 建议 |
|---|---|---|
| S1 | 是否合批做 P1+P3+P6（`select` 返回 `SelectionResult` + 显式 `SelectionContext`） | 建议合批（一次破坏性到位，避免反复） |
| S2 | 歧义(AMBIGUOUS)默认是否仍抛异常 | 默认抛，兼容；可由上层策略/组合器改为“取第一”等 |
| S3 | `pickOne` 决胜钩子是否本期先行（兼容、解锁 weight/gray/ab 插件） | 建议先行 |
| S4 | 决策解释事件化(P7)是否本期做 | 建议做（可观测下沉，低风险） |
| S5 | 组合器 `CompositeSelector`(P4) 本期或下期 | 视排期，建议紧随 S1 之后 |

---

## 附：一句话结论
选择器内核当前**能用但偏“单选一次性”**，主要短板是：`select/explain` 双跑过滤、`AbstractSelector` 对“多候选选其一”不友好、缺显式上下文与组合能力。建议以 **`SelectionResult` + `SelectionContext` + `filter/pickOne` 两阶段 + `CompositeSelector` 组合** 重塑为“干净可组合的选择 SPI”，具体策略一律下沉插件。
