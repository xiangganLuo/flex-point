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
    boolean hasPermission(Long userId, String action);
}
```

2. 实现扩展点

```java
@Component
@ExtensionInfo(id = "admin-permission", description = "管理员权限校验器")
public class AdminPermissionValidator implements DemoAbility {
    @Override
    public String getCode() { return "admin"; }
    @Override
    public boolean hasPermission(Long userId, String action) { return true; }
}
```

3. 注入并使用扩展点

```java
@Service
public class DemoService {
    @ExtensionAbilityReference(code = "admin")
    private DemoAbility demoAbility;
    public void checkPermission() {
        boolean has = demoAbility.hasPermission(1L, "DELETE_USER");
        System.out.println("管理员权限校验结果: " + has);
    }
}
```

4. 启动类

```java
@SpringBootApplication
public class ExampleApplication implements CommandLineRunner {
    @Autowired
    private DemoService demoService;

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Override
    public void run(String... args) {
        demoService.checkPermission();
    }
}
```

---

## 自定义解析器自动注册

Spring Boot环境下，自定义解析器会自动注册，无需手动配置。

### 1. 定义自定义解析器

```java
@Component
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

### 2. 自动注册

Spring Boot启动时会自动扫描并注册所有 `@Component` 注解的 `ExtensionResolutionStrategy` 实现。

### 3. 使用自定义解析器

```java
@ExtensionResolver("CustomExtensionResolutionStrategy")
public interface DemoAbility extends ExtensionAbility {
    boolean hasPermission(Long userId, String action);
}
```

---

## 配置说明

框架提供自动配置，无需额外配置即可使用。如需自定义配置：

```java
@Configuration
public class FlexPointConfig {
    @Bean
    public ExtensionResolverFactory customExtensionResolverFactory() {
        return new CustomExtensionResolverFactory();
    }
}
```

---

更多高级用法请参考主项目文档和源码注释。 