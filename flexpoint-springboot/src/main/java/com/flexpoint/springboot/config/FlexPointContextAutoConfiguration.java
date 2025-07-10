package com.flexpoint.springboot.config;

import com.flexpoint.core.context.ContextProvider;
import com.flexpoint.core.context.providers.ParameterContextProvider;
import com.flexpoint.core.context.providers.SystemPropertyContextProvider;
import com.flexpoint.core.context.providers.ThreadLocalContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 选择器上下文自动配置
 * 自动注册默认的上下文提供者
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@Configuration
public class FlexPointContextAutoConfiguration {

    /**
     * 注册ThreadLocal上下文提供者
     */
    @Bean
    @ConditionalOnMissingBean(name = "threadLocalContextProvider")
    public ContextProvider threadLocalContextProvider() {
        log.info("注册ThreadLocal上下文提供者");
        return new ThreadLocalContextProvider();
    }

    /**
     * 注册方法参数上下文提供者
     */
    @Bean
    @ConditionalOnMissingBean(name = "parameterContextProvider")
    public ContextProvider parameterContextProvider() {
        log.info("注册方法参数上下文提供者");
        return new ParameterContextProvider();
    }

    /**
     * 注册系统属性上下文提供者
     */
    @Bean
    @ConditionalOnMissingBean(name = "systemPropertyContextProvider")
    public ContextProvider systemPropertyContextProvider() {
        log.info("注册系统属性上下文提供者");
        return new SystemPropertyContextProvider();
    }
} 