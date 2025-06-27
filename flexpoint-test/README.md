# Flex Point 测试模块

本模块包含 Flex Point 框架的完整测试用例，验证框架的核心功能和特性。

## 测试结构

```
flexpoint-test/
├── src/test/java/com/flexpoint/test/
│   ├── FlexPointFrameworkTest.java      # 框架基础功能测试
│   ├── FlexPointCoreTest.java           # 核心功能测试
│   ├── FlexPointConfigTest.java         # 配置功能测试
│   └── ExtensionResolutionStrategyTest.java # 解析策略测试
```

## 测试覆盖范围

### 1. FlexPointFrameworkTest - 框架基础功能测试

测试 ExtensionAbilityFactory 和基础组件的功能：

- **基础扩展点注册和查找**
  - 测试扩展点的注册、查找和调用
  - 验证扩展点代码解析功能

- **代码解析功能**
  - 测试不同 code 的扩展点查找
  - 验证上下文传递和解析

- **监控功能**
  - 测试扩展点调用监控
  - 验证性能指标收集

### 2. FlexPointCoreTest - 核心功能测试

测试 FlexPoint 核心类和建造者模式：

- **建造者模式**
  - 测试 FlexPointBuilder 的各种配置方式
  - 验证组件创建和配置

- **扩展点注册**
  - 测试带元数据和无元数据的注册
  - 验证注册中心功能

- **扩展点查找**
  - 测试多种查找方式
  - 验证上下文传递

- **自定义解析策略**
  - 测试自定义解析策略的注册和使用
  - 验证策略切换功能

- **配置支持**
  - 测试配置驱动的组件创建
  - 验证配置验证功能

### 3. FlexPointConfigTest - 配置功能测试

测试配置类和配置验证功能：

- **默认配置**
  - 验证默认配置值
  - 测试配置构建器

- **配置验证**
  - 测试配置有效性验证
  - 验证边界条件处理

- **配置组件**
  - 测试监控配置
  - 测试注册中心配置

- **配置操作**
  - 测试配置复制、比较
  - 验证配置序列化

### 4. ExtensionResolutionStrategyTest - 解析策略测试

测试扩展点解析策略的各种场景：

- **默认解析策略**
  - 测试有 code 的情况
  - 测试空 code 的情况
  - 测试 null code 的情况
  - 测试没有 code 的情况
  - 测试 null context 的情况
  - 测试不存在的 code

- **自定义解析策略**
  - 测试自定义解析逻辑
  - 测试回退机制

- **边界条件**
  - 测试空扩展点列表
  - 测试 null 扩展点列表

- **抽象类测试**
  - 测试 AbstractExtensionResolutionStrategy
  - 验证策略名称功能

## 运行测试

### 运行所有测试

```bash
mvn test
```

### 运行特定测试类

```bash
# 运行框架基础功能测试
mvn test -Dtest=FlexPointFrameworkTest

# 运行核心功能测试
mvn test -Dtest=FlexPointCoreTest

# 运行配置功能测试
mvn test -Dtest=FlexPointConfigTest

# 运行解析策略测试
mvn test -Dtest=ExtensionResolutionStrategyTest
```

### 运行特定测试方法

```bash
# 运行特定测试方法
mvn test -Dtest=FlexPointFrameworkTest#testBasicExtensionRegistration
```

## 测试数据

测试中使用的示例扩展点：

### TestExtension 接口

```java
interface TestExtension extends ExtensionAbility {
    String sayHello();
}
```

### TestExtensionImpl 实现

```java
static class TestExtensionImpl implements TestExtension {
    @Override
    public String getCode() {
        return "test";
    }

    @Override
    public String sayHello() {
        return "Hello from test extension";
    }
}
```

### TestExtensionImpl2 实现

```java
static class TestExtensionImpl2 implements TestExtension {
    @Override
    public String getCode() {
        return "test2";
    }

    @Override
    public String sayHello() {
        return "Hello from test2 extension";
    }
}
```

## 测试最佳实践

1. **测试隔离**：每个测试方法都是独立的，不依赖其他测试的状态
2. **边界条件**：测试各种边界条件和异常情况
3. **配置验证**：验证配置的有效性和默认值
4. **功能覆盖**：确保所有核心功能都有对应的测试用例
5. **性能测试**：包含基本的性能验证

## 持续集成

测试模块配置了持续集成支持：

- 自动运行所有测试用例
- 生成测试覆盖率报告
- 验证代码质量指标

## 扩展测试

如需添加新的测试用例：

1. 在对应的测试类中添加新的测试方法
2. 遵循现有的测试命名规范
3. 确保测试的独立性和可重复性
4. 添加必要的注释说明测试目的 