# EventPublisher 技术方案与去留评估（最终决策版）

## 1. 评审问题回顾

评审指出两个核心问题：
1. `build()` 过程对全局 EventBus 的覆盖会导致后创建实例“劫持事件路由”；
2. `shutdown()` 通过全局静态入口关闭总线会误伤其他实例。

问题根因：**静态全局发布器与实例生命周期耦合不当**。

---

## 2. 去留评估

### 结论：移除 `EventPublisher`（静态全局发布器）

当前框架尚未正式上线，不需要兼容历史调用路径，因此采用更干净的方案：

- 移除静态全局 `EventPublisher`；
- 以实例级 `EventDispatcher` 替代，绑定到 `FlexPoint` 对象；
- 事件发布、关闭、路由全部按实例隔离。

这可以从根本上消除“全局覆盖”和“跨实例误关”两类问题。

---

## 3. 新方案设计

### 3.1 实例级事件分发
- 新增 `EventDispatcher`（非静态、持有实例 `EventBus`）。
- `FlexPointBuilder` 在构建期创建 `EventBus + EventDispatcher`，注入到：
  - `FlexPoint`
  - `DefaultExtAbilityRegistry`
  - `DefaultSelectorRegistry`
  - 调用代理 `EventPublisherInvocationHandler`

### 3.2 生命周期控制
- `FlexPoint.shutdown()` 仅关闭当前实例持有的 `EventBus`。
- 不再存在全局共享总线，不需要引用计数与跨实例协调。

### 3.3 行为结果
- 多实例之间事件隔离；
- 任一实例关闭不会影响其他实例；
- 不再有“后建实例替换全局路由”的行为。

---

## 4. 风险与约束

- 这是一次“去兼容化”改动，适合当前未正式上线阶段；
- 后续如需跨实例统一事件治理，应通过明确的外部总线/平台适配实现，而非静态全局变量。

---

## 5. 验收标准

- 不存在 `EventPublisher` 静态全局类；
- 每个 `FlexPoint` 实例拥有独立 `EventBus`；
- 多实例测试中，关闭 A 不影响 B 的事件能力。
