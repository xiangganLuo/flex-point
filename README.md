<p align="center">
  <img src="statics/img/logo.svg" alt="Flex Point Logo" width="120"/>
</p>
<h1 align="center">Flex Point</h1>
<p align="center">
  <strong>多场景适配、轻量级、极致灵活的扩展点框架</strong>
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

Flex Point 是一款面向企业级应用的高可扩展性扩展点（Extension Point）框架，专为多业务场景下的"能力解耦、动态路由、灵活扩展"而设计。
它支持在不同业务上下文、租户、版本、A/B测试等多种场景下，动态选择和切换业务实现，极大提升了系统的可维护性和业务创新能力。

**核心特性：**
- 🚀 **轻量级设计** - 无缓存依赖，专注于核心扩展点功能
- 🎯 **灵活解析** - 支持自定义扩展点选择器，基于选择器名称管理
- 🔧 **多环境支持** - Spring Boot、Java原生环境
- 📊 **监控集成** - 内置扩展点调用监控和性能统计并预留了各种扩展点
- 🏗️ **模块化架构** - 清晰的模块划分，按需引入

**使用场景：**

- 多租户/多业务线能力隔离：不同租户/业务线可动态选择不同实现，支持灰度、A/B测试、版本切换等。
- 平台型/中台型系统：平台能力通过扩展点暴露，业务方可按需接入和自定义实现。
- 企业级复杂业务：如订单处理、风控、营销等场景下的多策略动态切换。

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

- [核心架构V1](statics/ARCHITECTURE_V1.md)
- [项目计划](statics/FLEXPOINT_PLAN.md)
- [多场景接入示例（Spring Boot/Java原生）](flexpoint-examples/README.md)
- [测试用例](flexpoint-test)

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
@FpSelector("codeVersionSelector")  // 指定使用的选择器名称
public interface OrderProcessAbility extends ExtensionAbility {
    String processOrder(String orderId, String orderData);
    String getOrderStatus(String orderId);
}
```

### 3. 实现扩展点（自动注册）

```java
import org.springframework.stereotype.Component;

@Component
public class MallOrderProcessAbility implements OrderProcessAbility {
    @Override public String getCode() { return "mall-app"; }
    @Override public String processOrder(String orderId, String orderData) { return "商城订单处理完成"; }
    @Override public String getOrderStatus(String orderId) { return "已支付"; }
}

@Component
public class LogisticsOrderProcessAbility implements OrderProcessAbility {
    @Override
    public String getCode() { return "logistics"; }
    @Override
    public String version() { return "1.0.0"; }
    @Override
    public String processOrder(String orderId, String orderData) {
        return "物流订单处理完成";
    }
}
```

### 4. 定义选择器

```java
import org.springframework.stereotype.Component;

@Component
public class CodeVersionSelector implements Selector {
    @Override
    public <T extends ExtensionAbility> T select(List<T> candidates, Context context) {
        String code = SysAppContext.getAppCode();
        for (T ext : candidates) {
            if (code.equals(ext.getCode())) return ext;
        }
        return null;
    }
    
    @Override
    public String getName() { return "codeVersionSelector"; }  // 与@FpSelector注解中的名称一致
}
```

### 5. 业务代码中直接注入并调用

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {
    @FpExt
    private OrderProcessAbility orderProcessAbility;

    @GetMapping("/order/process")
    public String process(String orderId, String orderData) {
        return orderProcessAbility.processOrder(orderId, orderData);
    }
}
```
---

## 🔧 核心功能

### 扩展点注册与查找

```java
// 注册扩展点
flexPoint.register(new MallOrderProcessAbilityV1());
flexPoint.register(new MallOrderProcessAbilityV2());
flexPoint.register(new LogisticsOrderProcessAbility());

// 查找扩展点（自动根据@FpSelector注解指定的选择器进行选择）
OrderProcessAbility ability = flexPoint.findAbility(OrderProcessAbility.class);
```

### 选择器自定义与注册

#### 推荐：默认选择器注册（Spring Boot最佳实践）

```java
import com.flexpoint.core.selector.resolves.CodeVersionSelector;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FlexPointConfig {
    /**
     * 注册默认的代码版本选择器
     */
    @Bean
    public CodeVersionSelector codeVersionSelector() {
        return new CodeVersionSelector(context -> SysAppContext.getAppCode());
    }
}
```

- 这样，业务代码只需在请求入口（如过滤器）设置好 `SysAppContext.setAppCode(appCode)`，能力查找时自动感知上下文。

#### 进阶：自定义 Selector（如需特殊路由/多维选择）

```java
import org.springframework.stereotype.Component;

@Component
public class CustomSelector implements Selector {
    @Override
    public <T extends ExtensionAbility> T select(List<T> candidates, Context context) {
        String code = SysAppContext.getAppCode();
        for (T ext : candidates) {
            if (code.equals(ext.getCode())) return ext;
        }
        return null;
    }
    
    @Override
    public String getName() { return "customSelector"; }
}
```

- 通过 @FpSelector 注解在扩展点接口上指定选择器名称：

```java
@FpSelector("customSelector")  // 指定使用名为customSelector的选择器
public interface OrderProcessAbility extends ExtensionAbility {
    String processOrder(String orderId, String orderData);
    String version();
}
```

### 扩展点监控

```java
// 获取扩展点调用统计
ExtensionMonitor.ExtensionMetrics metrics = flexPoint.getExtensionMetrics("mall:1.0.0");
System.out.println("调用次数: " + metrics.getTotalInvocations());
System.out.println("平均耗时: " + metrics.getAverageDuration() + "ms");
```

---

## ⚙️ 配置项说明

| 配置项 | 类型 | 默认值   | 说明 |
|--------|------|-------|------|
| flexpoint.enabled | boolean | true  | 是否启用Flex Point框架 |
| flexpoint.monitor.enabled | boolean | true  | 是否启用扩展点监控功能 |
| flexpoint.monitor.log-invocation | boolean | true  | 是否记录扩展点调用日志 |
| flexpoint.monitor.log-selection | boolean | true  | 是否记录扩展点选择日志 |
| flexpoint.monitor.log-exception-details | boolean | true  | 是否记录异常详情 |
| flexpoint.monitor.performance-stats-enabled | boolean | true  | 是否启用性能统计 |
| flexpoint.monitor.async-enabled | boolean | false | 是否启用异步处理 |
| flexpoint.monitor.async-queue-size | int | 1000  | 异步处理队列大小 |
| flexpoint.monitor.async-core-pool-size | int | 2  | 异步监控核心线程数 |
| flexpoint.monitor.async-max-pool-size | int | 4  | 异步监控最大线程数 |
| flexpoint.monitor.async-keep-alive-time | long | 60  | 异步监控线程保活时间（秒） |
| flexpoint.registry.enabled | boolean | true  | 是否启用扩展点自动注册 |


> 以上配置可在 application.yml 或 application.properties 中灵活配置，详细含义见上表。

---

## 🎯 最佳实践

- **注册扩展点时只需 flexPoint.register(ability)**，无需类型和元数据。
- **查找扩展点时直接 flexPoint.findAbility(AbilityClass.class)**，框架会根据@FpSelector注解自动查找对应的选择器。
- **自定义选择器通过 @Component 注解自动注册，或者手动调用 flexPoint.registerSelector(selector) 注册。**
- **选择器的名称（getName()方法返回值）必须与@FpSelector注解中指定的名称一致。**
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
        String appCode = req.getHeader("X-App-Code");
        SysAppContext.setAppCode(appCode);
        try {
            chain.doFilter(request, response);
        } finally {
            SysAppContext.clear();
        }
    }
}
```

#### 2. 选择器注册（推荐方式）

```java
// src/main/java/com/flexpoint/example/springboot/framework/flexpoint/FlexPointConfig.java
@Component
public class FlexPointConfig {
    @Bean
    public CodeVersionSelector codeVersionSelector() {
        return new CodeVersionSelector(context -> SysAppContext.getAppCode());
    }
}
```

#### 3. 扩展点接口与实现

```java
// src/main/java/com/flexpoint/example/springboot/ext/OrderProcessAbility.java
@FpSelector("codeVersionSelector")  // 指定使用codeVersionSelector选择器
public interface OrderProcessAbility extends ExtensionAbility {
    String processOrder(String orderId, String orderData);
    String getOrderStatus(String orderId);
}

// src/main/java/com/flexpoint/example/springboot/ext/mall/MallOrderProcessAbility.java
@Component
public class MallOrderProcessAbility implements OrderProcessAbility {
    @Override public String getCode() { return "mall-app"; }
    @Override public String processOrder(String orderId, String orderData) { return "商城订单处理完成"; }
    @Override public String getOrderStatus(String orderId) { return "已支付"; }
}
```

#### 4. 控制器中注入扩展点并调用

```java
// src/main/java/com/flexpoint/example/springboot/controller/OrderController.java
@RestController
public class OrderController {
    @FpExt
    private OrderProcessAbility orderProcessAbility;

    @PostMapping("/api/v1/orders/process")
    public String processOrder(@RequestBody Map<String, String> request) {
        String orderId = request.get("orderId");
        String orderData = request.get("orderData");
        return orderProcessAbility.processOrder(orderId, orderData);
    }
}
```

#### 5. 配置文件示例

```yaml
flexpoint:
  enabled: true
  monitor:
    enabled: true
    log-invocation: true
    async-enabled: true      # 启用异步监控
    async-queue-size: 2000   # 队列大小
    async-core-pool-size: 4  # 核心线程数
    async-max-pool-size: 8   # 最大线程数
    async-keep-alive-time: 120  # 线程保活时间(秒)
  registry:
    enabled: true
```

可通过注入 `ExtensionMonitor` 获取扩展点调用统计：
```java
@Autowired
private ExtensionMonitor extensionMonitor;

ExtensionMonitor.ExtensionMetrics metrics = extensionMonitor.getExtensionMetrics(扩展点实例);
```

---

## 🤝 贡献

如果您觉得 Flex Point 有优化空间或有更好的设计思路，欢迎随时提交 PR（Pull Request）！我们鼓励社区共同完善和壮大本项目。

### 🐾 贡献代码的步骤
1. 在 GitHub 上 fork 本项目到您的个人仓库。
2. 将 fork 后的项目（即您的仓库）clone 到本地。
3. 在本地新建分支进行代码修改和优化。
4. commit 并 push 到您的远程仓库。
5. 登录 GitHub，在您的仓库首页点击 "Pull Request" 按钮，填写说明信息后提交。
6. 等待维护者 review 并合并。

### 📐 PR 遵循的原则

欢迎任何人为 Flex Point 添砖加瓦，贡献代码。

- **注释完备**：每个新增方法请按照 JavaDoc 规范标明方法说明、参数说明、返回值说明等，必要时请添加单元测试。
- **依赖规范**：新增方法尽量避免引入额外的第三方库。
- **风格统一**：请遵循项目现有代码风格和格式。

---

## 联系

如有问题或需要支持，扫码加微信(备注 flex-point)
<p>
<img src="statics/img/weixin.png" alt="oslsls" width="230px"/>
</p>

---

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

---

<p align="center">
  <strong>让扩展点开发更简单、更灵活！</strong>
</p>