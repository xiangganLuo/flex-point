package com.flexpoint.springboot.config;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.event.EventBus;
import com.flexpoint.core.monitor.subscribers.MonitorEventSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FlexPoint 事件自动配置
 * 负责事件总线和相关组件的创建
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnBean(FlexPoint.class)
public class FlexPointEventAutoConfiguration {

    /**
     * 创建事件总线
     * 负责事件的发布和订阅管理
     */
    @Bean
    @ConditionalOnMissingBean
    public EventBus eventBus(FlexPoint flexPoint) {
        log.info("创建事件总线");
        EventBus eventBus = flexPoint.getEventBus();
        
        // 注册监控事件订阅者
        eventBus.subscribe(new MonitorEventSubscriber(flexPoint.getExtMonitor()));
        
        return eventBus;
    }
}
