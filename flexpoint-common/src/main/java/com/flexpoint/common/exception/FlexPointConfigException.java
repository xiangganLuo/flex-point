package com.flexpoint.common.exception;

/**
 * Flex Point配置异常基类
 * 当框架配置错误时抛出此异常
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class FlexPointConfigException extends RuntimeException {
    
    /**
     * 配置组件名称
     */
    private final String componentName;
    
    /**
     * 配置项名称
     */
    private final String configKey;
    
    /**
     * 配置值
     */
    private final String configValue;
    
    private FlexPointConfigException(String message, String componentName, String configKey, String configValue) {
        super(message);
        this.componentName = componentName;
        this.configKey = configKey;
        this.configValue = configValue;
    }
    
    private FlexPointConfigException(String message, String componentName, String configKey, String configValue, Throwable cause) {
        super(message, cause);
        this.componentName = componentName;
        this.configKey = configKey;
        this.configValue = configValue;
    }
    
    /**
     * 创建基础配置异常
     */
    public static FlexPointConfigException create(String message) {
        return new FlexPointConfigException(message, null, null, null);
    }
    
    /**
     * 创建组件配置异常
     */
    public static FlexPointConfigException forComponent(String componentName, String message) {
        return new FlexPointConfigException(
            String.format("组件[%s]配置错误: %s", componentName, message),
            componentName, null, null);
    }
    
    /**
     * 创建配置项异常
     */
    public static FlexPointConfigException forConfig(String componentName, String configKey, String message) {
        return new FlexPointConfigException(
            String.format("组件[%s]配置项[%s]错误: %s", componentName, configKey, message),
            componentName, configKey, null);
    }
    
    /**
     * 创建配置值异常
     */
    public static FlexPointConfigException forValue(String componentName, String configKey, String configValue, String message) {
        return new FlexPointConfigException(
            String.format("组件[%s]配置项[%s]值[%s]错误: %s", componentName, configKey, configValue, message),
            componentName, configKey, configValue);
    }
    
    /**
     * 创建无效配置值异常
     */
    public static FlexPointConfigException invalidValue(String componentName, String configKey, String configValue, String expectedFormat) {
        return new FlexPointConfigException(
            String.format("组件[%s]配置项[%s]值[%s]格式错误，期望格式: %s", componentName, configKey, configValue, expectedFormat),
            componentName, configKey, configValue);
    }
    
    /**
     * 创建缺失配置异常
     */
    public static FlexPointConfigException missingConfig(String componentName, String configKey) {
        return new FlexPointConfigException(
            String.format("组件[%s]缺少必需配置项: %s", componentName, configKey),
            componentName, configKey, null);
    }
    
    /**
     * 创建带异常链的配置异常
     */
    public static FlexPointConfigException create(String message, Throwable cause) {
        return new FlexPointConfigException(message, null, null, null, cause);
    }
    
    /**
     * 创建带异常链的组件配置异常
     */
    public static FlexPointConfigException forComponent(String componentName, String message, Throwable cause) {
        return new FlexPointConfigException(
            String.format("组件[%s]配置错误: %s", componentName, message),
            componentName, null, null, cause);
    }
    
    /**
     * 获取配置组件名称
     * 
     * @return 组件名称，可能为null
     */
    public String getComponentName() {
        return componentName;
    }
    
    /**
     * 获取配置项名称
     * 
     * @return 配置项名称，可能为null
     */
    public String getConfigKey() {
        return configKey;
    }
    
    /**
     * 获取配置值
     * 
     * @return 配置值，可能为null
     */
    public String getConfigValue() {
        return configValue;
    }
} 