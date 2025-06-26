<p align="center">
  <img src="statics/img/logo.svg" alt="Flex Point Logo" width="120"/>
</p>
<h1 align="center">Flex Point 灵活扩展点框架</h1>
<p align="center">
  <strong>轻量级、极致灵活的扩展点解决方案</strong>
</p>
<p align="center">
    <a target="_blank" href="https://search.maven.org/artifact/com.oneduedata.platform/flexpoint-springboot">
        <img src="https://img.shields.io/maven-central/v/com.oneduedata.platform/flexpoint-springboot.svg?label=Maven%20Central" />
    </a>
    <a target="_blank" href='https://www.apache.org/licenses/LICENSE-2.0.html'>
        <img src='https://img.shields.io/badge/license-MIT-green.svg'/>
    </a>
    <a target="_blank" href="https://github.com/xiangganLuo/flex-point">
        <img src="https://img.shields.io/github/stars/xiangganLuo/flex-point.svg?style=social" alt="github star"/>
    </a>
</p>

---

## 📚 简介

Flex Point 是一个极致灵活、支持多业务场景的扩展点（Extension Point）框架。它允许系统根据不同的应用代码（code）动态选择不同的业务实现，实现业务逻辑的灵活扩展和隔离。支持 Spring/Spring Boot 生态，适合中大型企业级项目。

---

## 🏗️ 模块结构

```
FlexPoint/
├── flexpoint-dependencies-bom/   # 依赖版本BOM管理模块，统一管理所有依赖版本
├── flexpoint-common/             # 公共模块 - 接口、注解、常量、异常等
├── flexpoint-core/               # 核心模块 - 核心功能实现，不依赖Spring
├── flexpoint-spring/             # Spring集成模块 - Spring环境下的集成
├── flexpoint-springboot/         # Spring Boot自动配置模块
└── flexpoint-test/               # 测试模块 - 测试用例和示例
```

| 模块名                        | 说明                                   |
|------------------------------|----------------------------------------|
| flexpoint-dependencies-bom    | 依赖版本BOM管理，所有依赖版本统一配置   |
| flexpoint-common              | 公共基础模块，接口、注解、常量、异常等   |
| flexpoint-core                | 核心实现模块，扩展点注册/查找/缓存/监控 |
| flexpoint-spring              | Spring集成，自动扫描注册扩展点           |
| flexpoint-springboot          | Spring Boot自动配置，开箱即用           |
| flexpoint-test                | 测试模块，测试用例和示例                |
| flexpoint-examples            | 多场景接入示例模块（Spring、Spring Boot、Java原生） |

---

## 📦 安装

### 🍊 Maven

在项目的 `pom.xml` 的 dependencies 中加入以下内容：

```xml
<dependency>
    <groupId>com.oneduedata.platform</groupId>
    <artifactId>flexpoint-springboot</artifactId>
    <version>${revision}</version>
</dependency>
```

> 推荐业务项目通过 BOM 方式统一依赖版本：
>
> ```xml
> <dependencyManagement>
>   <dependency>
>     <groupId>com.oneduedata.platform</groupId>
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
- [示例代码](flexpoint-test)
- [多场景接入示例（Spring/Spring Boot/Java原生）](flexpoint-examples/README.md)
- [迁移指南](#迁移指南)

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

---

## ✨ 核心特性

- 🧩 **自动注册**：Spring 启动时自动扫描和注册扩展点
- 🧠 **智能解析**：根据 code 自动选择对应实现
- 🚀 **缓存优化**：缓存扩展点实例，提高性能
- 📊 **监控统计**：记录调用次数、成功率、响应时间等指标
- 🛡️ **异常处理**：完善的异常处理和降级机制
- 🏷️ **元数据管理**：支持扩展点的版本、优先级等元数据

---

## 🛠️ 高级用法

### 自定义解析策略

```java
@Component
public class CustomResolutionStrategy implements ExtensionResolutionStrategy {
    @Override
    public <T extends ExtensionAbility> T resolve(List<T> extensions, Map<String, Object> context) {
        // 自定义解析逻辑
        return extensions.get(0);
    }
    @Override
    public String getStrategyName() {
        return "CustomResolutionStrategy";
    }
}
```

### 缓存与监控

```java
@Service
public class CacheService {
    @Autowired
    private ExtensionAbilityFactory factory;
    public void invalidateCache() {
        factory.invalidateCache(OrderStatusValidator.class);
    }
    public void getCacheStats() {
        var stats = factory.getCacheStatistics();
        System.out.println("缓存命中率: " + stats.getHitRate());
    }
}
```

```java
@Service
public class MonitorService {
    @Autowired
    private ExtensionAbilityFactory factory;
    public void getMetrics() {
        var metrics = factory.getExtensionMetrics("mall-validator");
        System.out.println("总调用次数: " + metrics.getTotalInvocations());
        System.out.println("成功率: " + metrics.getSuccessRate());
        System.out.println("平均响应时间: " + metrics.getAverageResponseTime());
    }
}
```

---

## ⚙️ 配置说明

### Spring Boot 自动配置

框架提供自动配置，无需额外配置即可使用。如需自定义配置：

```java
@Configuration
public class FlexPointConfig {
    @Bean
    public ExtensionRegistry customExtensionRegistry() {
        return new CustomExtensionRegistry();
    }
}
```

### 非Spring环境使用

```java
// 手动创建组件
DefaultExtensionRegistry registry = new DefaultExtensionRegistry();
DefaultExtensionCacheManager cacheManager = new DefaultExtensionCacheManager();
DefaultExtensionMonitor monitor = new DefaultExtensionMonitor();
ExtensionAbilityFactory factory = new ExtensionAbilityFactory(registry, cacheManager, monitor);
// 手动注册扩展点
TestExtensionImpl extension = new TestExtensionImpl();
registry.register(TestExtension.class, extension, null);
// 使用扩展点
TestExtension found = factory.findAbility(TestExtension.class);
```

---

## 🏆 最佳实践

- 扩展点接口职责单一，功能明确
- 使用 `@ExtensionInfo` 注解提供元数据信息
- 实现类应无状态，避免实例变量
- 优先使用注解注入，代码更简洁
- 合理使用缓存，监控扩展点调用情况
- 记录详细错误日志，合理处理未找到扩展点的情况

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request 来改进框架。

### 贡献步骤

1. Fork 本项目到自己的仓库
2. Clone 到本地进行开发
3. 提交代码并 Push 到你的仓库
4. 在 GitHub 上发起 Pull Request

### PR 规范

- 注释完备，方法应有 JavaDoc
- 尽量不引入额外三方库
- 新增功能建议补充单元测试