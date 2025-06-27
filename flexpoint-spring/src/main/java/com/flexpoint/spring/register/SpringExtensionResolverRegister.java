package com.flexpoint.spring.register;

import com.flexpoint.core.resolution.AbstractExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ExtensionResolverFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * Spring扩展点解析器注册器
 * 自动扫描和注册所有扩展点解析策略实现
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SpringExtensionResolverRegister implements ApplicationContextAware {

    private final ExtensionResolverFactory extensionResolverFactory;
    private ApplicationContext applicationContext;

    @PostConstruct
    public void initResolvers() {
        log.info("开始扫描和注册扩展点解析器...");

        Map<String, ExtensionResolutionStrategy> beans = applicationContext.getBeansOfType(ExtensionResolutionStrategy.class);
        
        int registeredCount = 0;
        for (Map.Entry<String, ExtensionResolutionStrategy> entry : beans.entrySet()) {
            ExtensionResolutionStrategy resolver = entry.getValue();
            String beanName = entry.getKey();
            
            // 跳过默认解析器，避免重复注册
            if (resolver.getClass().getSimpleName().equals(AbstractExtensionResolutionStrategy.DEFAULT_RESOLVER_NAME)) {
                log.debug("跳过默认解析器: {}", beanName);
                continue;
            }
            
            // 注册解析器
            extensionResolverFactory.registerResolver(resolver);
            registeredCount++;
            
            log.debug("解析器注册成功: name={}, strategy={}, bean={}", 
                    resolver.getStrategyName(), resolver.getClass().getSimpleName(), beanName);
        }
        
        log.info("解析器扫描完成，共注册 {} 个自定义解析器", registeredCount);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
} 