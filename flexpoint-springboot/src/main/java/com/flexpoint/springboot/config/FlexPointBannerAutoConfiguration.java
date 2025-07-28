package com.flexpoint.springboot.config;

import com.flexpoint.spring.banner.FlexPointBanner;
import com.flexpoint.springboot.properties.FlexPointProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FlexPoint Banner自动配置
 * 负责启动Banner的创建
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@Configuration
public class FlexPointBannerAutoConfiguration {

    /**
     * 创建启动Banner
     * 根据配置决定是否启用
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = FlexPointProperties.PREFIX, name = "bannerPrint", havingValue = "true", matchIfMissing = true)
    public FlexPointBanner bannerPrint() {
        log.info("创建FlexPoint启动Banner");
        return new FlexPointBanner();
    }
} 