---
layout: home

hero:
  name: Flex Point
  text: 多场景适配、极致灵活的扩展点框架
  tagline: 轻量级 Java 扩展点（Ext Point）框架 —— 能力解耦、动态路由、灵活扩展。核心零 Spring 依赖，Spring Boot 零配置启动。
  image:
    src: /logo.svg
    alt: Flex Point
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/quickstart
    - theme: alt
      text: 什么是 Flex Point
      link: /guide/introduction
    - theme: alt
      text: GitHub
      link: https://github.com/xiangganLuo/flex-point

features:
  - icon: 🧩
    title: 轻量内核
    details: 核心模块 flexpoint-core 零 Spring 依赖，基于标准 Java（JDK 1.8+）实现，可嵌入任意 JVM 应用；通过 FlexPointBuilder 即可构建实例。
    link: /guide/concepts
    linkText: 核心概念
  - icon: 🧭
    title: 场景选择器
    details: 一个接口、多套实现，由 Selector 在运行期按上下文动态选中；一次 select 同时产出命中实现与决策解释（HIT / MISS / AMBIGUOUS），路由可追溯。
    link: /guide/selector
    linkText: 选择器
  - icon: 🔌
    title: 插件化 SPI
    details: 选择器、事件订阅、监控、拦截器均以插件接入。模型极简 —— getId() + 生命周期，由 PluginManager 统一装配启停，任一插件失败统一降级。
    link: /guide/plugin
    linkText: 插件体系
  - icon: 🪝
    title: 调用拦截器
    details: 扩展点调用经动态代理进入 around 语义的拦截器链，可插拔式接入重试、超时、熔断、限流、缓存等横切能力。
    link: /guide/ext
    linkText: 扩展点 · 调用管线
  - icon: 📊
    title: 可观测
    details: 调用管线内置事件埋点拦截器，发布调用生命周期事件；配合 ExtMonitor 沉淀调用次数、耗时、成功率等指标，支持责任链处理与异步。
    link: /guide/observability
    linkText: 可观测
  - icon: 🚀
    title: Spring Boot 开箱即用
    details: 引入 flexpoint-springboot 即零配置启动：自动扫描注册扩展点/选择器，@FpSelector 声明选择器、@FpExt 自动注入扩展点。
    link: /guide/springboot
    linkText: Spring Boot 接入
---

## 一分钟接入

在 `pom.xml` 引入 Spring Boot 接入模块（版本统一 `2.0.0`）：

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>flexpoint-springboot</artifactId>
    <version>2.0.0</version>
</dependency>
```

定义扩展点接口、写多套实现、注入调用，路由交给选择器：

```java
// 1. 定义扩展点接口，声明使用的选择器
@FpSelector("codeVersionSelector")
public interface OrderProcessAbility extends ExtAbility {
    String processOrder(String orderId);
}

// 2. 多实现自动注册，按上下文动态路由
@Component
public class MallOrder implements OrderProcessAbility {
    public String getCode() { return "mall"; }
    public String processOrder(String id) { return "商城下单"; }
}

// 3. 业务里直接注入调用，选择器自动选中匹配实现
@FpExt
private OrderProcessAbility orderProcessAbility;
```

更多细节见 [快速开始](/guide/quickstart) 与 [核心概念](/guide/concepts)。
