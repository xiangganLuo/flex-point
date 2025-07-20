# Flex Point 核心架构（V1）

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
        A2[扩展点接口<br/>@FpSelector]
        A3[扩展点实现]
        A4[选择器实现<br/>getName]
    end

    %% ===== 框架核心层 =====
    subgraph 框架核心层
        B1[FlexPoint<br/>门面]
        B2[ExtAbilityRegistry<br/>注册中心]
        B3[ExtMonitor<br/>监控器]
        B4[SelectorRegistry<br/>选择器注册表<br/>Name->Selector]
        C1[FlexPointConfig<br/>配置]
        C2[FlexPointConfigValidator<br/>校验]
    end

    %% ===== 测试层 =====
    subgraph 测试层
      	D2[complx]
      	D6[integration]
    end

    %% ===== 简化后的关系 =====
    A1 --> B1
    A2 & A3 --> B2
    A4 -->|按名称注册| B4
    A2 -->|@FpSelector指定| A4
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
- **FlexPoint**：门面类，统一API，协调注册、查找、监控、选择器
- **ExtAbilityRegistry**：扩展点注册中心，负责扩展点的注册、查找、注销、并发安全
- **ExtMonitor**：监控器，统计扩展点调用、异常、耗时等
- **SelectorRegistry**：选择器注册表，按选择器名称管理选择器实例，支持基于@FpSelector注解的动态选择
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

### 2. 选择器注册
```java
// 选择器实现
public class CustomSelector implements Selector {
    @Override
    public String getName() { return "customSelector"; }
    
    @Override
    public <T extends ExtAbility> T select(List<T> candidates) {
        // 选择逻辑
    }
}

// 注册选择器
flexPoint.registerSelector(new CustomSelector());
```

### 3. 扩展点接口定义
```java
@FpSelector("customSelector")  // 指定使用的选择器名称
public interface OrderProcessAbility extends ExtAbility {
    String processOrder(String orderId, String orderData);
}
```

### 4. 查找扩展点
```java
// 框架会根据@FpSelector注解自动查找对应的选择器
OrderProcessAbility ability = flexPoint.findAbility(OrderProcessAbility.class);
```

### 5. 监控统计
```java
String extId = ability.getCode();
flexPoint.recordInvocation(extId, 100, true);
ExtMonitor.ExtMetrics metrics = flexPoint.getExtMetrics(extId);
```

### 6. 注销扩展点
```java
flexPoint.unregister(extId);
```

---

## 选择器与业务规则

### 选择器架构特点
- **基于名称管理**：选择器通过getName()方法返回的名称进行注册和查找
- **注解驱动**：扩展点接口通过@FpSelector注解指定使用的选择器名称
- **简化配置**：避免复杂的选择器链配置，每个扩展点对应一个选择器
- 
### 使用示例
```java
// 1. 定义选择器
@Component
public class CodeVersionSelector implements Selector {
    @Override
    public String getName() { return "codeVersionSelector"; }
    
    @Override
    public <T extends ExtAbility> T select(List<T> candidates) {
        String code = AppContext.getAppCode();
        for (T ext : candidates) {
            if (code.equals(ext.getCode())) return ext;
        }
        return null;
    }
}

// 2. 扩展点接口指定选择器
@FpSelector("codeVersionSelector")
public interface OrderProcessAbility extends ExtAbility {
    String processOrder(String orderId);
}

// 3. 框架自动匹配
OrderProcessAbility ability = flexPoint.findAbility(OrderProcessAbility.class);
```

### 支持的业务规则
- **多字段匹配**：如code+version组合匹配
- **灰度发布**：基于用户ID或百分比的灰度选择
- **A/B测试**：基于用户分组的实验选择
- **环境隔离**：基于环境变量的实现选择
- **租户隔离**：基于租户ID的实现选择

---

## 测试体系与用例覆盖

- **ConfigTest**：配置默认值、校验、禁用场景
- **ExtRegistryTest**：注册、查找、注销、重复注册、并发注册
- **MonitorTest**：调用统计、异常统计、指标重置
- **SelectorTest**：选择器注册、名称匹配、@FpSelector注解解析、选择器未找到异常
- **IntegrationTest**：注册、查找、选择、监控、注销等全流程
- **complx**：灰度、A/B、多字段动态匹配等复杂业务规则

---