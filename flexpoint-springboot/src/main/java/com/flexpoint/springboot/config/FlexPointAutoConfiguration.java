package com.flexpoint.springboot.config;

import com.flexpoint.springboot.properties.FlexPointProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * FlexPoint 主自动配置类
 * 导入各个功能模块的自动配置
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = FlexPointProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FlexPointProperties.class)
@Import({
    FlexPointCoreAutoConfiguration.class,
    FlexPointRegistryAutoConfiguration.class,
    FlexPointEventAutoConfiguration.class,
    FlexPointMonitorAutoConfiguration.class,
    FlexPointProcessorAutoConfiguration.class,
    FlexPointBannerAutoConfiguration.class
})
public class FlexPointAutoConfiguration {

    /**
     * 主配置类，负责导入各个功能模块的配置
     */
    public FlexPointAutoConfiguration() {
        log.info("FlexPoint 自动配置已启用");
    }
}