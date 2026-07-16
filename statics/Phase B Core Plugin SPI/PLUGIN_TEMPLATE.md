# 最小插件模板

> 复制下面的骨架，改名与元数据即可开始。配合 [ONE_PAGER.md](./ONE_PAGER.md) 阅读。

---

## 1. 骨架：继承 `AbstractPlugin`（推荐）

`AbstractPlugin` 提供四个生命周期方法的空实现，按需覆写即可。

```java
package com.example.plugin;

import com.flexpoint.core.plugin.*;

import java.util.EnumSet;

/**
 * 示例插件：在 start 阶段注册一个自定义能力，在 stop 阶段对称反注册。
 */
public class MyPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "example.my-plugin";

    private final PluginDescriptor descriptor = PluginDescriptor.builder(PLUGIN_ID, "1.0.0")
            .capabilities(EnumSet.of(PluginCapability.OTHER))
            .order(50)
            .critical(false)
            .build();

    private PluginContext ctx;

    @Override
    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void init(PluginContext context) {
        // 读取配置、准备资源。仅通过 context 访问内核能力。
        this.ctx = context;
    }

    @Override
    public void start() {
        // 注册能力，例如：ctx.selectorRegistry().register(new MySelector());
    }

    @Override
    public void stop() {
        // 与 start 对称反注册，例如：ctx.selectorRegistry().unregister("mySelector");
    }

    @Override
    public void destroy() {
        // 释放资源、置空引用
        this.ctx = null;
    }
}
```

## 2. 声明依赖

```java
import java.util.Collections;

private final PluginDescriptor descriptor = PluginDescriptor.builder(PLUGIN_ID, "1.0.0")
        .capabilities(EnumSet.of(PluginCapability.OTHER))
        .dependencies(Collections.singletonList(
                new PluginDependency("example.base-plugin", null))) // 依赖先装配
        .order(50)
        .build();
```

## 3. 一个可注册选择器的插件

```java
public class MySelectorPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "example.selector";
    private static final String SELECTOR_NAME = "mySelector";

    private final PluginDescriptor descriptor = PluginDescriptor.builder(PLUGIN_ID, "1.0.0")
            .capabilities(EnumSet.of(PluginCapability.SELECTOR))
            .order(10)
            .build();

    private SelectorRegistry registry;

    @Override public PluginDescriptor getDescriptor() { return descriptor; }

    @Override public void init(PluginContext ctx) { this.registry = ctx.selectorRegistry(); }

    @Override public void start() {
        registry.register(new Selector() {
            @Override public <T extends ExtAbility> T select(List<T> candidates) {
                // 你的路由逻辑
                return candidates.isEmpty() ? null : candidates.get(0);
            }
            @Override public String getName() { return SELECTOR_NAME; }
        });
    }

    @Override public void stop() {
        if (registry != null) registry.unregister(SELECTOR_NAME);
    }
}
```

> 注意：选择器名是「资源名」，全局禁止同名覆盖。重复注册会直接抛异常。

## 4. 装配到 FlexPoint

```java
FlexPoint fp = FlexPointBuilder.create()
        .withPlugin(new MySelectorPlugin())
        .build();
```

## 5. 最小自测脚手架

直接构造 `DefaultPluginManager` 精确验证装配 / 依赖 / 状态流转（无需完整 FlexPoint）：

```java
FlexPointConfig config = FlexPointConfig.defaultConfig();
EventDispatcher dispatcher = new EventDispatcher(new DefaultEventBus());
ExtAbilityRegistry registry = new DefaultExtAbilityRegistry(config.getRegistry(), dispatcher);
SelectorRegistry selectorRegistry = new DefaultSelectorRegistry(dispatcher);
ExtMonitor monitor = MonitorFactory.createDefaultMonitor(config.getMonitor());

DefaultPluginManager pm = new DefaultPluginManager(
        registry, selectorRegistry, dispatcher.getEventBus(), monitor, config);
pm.register(new MySelectorPlugin());
pm.resolve();
pm.installAll();

assert pm.getPluginStates().get("example.selector") == PluginState.STARTED;
assert selectorRegistry.has("mySelector");

pm.stopAll(); // 逆序 stop + destroy
```

> 完整用例可参考 `flexpoint-test` 下的 `plugin/DependencyAndConflictTest`、
> `plugin/PluginLifecycleTest`、`plugin/PluginContextBoundaryTest`。

## 6. 常见坑

| 现象 | 原因 | 处理 |
|------|------|------|
| 构建期抛 `PluginDependencyException: Missing dependency` | 依赖的 pluginId 未注册 | 确认依赖插件一并装配 |
| 构建期抛 `Cyclic dependency detected` | 依赖成环 | 拆解环、引入中间层 |
| 构建期抛 `Duplicate pluginId` | ID 冲突 | 改为唯一 ID |
| 插件被标记 `FAILED` 但构建成功 | 非关键插件 start 抛异常 | 查 `getLoadReport().getErrors()` |
| 选择器注册抛「名称已存在」 | 资源名同名覆盖 | 改选择器名，或确认只装配一次 |
| 关键插件失败导致构建中断 | `critical=true` 且 start 抛异常 | 修复启动逻辑或下调关键性 |
