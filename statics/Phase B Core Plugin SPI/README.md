# 第一阶段（core 插件 SPI 化）文档索引（进度已对齐 2026-03-25）

- [技术方案](./TECHNICAL_DESIGN.md)
- [详细子任务拆解](./TASK_BREAKDOWN.md)

> 本目录用于沉淀第一阶段执行文档，作为 `statics/FLEXPOINT_PLAN.md` 中 Phase A 的配套细化资料。
>
> 最新进展摘要：
> - 核心 SPI、管理器与 Builder 装配入口已合入主干；
> - 新增示例测试 `PluginSpiExampleTest` 验证装配、依赖与降级；
> - 官方内置插件（Selector/Event/Monitor）按 P1 排期保留；
> - 单元测试（依赖解析/冲突/状态机）将按计划补齐。

> 进展更新（2026-07-16）：
> - 修复 `DependencyResolver` order 全局重排破坏拓扑顺序的缺陷（回归测试已补）。
> - 单元测试补齐：依赖/冲突/资源级唯一、生命周期状态机、描述符校验、上下文边界、注册中心并发。
> - `FlexPoint` 接管插件生命周期：shutdown 逆序停止插件、暴露加载报告与状态。
> - 决策解释 v1：`DecisionExplanation` + `Selector.explain`（Debug 级输出）。
> - DX 文档：`ONE_PAGER.md` / `PLUGIN_TEMPLATE.md`；核心 Review：`../CORE_REVIEW_2026-07-16.md`。
> - 运行期治理：`PluginManager.enable/disable` 落地最小可用，`FlexPoint.enablePlugin/disablePlugin` 暴露入口。
> - E2/E3：官方插件装配生效测试、并发发布事件下插件稳定性测试补齐（测试总数 50，全绿）。
> - 事件语义收敛：查找事件去重/去矛盾；新增 code+tags→code 回退（findAbilityByCodeAndTagsOrFallback）。
> - F2 示例：java-example/PluginExampleMain 演示依赖/顺序/降级/运行期启停（已本地运行验证，测试总数 57，全绿）。
> - 详细清单见 [TASK_BREAKDOWN.md](./TASK_BREAKDOWN.md)。

> 前置依赖：第0阶段（现有内核优化）完成。
