package com.flexpoint.spring.config;

import com.flexpoint.core.ExtensionAbilityFactory;
import com.flexpoint.core.cache.DefaultExtensionCacheManager;
import com.flexpoint.core.cache.ExtensionCacheManager;
import com.flexpoint.core.monitor.DefaultExtensionMonitor;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.registry.DefaultExtensionRegistry;
import com.flexpoint.core.registry.ExtensionRegistry;
import com.flexpoint.core.resolution.DefaultExtensionResolverFactory;
import com.flexpoint.core.resolution.ExtensionResolverFactory;
import com.flexpoint.spring.processor.ExtensionAbilityReferenceProcessor;
import com.flexpoint.spring.register.SpringExtensionAbilityRegister;
import com.flexpoint.spring.register.SpringExtensionResolverRegister;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring环境下的Flex Point框架配置
 * 为非Spring Boot环境提供统一的注册机制
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Configuration
@ComponentScan(basePackages = {
    "com.flexpoint.spring.processor",
    "com.flexpoint.spring.register",
    "com.flexpoint.spring.proxy"
})
public class FlexPointSpringConfiguration {

    /**
     * 扩展点注册表
     */
    @Bean
    public ExtensionRegistry extensionRegistry() {
        return new DefaultExtensionRegistry();
    }

    /**
     * 扩展点缓存管理器
     */
    @Bean
    public ExtensionCacheManager extensionCacheManager() {
        return new DefaultExtensionCacheManager();
    }

    /**
     * 扩展点监控器
     */
    @Bean
    public ExtensionMonitor extensionMonitor() {
        return new DefaultExtensionMonitor();
    }

    /**
     * 扩展点解析器工厂
     */
    @Bean
    public ExtensionResolverFactory extensionResolverFactory() {
        return new DefaultExtensionResolverFactory();
    }

    /**
     * 扩展点工厂
     */
    @Bean
    public ExtensionAbilityFactory extensionAbilityFactory(
            ExtensionRegistry extensionRegistry,
            ExtensionCacheManager extensionCacheManager,
            ExtensionMonitor extensionMonitor) {
        return new ExtensionAbilityFactory(extensionRegistry, extensionCacheManager, extensionMonitor);
    }

    /**
     * Spring扩展点注册器
     */
    @Bean
    public SpringExtensionAbilityRegister springExtensionAbilityRegister(ExtensionRegistry extensionRegistry) {
        return new SpringExtensionAbilityRegister(extensionRegistry);
    }

    /**
     * Spring扩展点解析器注册器
     */
    @Bean
    public SpringExtensionResolverRegister springExtensionResolverRegister(ExtensionResolverFactory extensionResolverFactory) {
        return new SpringExtensionResolverRegister(extensionResolverFactory);
    }

    /**
     * 扩展点引用处理器
     */
    @Bean
    public ExtensionAbilityReferenceProcessor extensionAbilityReferenceProcessor(
            ExtensionAbilityFactory extensionAbilityFactory,
            ExtensionMonitor extensionMonitor) {
        return new ExtensionAbilityReferenceProcessor(extensionAbilityFactory, extensionMonitor);
    }
} 