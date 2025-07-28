package com.flexpoint.springboot.config;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.spring.processor.ExtAbilityProcessor;
import com.flexpoint.springboot.properties.FlexPointProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FlexPoint 处理器自动配置
 * 负责扩展点处理器的创建
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnBean(FlexPoint.class)
public class FlexPointProcessorAutoConfiguration {

    /**
     * 创建扩展点引用处理器
     * 用于处理@FpExt注解
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = FlexPointProperties.PREFIX + ".processor", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ExtAbilityProcessor extAbilityProcessor(FlexPoint flexPoint) {
        log.info("创建扩展点引用处理器");
        return new ExtAbilityProcessor(flexPoint);
    }
} 