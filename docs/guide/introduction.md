# 简介

**Flex Point** 是一款面向应用的轻量级**扩展点（Ext Point）框架**，为多业务场景下的「能力解耦、动态路由、灵活扩展」而生。它把「同一能力的不同业务实现」抽象为扩展点，由选择器在运行期按上下文选中目标实现，让业务代码只依赖接口、路由决策收敛在框架内。

## 解决什么问题

随着业务场景增多，`if/else`、策略工厂、`Map<String, Strategy>` 会逐渐蔓延到业务代码各处，新增一种场景往往要改动多处。Flex Point 把这类「按场景选择实现」的诉求标准化：

- **能力解耦** —— 业务侧只写 `@FpExt` 注入接口，不感知具体实现。
- **动态路由** —— 灰度、A/B、多租户、版本切换都只是「一次选择」。
- **可解释** —— 每次选择产出 `SelectionResult`（命中 / 未命中 / 歧义 + 决策解释），路由问题可追溯。
- **可扩展** —— 选择器、拦截器、监控、事件订阅都以插件接入，内核保持克制。

## 核心模型

Flex Point 的核心只有三件事：

1. **扩展点 `ExtAbility`** —— 把一项能力抽象为接口，多套实现按 `getCode()` 与标签区分。
2. **选择器 `Selector`** —— 在运行期从候选实现中选中目标，产出决策解释。
3. **注册与查找** —— 通过 `FlexPoint` 门面把二者连接，`findAbility` 返回可调用的动态代理。

```mermaid
flowchart LR
    A["业务代码<br/>@FpExt 注入接口"] -->|findAbility| FP["FlexPoint 门面"]
    FP --> SEL["Selector 选择器"]
    FP --> C["候选实现列表"]
    C --> SEL
    SEL -->|select| RES["SelectionResult<br/>HIT / MISS / AMBIGUOUS"]
    RES -->|HIT| IMPL["选中的实现（动态代理）"]
```

## 模块一览

| 模块 | 说明 |
|------|------|
| `flexpoint-dependencies-bom` | 依赖版本统一管理 BOM |
| `flexpoint-common` | 公共注解与常量（`@FpExt` / `@FpSelector` 等） |
| `flexpoint-core` | 核心内核，**零 Spring 依赖**：扩展点、选择器、插件、拦截器、事件、监控 |
| `flexpoint-spring` | Spring 环境集成 |
| `flexpoint-springboot` | Spring Boot Starter，零配置自动装配 |
| `flexpoint-plugin-all` | 官方插件聚合模块（每插件一子模块，`flexpoint-plugin-*`） |

## 适用与要求

- **JDK**：编译目标 `1.8`，兼容 8 及以上运行。
- **Spring**：非必需。核心模块可用于任意 JVM 应用；`flexpoint-spring` / `flexpoint-springboot` 只是在 Spring 环境下提供自动扫描注册与注解注入的便利。
- **版本**：当前统一 `2.0.0`。

## 下一步

- [快速开始](/guide/quickstart)：在 Spring Boot 项目中跑通最短链路。
- [核心概念](/guide/concepts)：理解扩展点、选择器、注册查找。
- [插件体系](/guide/plugin)：以插件方式扩展能力。
- [Spring Boot 接入](/guide/springboot)：自动配置与注解注入。
