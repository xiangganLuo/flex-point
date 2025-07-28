package com.flexpoint.springboot.config;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.spring.register.FlexPointSpringExtAbilityRegister;
import com.flexpoint.spring.register.FlexPointSpringSelectorRegister;
import com.flexpoint.springboot.properties.FlexPointProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FlexPoint 注册自动配置
 * 负责扩展点和选择器的自动注册
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnBean(FlexPoint.class)
public class FlexPointRegistryAutoConfiguration {

    /**
     * 创建扩展点自动注册器
     * 根据配置决定是否启用
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = FlexPointProperties.PREFIX + ".registry", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FlexPointSpringExtAbilityRegister springExtAbilityRegister(FlexPoint flexPoint) {
        log.info("创建扩展点自动注册器");
        return new FlexPointSpringExtAbilityRegister(flexPoint);
    }

    /**
     * 创建选择器自动注册器
     * 根据配置决定是否启用
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = FlexPointProperties.PREFIX + ".registry", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FlexPointSpringSelectorRegister springSelectorRegister(FlexPoint flexPoint) {
        log.info("创建选择器自动注册器");
        return new FlexPointSpringSelectorRegister(flexPoint);
    }
} 