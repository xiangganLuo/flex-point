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
└── flexpoint-examples/           # 多场景接入示例模块 - Spring、Spring Boot、Java原生
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