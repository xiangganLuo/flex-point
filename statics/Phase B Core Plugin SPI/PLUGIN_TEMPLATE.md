# 最小插件模板（极简模型）

> 复制下面的骨架，改 `getId()` 与逻辑即可开始。配合 [ONE_PAGER.md](./ONE_PAGER.md) 阅读。

---

## 1. 骨架：继承 `AbstractPlugin`（推荐）

`AbstractPlugin` 提供四个生命周期方法的空实现，按需覆写；`getId()` 需自行实现。

```java
package com.example.plugin;

import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;

/**
 * 示例插件：在 start 阶段注册一个自定义能力，在 stop 阶段对称反注册。
 */
public class MyPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "example.my-plugin";

    private PluginContext ctx;

    @Override
    public String getId() {
        return PLUGIN_ID;
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
        this.ctx = null;
    }
}
```

## 2. 一个可注册选择器的插件

```java
public class MySelectorPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "example.selector";
    private static final String SELECTOR_NAME = "mySelector";

    private SelectorRegistry registry;

    @Override public String getId() { return PLUGIN_ID; }

    @Override public void init(PluginContext ctx) { this.registry = ctx.selectorRegistry(); }

    @Override public void start() {
        registry.register(new Selector() {
            @Override public <T extends ExtAbility> T select(List<T> candidates) {
                return candidates.isEmpty() ? null : candidates.get(0);
            }
            @Override public String getName() { return SELECTOR_NAME; }
            @Override public <T extends ExtAbility> DecisionExplanation explain(List<T> candidates) {
                return DecisionExplanation.fromSelection(getName(), candidates, select(candidates));
            }
        });
    }

    @Override public void stop() {
        if (registry != null) registry.unregister(SELECTOR_NAME);
    }
}
```

> 注意：选择器名是「资源名」，全局禁止同名覆盖，重复注册会直接抛异常（对应插件被降级为 FAILED）。

## 3. 装配到 FlexPoint（装配顺序 = 注册顺序）

```java
FlexPoint fp = FlexPointBuilder.create()
        .withPlugin(new MySelectorPlugin())
        .build();
```

## 4. 最小自测脚手架

直接构造 `DefaultPluginManager` 精确验证装配 / 状态流转（无需完整 FlexPoint）：

```java
FlexPointConfig config = FlexPointConfig.defaultConfig();
EventDispatcher dispatcher = new EventDispatcher(new DefaultEventBus());
ExtAbilityRegistry registry = new DefaultExtAbilityRegistry(config.getRegistry(), dispatcher);
SelectorRegistry selectorRegistry = new DefaultSelectorRegistry(dispatcher);
ExtMonitor monitor = MonitorFactory.createDefaultMonitor(config.getMonitor());

DefaultPluginManager pm = new DefaultPluginManager(
        registry, selectorRegistry, dispatcher.getEventBus(), monitor, config);
pm.register(new MySelectorPlugin());
pm.installAll();

assert pm.getPluginStates().get("example.selector") == PluginState.STARTED;
assert selectorRegistry.has("mySelector");

pm.stopAll(); // 逆序 stop + destroy
```

> 完整用例参考 `flexpoint-test` 下的 `plugin/PluginManagerBasicsTest`、`plugin/PluginLifecycleTest`、
> `plugin/PluginContextBoundaryTest`；可运行示例见 `flexpoint-examples/java-example` 的 `PluginExampleMain`。

## 5. 常见坑

| 现象 | 原因 | 处理 |
|------|------|------|
| 注册期抛 `Duplicate pluginId` | `getId()` 冲突 | 改为唯一 ID |
| 注册期抛 `pluginId must not be empty` | `getId()` 返回空 | 返回非空稳定标识 |
| 插件被标记 `FAILED` 但构建成功 | 该插件 start 抛异常（统一降级） | 查 `getLoadReport().getErrors()` |
| 选择器注册抛「名称已存在」 | 资源名同名覆盖 | 改选择器名，或确认只装配一次 |
| 装配顺序不符预期 | 顺序即注册顺序 | 调整 `withPlugin`/`withPlugins` 传入次序 |
