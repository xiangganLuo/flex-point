package com.flexpoint.springboot.config;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.extension.ExtensionAbilityFactory;
import com.flexpoint.spring.processor.ExtensionAbilityReferenceProcessor;
import com.flexpoint.spring.register.SpringExtensionAbilityRegister;
import com.flexpoint.spring.register.SpringExtensionResolverRegister;
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
        // 使用core模块的FlexPointBuilder构建FlexPoint实例
        FlexPoint flexPoint = FlexPointBuilder.create(properties).build();
        
        log.info("FlexPoint实例创建成功，配置: {}", flexPoint.getConfig());
        return flexPoint;
    }

    /**
     * 创建扩展点能力工厂
     * 使用配置创建的组件
     */
    @Bean
    @ConditionalOnMissingBean
    public ExtensionAbilityFactory extensionAbilityFactory(FlexPoint flexPoint) {
        return new ExtensionAbilityFactory(flexPoint.getRegistry(), flexPoint.getMonitor(), flexPoint.getResolverFactory());
    }

    /**
     * 创建扩展点自动注册器
     * 根据配置决定是否启用
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = FlexPointProperties.PREFIX + ".registry", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SpringExtensionAbilityRegister springExtensionAbilityRegister(FlexPoint flexPoint) {
        return new SpringExtensionAbilityRegister(flexPoint.getRegistry());
    }

    /**
     * 创建解析器自动注册器
     * 根据配置决定是否启用
     */
    @Bean
    @ConditionalOnMissingBean
    public SpringExtensionResolverRegister springExtensionResolverRegister(FlexPoint flexPoint) {
        return new SpringExtensionResolverRegister(flexPoint.getResolverFactory());
    }

    /**
     * 创建扩展点引用处理器
     * 用于处理@ExtensionAbilityReference注解
     */
    @Bean
    @ConditionalOnMissingBean
    public ExtensionAbilityReferenceProcessor extensionAbilityReferenceProcessor(
            FlexPoint flexPoint) {
        return new ExtensionAbilityReferenceProcessor(flexPoint.getAbilityFactory(), flexPoint.getMonitor());
    }
} 