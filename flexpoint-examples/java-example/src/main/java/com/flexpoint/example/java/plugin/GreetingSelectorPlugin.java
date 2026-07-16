package com.flexpoint.example.java.plugin;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import com.flexpoint.core.selector.DecisionExplanation;
import com.flexpoint.core.selector.Selector;
import com.flexpoint.core.selector.SelectorRegistry;
import com.flexpoint.example.java.context.AppContext;

import java.util.List;

/**
 * 自定义选择器插件：注册名为 {@code greetingSelector} 的选择器。
 * <p>演示：能力注册与对称反注册（支持运行期启停）。</p>
 *
 * @author xiangganluo
 */
public class GreetingSelectorPlugin extends AbstractPlugin {

    public static final String PLUGIN_ID = "example.greeting-selector";
    public static final String SELECTOR_NAME = "greetingSelector";

    private SelectorRegistry registry;

    private final Selector selector = new Selector() {
        @Override
        public <T extends ExtAbility> T select(List<T> candidates) {
            String code = AppContext.getAppCode();
            for (T c : candidates) {
                if (c.getCode().equals(code)) {
                    return c;
                }
            }
            return null;
        }

        @Override
        public String getName() {
            return SELECTOR_NAME;
        }

        @Override
        public <T extends ExtAbility> DecisionExplanation explain(List<T> candidates) {
            return DecisionExplanation.fromSelection(getName(), candidates, select(candidates));
        }
    };

    @Override
    public String getId() {
        return PLUGIN_ID;
    }

    @Override
    public void init(PluginContext context) {
        this.registry = context.selectorRegistry();
        System.out.println("[GreetingSelectorPlugin] init：上下文就绪");
    }

    @Override
    public void start() {
        registry.register(selector);
        System.out.println("[GreetingSelectorPlugin] start：已注册选择器 " + SELECTOR_NAME);
    }

    @Override
    public void stop() {
        if (registry != null) {
            registry.unregister(SELECTOR_NAME);
            System.out.println("[GreetingSelectorPlugin] stop：已反注册选择器 " + SELECTOR_NAME);
        }
    }
}
