package com.flexpoint.core.monitor;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.handler.MonitorHandler;

import java.util.Map;

/**
 * 扩展点监控接口
 * 提供扩展点调用统计、性能监控和异常监控
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public interface ExtMonitor {
    
    /**
     * 记录扩展点调用
     *
     * @param extAbility 扩展点能力实例
     * @param duration 调用耗时（毫秒）
     * @param success 是否成功
     */
    void recordInvocation(ExtAbility extAbility, long duration, boolean success);

    /**
     * 记录扩展点异常
     *
     * @param extAbility 扩展点能力实例
     * @param exception 异常信息
     */
    void recordException(ExtAbility extAbility, Throwable exception);
    
    /**
     * 获取扩展点指标
     *
     * @param extAbility 扩展点能力实例
     * @return 扩展点指标
     */
    ExtMetrics getExtMetrics(ExtAbility extAbility);
    
    /**
     * 获取所有扩展点指标
     *
     * @return 所有扩展点指标，key为扩展点标识符
     */
    Map<String, ExtMetrics> getAllExtMetrics();

    /**
     * 添加责任链节点
     */
    void addHandler(MonitorHandler handler);

    /**
     * 移除责任链节点
     */
    void removeHandler(MonitorHandler handler);

    /**
     * 清空责任链
     */
    void clearHandlers();

    /**
     * 获取监控配置
     * @return 配置对象
     */
    FlexPointConfig.MonitorConfig getConfig();

} 