# 第一阶段（core 插件 SPI 化）文档索引（进度已对齐 2026-03-25）

- [技术方案](./TECHNICAL_DESIGN.md)
- [详细子任务拆解](./TASK_BREAKDOWN.md)
- [core 对齐评审与优化项](./CORE_ALIGNMENT_REVIEW.md)

> 本目录用于沉淀第一阶段执行文档，作为 `statics/FLEXPOINT_PLAN.md` 中 Phase A 的配套细化资料。
>
> 最新进展摘要：
> - 核心 SPI、管理器与 Builder 装配入口已合入主干；
> - 新增示例测试 `PluginSpiExampleTest` 验证装配、依赖与降级；
> - 官方内置插件（Selector/Event/Monitor）按 P1 排期保留；
> - 单元测试（依赖解析/冲突/状态机）将按计划补齐。

> 前置依赖：第0阶段（现有内核优化）完成。
