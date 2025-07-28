package com.flexpoint.springboot.config;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.monitor.MonitorFactory;
import com.flexpoint.core.monitor.alert.AlertStrategy;
import com.flexpoint.core.monitor.handler.AlertHandler;
import com.flexpoint.core.monitor.handler.CollectorHandler;
import com.flexpoint.core.monitor.handler.MetricsHandler;
import com.flexpoint.core.monitor.handler.MonitorHandler;
import com.flexpoint.core.monitor.metrics.MetricsCollector;
import com.flexpoint.springboot.properties.FlexPointProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * FlexPoint 监控自动配置
 * 负责监控相关组件的创建
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnBean(FlexPoint.class)
public class FlexPointMonitorAutoConfiguration {

    /**
     * 创建本地指标处理器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = FlexPointProperties.PREFIX + ".monitor", name = "enabled", havingValue = "true", matchIfMissing = true)
    public MetricsHandler metricsHandler() {
        log.info("创建本地指标处理器");
        return new MetricsHandler();
    }

    /**
     * 创建收集器处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public CollectorHandler collectorHandler(List<MetricsCollector> collectors) {
        log.info("创建收集器处理器，收集器数量: {}", collectors.size());
        return new CollectorHandler(collectors);
    }

    /**
     * 创建告警处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public AlertHandler alertHandler(List<AlertStrategy> alertStrategies) {
        log.info("创建告警处理器，告警策略数量: {}", alertStrategies.size());
        return new AlertHandler(alertStrategies);
    }

    /**
     * 创建监控处理器链
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = FlexPointProperties.PREFIX + ".monitor", name = "enabled", havingValue = "true", matchIfMissing = true)
    public List<MonitorHandler> monitorHandlerChain(
            MetricsHandler metricsHandler,
            CollectorHandler collectorHandler,
            AlertHandler alertHandler) {
        
        log.info("创建监控处理器链");
        List<MonitorHandler> handlerChain = new ArrayList<>();
        
        // 添加本地指标处理器
        handlerChain.add(metricsHandler);
        
        // 添加收集器处理器（如果启用）
        if (collectorHandler != null) {
            handlerChain.add(collectorHandler);
        }
        
        // 添加告警处理器（如果启用）
        if (alertHandler != null) {
            handlerChain.add(alertHandler);
        }
        
        return handlerChain;
    }

    /**
     * 创建默认监控实现
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = FlexPointProperties.PREFIX + ".monitor", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ExtMonitor extMonitor(List<MonitorHandler> monitorHandlerChain, FlexPointProperties properties) {
        log.info("创建监控实现，处理器链长度: {}", monitorHandlerChain.size());
        return MonitorFactory.createMonitor(properties.getMonitor(), monitorHandlerChain);
    }
} 