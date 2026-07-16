package com.flexpoint.core.plugin;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.event.EventBus;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import com.flexpoint.core.ext.interceptor.InterceptorRegistry;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.selector.SelectorRegistry;

/**
 * 插件运行期可访问的受控上下文。
 *
 * <p>对核心内核能力进行受控暴露，避免插件直接持有全局可变状态引用，
 * 降低耦合并提升稳定性。</p>
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public interface PluginContext {
    /** 扩展点注册中心 */
    ExtAbilityRegistry extRegistry();
    /** 选择器注册表 */
    SelectorRegistry selectorRegistry();
    /** 事件总线 */
    EventBus eventBus();
    /** 监控器 */
    ExtMonitor monitor();
    /** 框架配置 */
    FlexPointConfig config();
    /** 调用拦截器注册表（行为增强类插件在此注册 around 拦截器） */
    InterceptorRegistry interceptorRegistry();
}
