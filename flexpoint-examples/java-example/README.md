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
    String hello(String name);
}
```

2. 实现扩展点

```java
public class DemoAbilityImpl implements DemoAbility {
    @Override
    public String getCode() { return "demo"; }
    @Override
    public String hello(String name) { return "Hello, " + name; }
}
```

3. 注册与使用扩展点

```java
DefaultExtensionRegistry registry = new DefaultExtensionRegistry();
DefaultExtensionCacheManager cacheManager = new DefaultExtensionCacheManager();
DefaultExtensionMonitor monitor = new DefaultExtensionMonitor();
ExtensionAbilityFactory factory = new ExtensionAbilityFactory(registry, cacheManager, monitor);

DemoAbilityImpl demo = new DemoAbilityImpl();
registry.register(DemoAbility.class, demo, null);
DemoAbility found = factory.findAbility(DemoAbility.class);
System.out.println(found.hello("world"));
``` 