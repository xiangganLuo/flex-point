package com.flexpoint.springboot.properties;

import com.flexpoint.core.config.FlexPointConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Flex Point框架配置属性
 * 继承core模块的配置类，提供Spring Boot特定的配置支持
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = FlexPointProperties.PREFIX)
public class FlexPointProperties extends FlexPointConfig {

    public static final String PREFIX = "flexpoint";

    private boolean bannerPrint = true;

}