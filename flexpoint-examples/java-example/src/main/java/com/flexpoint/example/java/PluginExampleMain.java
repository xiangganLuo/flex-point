package com.flexpoint.example.java;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.example.java.context.AppContext;
import com.flexpoint.example.java.plugin.BasePlugin;
import com.flexpoint.example.java.plugin.FaultyOptionalPlugin;
import com.flexpoint.example.java.plugin.GreetingAbility;
import com.flexpoint.example.java.plugin.GreetingSelectorPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义插件示例（Phase B F2）。
 *
 * <p>演示：
 * <ul>
 *   <li>装配顺序 = 注册顺序：按传入顺序 init/start；</li>
 *   <li>降级：FaultyOptionalPlugin 启动失败不阻断构建，其它插件仍可用；</li>
 *   <li>加载报告：可观测装配顺序与各插件状态；</li>
 *   <li>运行期启停：disable/enable 选择器插件动态摘挂能力。</li>
 * </ul>
 *
 * @author xiangganluo
 */
public class PluginExampleMain {

    public static void main(String[] args) {
        System.out.println("=== Flex Point 自定义插件示例 ===\n");

        // 装配顺序 = 注册顺序
        List<Plugin> plugins = new ArrayList<>();
        plugins.add(new BasePlugin());
        plugins.add(new GreetingSelectorPlugin());
        plugins.add(new FaultyOptionalPlugin());

        FlexPoint flexPoint = FlexPointBuilder.create().withPlugins(plugins).build();

        // 注册扩展点实现
        flexPoint.register(new com.flexpoint.example.java.plugin.impl.EnGreeting());
        flexPoint.register(new com.flexpoint.example.java.plugin.impl.ZhGreeting());

        // 加载报告：装配顺序与状态
        System.out.println("\n-- 加载报告 --");
        System.out.println("装配顺序: " + flexPoint.getPluginLoadReport().getOrderedPluginIds());
        System.out.println("插件状态: " + flexPoint.getPluginStates());
        System.out.println("失败原因: " + flexPoint.getPluginLoadReport().getErrors());

        // 按上下文路由
        System.out.println("\n-- 按上下文选择扩展点 --");
        AppContext.setAppCode("en");
        System.out.println("en -> " + flexPoint.findAbility(GreetingAbility.class).greet());
        AppContext.setAppCode("zh");
        System.out.println("zh -> " + flexPoint.findAbility(GreetingAbility.class).greet());
        AppContext.clear();

        // 运行期停用选择器插件后，能力不可用（降级为选择器缺失）
        System.out.println("\n-- 运行期停用选择器插件 --");
        flexPoint.disablePlugin(GreetingSelectorPlugin.PLUGIN_ID);
        System.out.println("选择器是否存在: " + flexPoint.hasSelector(GreetingSelectorPlugin.SELECTOR_NAME));

        // 重新启用后恢复
        System.out.println("\n-- 运行期重新启用 --");
        flexPoint.enablePlugin(GreetingSelectorPlugin.PLUGIN_ID);
        AppContext.setAppCode("en");
        System.out.println("en -> " + flexPoint.findAbility(GreetingAbility.class).greet());
        AppContext.clear();

        // 关闭：逆序停止插件
        flexPoint.shutdown();
        System.out.println("\n=== 示例运行完成 ===");
    }
}
