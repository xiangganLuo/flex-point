# Spring Boot Example

本示例演示如何在Spring Boot环境下集成和使用Flex Point扩展点框架。

## 依赖引入

```xml
<dependency>
    <groupId>com.flexpoint</groupId>
    <artifactId>flexpoint-springboot</artifactId>
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
@Component
@ExtensionInfo(id = "demo", description = "演示扩展点")
public class DemoAbilityImpl implements DemoAbility {
    @Override
    public String getCode() { return "demo"; }
    @Override
    public String hello(String name) { return "Hello, " + name; }
}
```

3. 注入并使用扩展点

```java
@Service
public class DemoService {
    @ExtensionAbilityReference(code = "demo")
    private DemoAbility demoAbility;
    public void test() {
        System.out.println(demoAbility.hello("world"));
    }
}
```

4. 启动类

```java
@SpringBootApplication
public class ExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
} 