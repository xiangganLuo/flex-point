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

- [核心架构](statics/ARCHITECTURE.md)
- [多场景接入示例（Spring Boot/Java原生）](flexpoint-examples/README.md)
- [测试用例](flexpoint-test/README.md)

---

## 🚀 快速开始

### 1. 依赖引入

在 Spring Boot 项目 `pom.xml` 中添加：
```xml
<dependency>
    <groupId>com.flexpoint</groupId>
    <artifactId>flexpoint-springboot</artifactId>
</dependency>
```

### 2. 定义扩展点接口

```java
@ExtensionResolverSelector("customStrategy")
public interface OrderProcessAbility extends ExtensionAbility {
    String processOrder(String orderId, double amount);
    String version(); // 必须实现
}
```

### 3. 实现扩展点（自动注册）

```java
import org.springframework.stereotype.Component;

@Component
public class MallOrderProcessAbilityV1 implements OrderProcessAbility {
    @Override
    public String getCode() { return "mall"; }
    @Override
    public String version() { return "1.0.0"; }
    @Override
    public String processOrder(String orderId, double amount) {
        return "商城订单处理完成V1";
    }
}

@Component
public class LogisticsOrderProcessAbility implements OrderProcessAbility {
    @Override
    public String getCode() { return "logistics"; }
    @Override
    public String version() { return "1.0.0"; }
    @Override
    public String processOrder(String orderId, double amount) {
        return "物流订单处理完成";
    }
}
```

### 4. 场景解析器

```java
@Component
public class CustomExtensionResolutionStrategy extends AbstractExtensionResolutionStrategy {
    @Override
    protected ResolutionContext extractContext() {
        // 例如：从ThreadLocal、上下文等获取业务code
        return new ResolutionContext("mall", null);
    }
    @Override
    public String getStrategyName() {
        return "customStrategy";
    }
}
```

### 5. 业务代码中直接注入并调用

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {
    @ExtensionAbilityReference
    private OrderProcessAbility orderProcessAbility;

    @GetMapping("/order/process")
    public String process(String orderId, double amount) {
        return orderProcessAbility.processOrder(orderId, amount);
    }
}
```

### 6. 配置文件示例

```yaml
flexpoint:
  enabled: true
  monitor:
    enabled: true
    log-invocation: true
  registry:
    allow-duplicate-registration: false
```

---

## 🔧 核心功能

### 扩展点注册与查找

```java
// 注册扩展点
flexPoint.register(new MallOrderProcessAbilityV1());
flexPoint.register(new MallOrderProcessAbilityV2());
flexPoint.register(new LogisticsOrderProcessAbility());

// 查找扩展点
OrderProcessAbility ability = flexPoint.findAbility(OrderProcessAbility.class);
```

### 扩展点监控

```java
// 获取扩展点调用统计
ExtensionMonitor.ExtensionMetrics metrics = flexPoint.getExtensionMetrics("mall:1.0.0");
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


---

## ⚙️ 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| flexpoint.enabled | boolean | true | 是否启用Flex Point框架 |
| flexpoint.monitor.enabled | boolean | true | 是否启用扩展点监控功能 |
| flexpoint.monitor.log-invocation | boolean | true | 是否记录扩展点调用日志 |
| flexpoint.monitor.log-resolution | boolean | true | 是否记录扩展点解析日志 |
| flexpoint.monitor.log-exception-details | boolean | true | 是否记录异常详情 |
| flexpoint.monitor.performance-stats-enabled | boolean | true | 是否启用性能统计 |
| flexpoint.registry.enabled | boolean | true | 是否启用扩展点自动注册 |
| flexpoint.registry.allow-duplicate-registration | boolean | false | 是否允许重复注册扩展点 |

> 以上配置可在 application.yml 或 application.properties 中灵活配置，详细含义见上表。

---

## 🎯 最佳实践

- **所有扩展点可选 version() 方法，默认1.0.0**。
- **注册扩展点时只需 flexPoint.register(ability)**，无需类型和元数据。
- **查找扩展点时直接 flexPoint.findAbility(AbilityClass.class)**。
- **自定义解析策略通过 flexPoint.registerResolver(...) 注册。**
- **推荐通过BOM统一依赖版本。**

---

### 🚦 Spring Boot 全流程实战

以 `flexpoint-examples/spring-boot-example` 为例，演示如何实现"基于上下文动态切换扩展点"的完整链路：

#### 1. 过滤器编写（上下文注入/鉴权）

```java
// src/main/java/com/flexpoint/example/springboot/framework/flexpoint/security/AppAuthFilter.java
@Component
public class AppAuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        // 假设header中有appCode
        String appCode = req.getHeader("X-App-Code");
        // 注入到ThreadLocal或自定义上下文
        SysAppContext.setAppCode(appCode);
        try {
            chain.doFilter(request, response);
        } finally {
            SysAppContext.clear();
        }
    }
}
```

#### 2. 自定义解析器（结合过滤器上下文）

```java
// src/main/java/com/flexpoint/example/springboot/framework/flexpoint/CustomExtensionResolutionStrategy.java
@Component
public class CustomExtensionResolutionStrategy extends AbstractExtensionResolutionStrategy {
    @Override
    protected ResolutionContext extractContext() {
        // 从SysAppContext获取appCode
        String code = SysAppContext.getAppCode();
        return new ResolutionContext(code, null);
    }
    @Override
    public String getStrategyName() { return "customStrategy"; }
}
```

#### 3. 扩展点接口与实现

```java
// src/main/java/com/flexpoint/example/springboot/framework/flexpoint/ability/OrderProcessAbility.java
@ExtensionResolverSelector("customStrategy")
public interface OrderProcessAbility extends ExtensionAbility {
    String processOrder(String orderId, double amount);
    String version();
}

// src/main/java/com/flexpoint/example/springboot/framework/flexpoint/ability/mall/MallOrderProcessAbility.java
@Component
public class MallOrderProcessAbility implements OrderProcessAbility {
    @Override
    public String getCode() { return "mall"; }
    @Override
    public String version() { return "1.0.0"; }
    @Override
    public String processOrder(String orderId, double amount) {
        return "商城订单处理完成";
    }
}
```

#### 4. 控制器中注入扩展点并调用

```java
// src/main/java/com/flexpoint/example/springboot/controller/OrderController.java
@RestController
public class OrderController {
    @ExtensionAbilityReference
    private OrderProcessAbility orderProcessAbility;

    @GetMapping("/order/process")
    public String process(String orderId, double amount) {
        return orderProcessAbility.processOrder(orderId, amount);
    }
}
```

#### 5. 监控与配置

```yaml
# src/main/resources/application.yml
flexpoint:
  enabled: true
  monitor:
    enabled: true
    log-invocation: true
  registry:
    allow-duplicate-registration: false
```

可通过注入 `ExtensionMonitor` 获取扩展点调用统计：
```java
@Autowired
private ExtensionMonitor extensionMonitor;

ExtensionMonitor.ExtensionMetrics metrics = extensionMonitor.getExtensionMetrics("mall:1.0.0");
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

<p align="center">
  <strong>让扩展点开发更简单、更灵活！</strong>
</p>