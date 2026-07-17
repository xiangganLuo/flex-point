# 快速开始

本节演示在 Spring Boot 项目中，用 Flex Point 实现「同一能力多套实现、按请求上下文动态选中」的最短链路。

## 1. 引入依赖

在 `pom.xml` 中加入 Spring Boot 接入模块：

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>flexpoint-springboot</artifactId>
    <version>2.0.0</version>
</dependency>
```

::: tip 推荐用 BOM 统一版本
通过 BOM 统一管理依赖版本，业务侧无需重复写 `<version>`：

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.xiangganluo</groupId>
      <artifactId>flexpoint-dependencies-bom</artifactId>
      <version>2.0.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```
:::

官方选择器已独立为插件模块，按需引入（本例使用 code + version 选择器）：

```xml
<dependency>
    <groupId>io.github.xiangganluo</groupId>
    <artifactId>flexpoint-plugin-selector-code-version</artifactId>
    <version>2.0.0</version>
</dependency>
```

## 2. 定义扩展点接口

扩展点接口继承 `ExtAbility`，并用 `@FpSelector` 声明使用哪个选择器：

```java
import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.ext.ExtAbility;

@FpSelector("codeVersionSelector") // 选择器名称，与 Selector.getName() 一致
public interface OrderProcessAbility extends ExtAbility {
    String processOrder(String orderId);
}
```

## 3. 编写多套实现（自动注册）

实现类标注 `@Component` 即可被自动扫描注册。`getCode()` 用于区分业务实现，`getTags()` 可携带版本等元数据：

```java
import com.flexpoint.core.ext.ExtTags;
import org.springframework.stereotype.Component;

@Component
public class MallOrderProcessAbility implements OrderProcessAbility {
    @Override
    public String getCode() { return "mall"; }

    @Override
    public String processOrder(String orderId) { return "商城订单处理完成"; }
}

@Component
public class MallOrderProcessAbilityV2 implements OrderProcessAbility {
    @Override
    public String getCode() { return "mall"; }

    @Override
    public ExtTags getTags() { return ExtTags.builder().set("version", "2.0.0").build(); }

    @Override
    public String processOrder(String orderId) { return "商城订单处理完成 V2"; }
}
```

## 4. 注册选择器

官方 code + version 选择器以插件方式注册。业务方只需实现 `CodeVersionResolver`，从**自己维护的来源**解析 code / version（框架不再提供请求上下文）。本例的 `AppRequestHolder` 是业务方自定义的请求持有者：

```java
import com.flexpoint.plugin.selector.codeversion.CodeVersionSelector;
import com.flexpoint.plugin.selector.codeversion.CodeVersionSelectorPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlexPointConfig {

    @Bean
    public CodeVersionSelectorPlugin codeVersionSelectorPlugin(AppRequestHolder holder) {
        return new CodeVersionSelectorPlugin(new CodeVersionSelector.CodeVersionResolver() {
            @Override
            public String resolveCode() { return holder.getAppCode(); }

            @Override
            public String resolveVersion() { return holder.getAppVersion(); }
        });
    }
}
```

## 5. 由业务方维护路由数据来源

选择器所需的 code / version 由业务方自己维护——框架不再提供任何请求上下文。以 Web 过滤器把请求头写入业务自定义的请求持有者为例，Resolver 再从该持有者读取：

```java
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AppRequestFilter implements Filter {
    private final AppRequestHolder holder; // 业务方自定义的请求持有者（如基于 ThreadLocal）

    public AppRequestFilter(AppRequestHolder holder) { this.holder = holder; }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        holder.setAppCode(request.getHeader("X-App-Code"));
        holder.setAppVersion(request.getHeader("X-App-Version"));
        try {
            chain.doFilter(req, resp);
        } finally {
            holder.clear(); // 线程池复用，务必清理
        }
    }
}
```

## 6. 注入并调用

业务代码用 `@FpExt` 注入扩展点接口，直接调用即可 —— 框架在每次调用时按选择器选中匹配实现：

```java
import com.flexpoint.common.annotations.FpExt;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    @FpExt
    private OrderProcessAbility orderProcessAbility;

    @GetMapping("/order/process")
    public String process(String orderId) {
        return orderProcessAbility.processOrder(orderId);
    }
}
```

请求头 `X-App-Code: mall`、`X-App-Version: 2.0.0` 时，将命中 `MallOrderProcessAbilityV2`。

## 下一步

- 理解模型：[核心概念](/guide/concepts)
- 深入扩展点：[扩展点](/guide/ext)
- 路由与排障：[选择器](/guide/selector)
- 观测调用：[可观测](/guide/observability)
