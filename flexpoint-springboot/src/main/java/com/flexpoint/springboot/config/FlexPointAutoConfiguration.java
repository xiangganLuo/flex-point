package com.flexpoint.springboot.config;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.spring.banner.FlexPointBanner;
import com.flexpoint.spring.processor.ExtensionAbilityProcessor;
import com.flexpoint.spring.register.SpringExtensionAbilityRegister;
import com.flexpoint.spring.register.SpringExtensionSelectorRegister;
import com.flexpoint.springboot.properties.FlexPointProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 扩展点框架自动配置
 * 通过配置进行构建，使用组件工厂创建组件
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = FlexPointProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FlexPointProperties.class)
public class FlexPointAutoConfiguration {

    /**
     * 创建FlexPoint实例
     * 使用core模块的FlexPointBuilder，通过配置进行构建
     */
    @Bean
    @ConditionalOnMissingBean
    public FlexPoint flexPoint(FlexPointProperties properties) {
        log.info("创建FlexPoint实例，使用配置: enabled={}", properties.isEnabled());
        return FlexPointBuilder.create(properties).build();
    }

    /**
     * 创建扩展点自动注册器
     * 根据配置决定是否启用
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = FlexPointProperties.PREFIX + ".registry", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SpringExtensionAbilityRegister springExtensionAbilityRegister(FlexPoint flexPoint) {
        return new SpringExtensionAbilityRegister(flexPoint.getExtensionAbilityRegistry());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = FlexPointProperties.PREFIX, name = "bannerPrint", havingValue = "true", matchIfMissing = true)
    public FlexPointBanner bannerPrint() {
        return new FlexPointBanner();
    }

    /**
     * 创建选择器自动注册器
     * 根据配置决定是否启用
     */
    @Bean
    @ConditionalOnMissingBean
    public SpringExtensionSelectorRegister springExtensionSelectorRegister(FlexPoint flexPoint) {
        return new SpringExtensionSelectorRegister(flexPoint);
    }

    /**
     * 创建扩展点引用处理器
     * 用于处理@Extension注解
     */
    @Bean
    @ConditionalOnMissingBean
    public ExtensionAbilityProcessor extensionAbilityProcessor(FlexPoint flexPoint) {
        return new ExtensionAbilityProcessor(flexPoint);
    }
} 