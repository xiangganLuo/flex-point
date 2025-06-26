# Java Example

本示例演示如何在Java原生（非Spring）环境下集成和使用Flex Point扩展点框架。

## 依赖引入

```xml
<dependency>
    <groupId>com.flexpoint</groupId>
    <artifactId>flexpoint-core</artifactId>
</dependency>
```

## 主要代码示例

1. 定义扩展点接口

```java
public interface DemoAbility extends ExtensionAbility {
    double discount(double price);
}
```

2. 实现扩展点

```java
public class MallDiscountAbility implements DemoAbility {
    @Override
    public String getCode() { return "mall"; }
    @Override
    public double discount(double price) { return price * 0.9; }
}
```

3. 注册与使用扩展点

```java
FlexPoint flexPoint = FlexPointBuilder.create().build();
flexPoint.register(DemoAbility.class, new MallDiscountAbility());
DemoAbility found = flexPoint.findAbility(DemoAbility.class);
double price = 100.0;
System.out.println("原价: " + price + ", 折扣价: " + found.discount(price));
```

---

## 注册自定义解析器

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

---

## 进阶：多策略动态切换

你可以为不同扩展点接口通过注解指定解析策略：

```java
@ExtensionResolver("CustomExtensionResolutionStrategy")
public interface DemoAbility extends ExtensionAbility {
    ...
}
```

---

更多高级用法请参考主项目文档和源码注释。 