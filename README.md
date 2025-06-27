<p align="center">
  <img src="statics/img/logo.svg" alt="Flex Point Logo" width="120"/>
</p>
<h1 align="center">Flex Point 灵活扩展点框架</h1>
<p align="center">
  <strong>轻量级、极致灵活的扩展点解决方案</strong>
</p>
<p align="center">
    <a target="_blank" href="https://search.maven.org/artifact/com.flexpoint/flexpoint-springboot">
        <img src="https://img.shields.io/maven-central/v/com.flexpoint/flexpoint-springboot.svg?label=Maven%20Central" />
    </a>
    <a target="_blank" href='https://www.apache.org/licenses/LICENSE-2.0.html'>
        <img src='https://img.shields.io/badge/license-Apache%202.0-blue.svg'/>
    </a>
    <a target="_blank" href="https://github.com/xiangganLuo/flex-point">
        <img src="https://img.shields.io/github/stars/xiangganLuo/flex-point.svg?style=social" alt="github star"/>
    </a>
</p>

---

## 📚 简介

Flex Point 是一个极致灵活、支持多业务场景的扩展点（Extension Point）框架。它允许系统根据不同的应用代码（code）动态选择不同的业务实现，实现业务逻辑的灵活扩展和隔离。支持 Spring 生态，适合中大型企业级项目。

**核心特性：**
- 🚀 **轻量级设计** - 无缓存依赖，专注于核心扩展点功能
- 🎯 **灵活解析** - 支持自定义扩展点解析策略
- 🔧 **多环境支持** - Spring Boot、Spring、Java原生环境
- 📊 **监控集成** - 内置扩展点调用监控和性能统计
- 🏗️ **模块化架构** - 清晰的模块划分，按需引入

---

## 🏗️ 模块结构

```
FlexPoint/
├── flexpoint-dependencies-bom/   # 依赖版本BOM管理模块，统一管理所有依赖版本
├── flexpoint-common/             # 公共模块 - 接口、注解、常量、异常等
├── flexpoint-core/               # 核心模块 - 核心功能实现，不依赖Spring
├── flexpoint-spring/             # Spring集成模块 - Spring环境下的集成
├── flexpoint-springboot/         # Spring Boot自动配置模块
├── flexpoint-test/               # 测试模块 - 测试用例和示例
└── flexpoint-examples/           # 多场景接入示例模块 - Spring Boot、Java原生
```

| 模块名                        | 说明                                   |
|------------------------------|----------------------------------------|
| flexpoint-dependencies-bom    | 依赖版本BOM管理，所有依赖版本统一配置   |
| flexpoint-common              | 公共基础模块，接口、注解、常量、异常等   |
| flexpoint-core                | 核心实现模块，扩展点注册/查找/监控      |
| flexpoint-spring              | Spring集成，自动扫描注册扩展点           |
| flexpoint-springboot          | Spring Boot自动配置，开箱即用           |
| flexpoint-test                | 测试模块，测试用例和示例                |
| flexpoint-examples            | 多场景接入示例模块（Spring Boot、Java原生） |

---

## 📦 安装

### 🍊 Maven

在项目的 `pom.xml` 的 dependencies 中加入以下内容：

```xml
<dependency>
    <groupId>com.flexpoint</groupId>
    <artifactId>flexpoint-springboot</artifactId>
    <version>${revision}</version>
</dependency>
```

> 推荐业务项目通过 BOM 方式统一依赖版本：
>
> ```xml
> <dependencyManagement>
>   <dependency>
>     <groupId>com.flexpoint</groupId>
>     <artifactId>flexpoint-dependencies-bom</artifactId>
>     <version>${revision}</version>
>     <type>pom</type>
>     <scope>import</scope>
>   </dependency>
> </dependencyManagement>
> ```

---

## 📝 文档

- [官方文档（建设中）](#)
- [核心架构](statics/ARCHITECTURE.md)
- [示例代码](flexpoint-test)
- [多场景接入示例（Spring Boot/Java原生）](flexpoint-examples/README.md)
- [自定义扩展点解析策略与注册示例](#自定义扩展点解析策略)

---

## 🚀 快速开始

### 1. 定义扩展点接口

```java
public interface OrderStatusValidator extends ExtensionAbility {
    boolean isValidStatus(String fromStatus, String toStatus);
}
```

### 2. 实现扩展点

```java
@Component
@ExtensionInfo(id = "mall-validator", description = "商城订单状态验证器")
public class MallOrderStatusValidator implements OrderStatusValidator {
    @Override
    public String getCode() {
        return "mall";
    }
    @Override
    public boolean isValidStatus(String fromStatus, String toStatus) {
        return true;
    }
}
```

### 3. 注入并使用扩展点

```java
@Service
public class OrderService {
    @ExtensionAbilityReference(code = "mall")
    private OrderStatusValidator orderStatusValidator;
    public void validateOrderStatus(String fromStatus, String toStatus) {
        boolean valid = orderStatusValidator.isValidStatus(fromStatus, toStatus);
    }
}
```

## 🧩 自定义扩展点解析策略

Flex Point 支持自定义扩展点解析策略。只需继承 `AbstractExtensionResolutionStrategy` 并实现 `extractCode` 方法，然后通过 `withResolver` 或 `registerResolver` 注册。

### 1. 定义自定义解析器

```java
public class CustomExtensionResolutionStrategy extends AbstractExtensionResolutionStrategy {
    @Override
    protected String extractCode(Map<String, Object> context) {
        // 只需关注如何从context中提取code
        return (String) context.get("appCode");
    }
    @Override
    public String getStrategyName() {
        return "CustomExtensionResolutionStrategy";
    }
}
```

### 2. 注册自定义解析器

**方式一：建造者链式注册**
```java
FlexPoint flexPoint = FlexPointBuilder.create()
    .withResolver(new CustomExtensionResolutionStrategy())
    .build();
```

**方式二：运行时注册**
```java
flexPoint.registerResolver(new CustomExtensionResolutionStrategy());
```

### 3. 多策略动态切换

你可以为不同扩展点接口通过注解指定解析策略：

```java
@ExtensionResolver("CustomExtensionResolutionStrategy")
public interface DemoAbility extends ExtensionAbility {
    ...
}
```

---

## 🔧 核心功能

### 扩展点注册与查找

```java
// 手动注册扩展点
FlexPoint flexPoint = FlexPointBuilder.create().build();
flexPoint.register(OrderStatusValidator.class, new MallOrderStatusValidator());

// 查找扩展点
OrderStatusValidator validator = flexPoint.findAbility(OrderStatusValidator.class);
```

### 扩展点监控

```java
// 获取扩展点调用统计
ExtensionMonitor.ExtensionMetrics metrics = flexPoint.getExtensionMetrics("mall");
System.out.println("调用次数: " + metrics.getTotalInvocations());
System.out.println("平均耗时: " + metrics.getAverageDuration() + "ms");
```

### 配置管理

```yaml
# application.yml
flexpoint:
  enabled: true
  monitor:
    enabled: true
    log-invocation: true
    performance-stats-enabled: true
  registry:
    enabled: true
    allow-duplicate-registration: false
```

---

## 🎯 高级用法

### 1. 多业务场景扩展点

```java
// 商城订单处理
@Component
public class MallOrderProcessAbility implements OrderProcessAbility {
    @Override
    public String getCode() {
        return "mall";
    }
    // 实现商城特有的订单处理逻辑
}

// 物流订单处理
@Component
public class LogisticsOrderProcessAbility implements OrderProcessAbility {
    @Override
    public String getCode() {
        return "logistics";
    }
    // 实现物流特有的订单处理逻辑
}
```

### 2. 动态扩展点选择

```java
@Service
public class OrderService {
    private final FlexPoint flexPoint;
    
    public void processOrder(String appCode, Order order) {
        // 根据应用代码动态选择扩展点
        OrderProcessAbility processor = flexPoint.findAbility(OrderProcessAbility.class, 
            Map.of("code", appCode));
        processor.process(order);
    }
}
```

### 3. 扩展点元数据管理

```java
// 获取扩展点元数据
ExtensionMetadata metadata = flexPoint.getExtensionMetadata(
    OrderProcessAbility.class, "mall");
System.out.println("扩展点描述: " + metadata.getDescription());
System.out.println("扩展点版本: " + metadata.getVersion());
```

---

## 🏆 最佳实践

### 1. 扩展点设计原则

- **单一职责**：每个扩展点只负责一个特定的业务功能
- **接口稳定**：扩展点接口一旦发布，应保持向后兼容
- **版本管理**：通过元数据管理扩展点版本，支持平滑升级

### 2. 性能优化

- **合理使用监控**：生产环境建议关闭详细日志，保留性能统计
- **避免过度扩展**：扩展点数量过多会影响查找性能
- **及时清理**：定期清理不再使用的扩展点

### 3. 错误处理

```java
// 使用Optional处理扩展点不存在的情况
Optional<OrderProcessAbility> processor = flexPoint.findAbilityOpt(OrderProcessAbility.class);
if (processor.isPresent()) {
    processor.get().process(order);
} else {
    // 处理扩展点不存在的情况
    log.warn("未找到订单处理扩展点");
}
```

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

### 开发环境搭建

```bash
git clone https://github.com/xiangganLuo/flex-point.git
cd flex-point
mvn clean install
```

### 测试

```bash
mvn test
```

---

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

---

## 🙏 致谢

感谢所有为 Flex Point 框架做出贡献的开发者！

---

<p align="center">
  <strong>让扩展点开发更简单、更灵活！</strong>
</p>