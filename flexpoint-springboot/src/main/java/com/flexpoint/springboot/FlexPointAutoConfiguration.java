package com.flexpoint.springboot;

import com.flexpoint.spring.processor.ExtensionAbilityReferenceProcessor;
import com.flexpoint.spring.register.SpringExtensionAbilityRegister;
import com.flexpoint.core.ExtensionAbilityFactory;
import com.flexpoint.core.cache.DefaultExtensionCacheManager;
import com.flexpoint.core.cache.ExtensionCacheManager;
import com.flexpoint.core.monitor.DefaultExtensionMonitor;
import com.flexpoint.core.monitor.ExtensionMonitor;
import com.flexpoint.core.registry.DefaultExtensionRegistry;
import com.flexpoint.core.registry.ExtensionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 扩展点框架自动配置
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Configuration
public class FlexPointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExtensionRegistry extensionRegistry() {
        return new DefaultExtensionRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExtensionCacheManager extensionCacheManager() {
        return new DefaultExtensionCacheManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExtensionMonitor extensionMonitor() {
        return new DefaultExtensionMonitor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExtensionAbilityFactory ExtensionAbilityFactory(
            ExtensionRegistry extensionRegistry,
            ExtensionCacheManager extensionCacheManager,
            ExtensionMonitor extensionMonitor) {
        return new ExtensionAbilityFactory(extensionRegistry, extensionCacheManager, extensionMonitor);
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringExtensionAbilityRegister springExtensionAbilityRegister(ExtensionRegistry extensionRegistry) {
        return new SpringExtensionAbilityRegister(extensionRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExtensionAbilityReferenceProcessor ExtensionAbilityReferenceProcessor(
            ExtensionAbilityFactory ExtensionAbilityFactory,
            ExtensionMonitor extensionMonitor) {
        return new ExtensionAbilityReferenceProcessor(ExtensionAbilityFactory, extensionMonitor);
    }
} 