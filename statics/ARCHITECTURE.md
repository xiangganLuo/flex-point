# Flex Point 核心架构

## 项目核心作用

Flex Point 是一个**灵活、可扩展、可监控**的扩展点框架，核心目标：

1. **多业务支持**：支持多业务线/多前台/多版本的动态扩展
2. **插件化**：支持即插即用的扩展点注册与管理
3. **可测试**：内置完善的单元与集成测试体系
4. **轻量级**：无外部依赖，专注核心功能
5. **可监控**：内置监控，便于问题排查

---

## 核心架构图

```mermaid
graph TB
    %% ===== 业务层 =====
    subgraph 业务层
        A1[业务应用]
        A2[扩展点接口]
        A3[扩展点实现]
        A4[场景解析器]
    end

    %% ===== 框架核心层 =====
    subgraph 框架核心层
        B1[FlexPoint<br/>门面]
        B2[ExtensionAbilityRegistry<br/>注册中心]
        B3[ExtensionMonitor<br/>监控器]
        B4[ExtensionResolutionStrategyRegistry<br/>策略注册表]
        C1[FlexPointConfig<br/>配置]
        C2[FlexPointConfigValidator<br/>校验]
    end

    %% ===== 测试层 =====
    subgraph 测试层
        D[monitor]
      	D2[complx]
      	D3[config]
      	D4[extension]
      	D5[resolution]
      	D6[integration]
    end

    %% ===== 简化后的关系 =====
    A1 --> B1
    A2 & A3 --> B2
    A4 --> B4
    B1 -->|使用| B2
    B1 -->|使用| B3
    B1 -->|使用| B4
    B1 -->|读取| C1
    C1 -->|校验| C2
    D -->|验证| B1
    D2 -->|验证| B1
    D3 -->|验证| B1
    D4 -->|验证| B1
    D5 -->|验证| B1
    D6 -->|验证| B1
    
    %% ===== 样式定义 =====
    classDef business fill:#4CAF50,stroke:#2E7D32,color:white
    classDef core fill:#2196F3,stroke:#0D47A1,color:white
    classDef test fill:#9C27B0,stroke:#6A1B9A,color:white
    class A1,A2,A3,A4 business
    class B1,B2,B3,B4,C1,C2 core
    class D,D2,D3,D4,D5,D6 test
```

---

## 各模块核心作用与协作

### flexpoint-core
- **FlexPoint**：门面类，统一API，协调注册、查找、监控、策略
- **ExtensionAbilityRegistry**：扩展点注册中心，负责扩展点的注册、查找、注销、并发安全
- **ExtensionMonitor**：监控器，统计扩展点调用、异常、耗时等
- **ExtensionResolutionStrategyRegistry**：解析策略注册表，支持多策略、注解优先、上下文动态选择
- **FlexPointConfig**：配置管理，支持灵活配置与校验

### flexpoint-common
- 注解、常量、异常、工具类等基础设施

### flexpoint-spring / flexpoint-springboot
- Spring/Spring Boot集成，自动注册、自动配置、代理、属性外部化

### flexpoint-test
- 单元测试、集成测试、复杂业务规则测试，保障主流程和扩展机制的健壮性

---

## 典型主流程

### 1. 扩展点注册
```java
flexPoint.register(new MallOrderProcessAbilityV1());
flexPoint.register(new LogisticsOrderProcessAbility());
```

### 2. 解析策略注册
```java
flexPoint.registerResolver(new CustomExtensionResolutionStrategy());
```

### 3. 查找扩展点
```java
OrderProcessAbility ability = flexPoint.findAbility(OrderProcessAbility.class, context);
```

### 4. 监控统计
```java
String extId = ability.getCode() + ":" + ability.version();
flexPoint.recordInvocation(extId, 100, true);
ExtensionMonitor.ExtensionMetrics metrics = flexPoint.getExtensionMetrics(extId);
```

### 5. 注销扩展点
```java
flexPoint.unregister(extId);
```

---

## 解析策略与业务规则
- 支持注解（@ExtensionResolverSelector）指定策略
- 支持上下文（如ThreadLocal、参数、环境变量）动态决策
- 支持多字段（如code+version）匹配、灰度、A/B等复杂业务规则

---

## 测试体系与用例覆盖

- **ConfigTest**：配置默认值、校验、禁用场景
- **ExtensionRegistryTest**：注册、查找、注销、重复注册、并发注册
- **MonitorTest**：调用统计、异常统计、指标重置
- **ResolutionTest**：策略注册、注解优先、上下文动态选择、策略未找到异常
- **IntegrationTest**：注册、查找、解析、监控、注销等全流程
- **complx/**：灰度、A/B、多字段动态匹配等复杂业务规则

---

## 设计模式应用

| 设计模式 | 应用场景 | 实现类 |
|---------|---------|--------|
| 门面模式 | 统一API | FlexPoint |
| 工厂模式 | 扩展点创建 | ExtensionAbilityRegistry |
| 策略模式 | 解析策略 | ExtensionResolutionStrategy |
| 代理模式 | Spring集成 | ExtensionAbilityInvocationHandler |
| 注册模式 | 扩展点管理 | ExtensionAbilityRegistry |
| 观察者模式 | 监控 | ExtensionMonitor |
