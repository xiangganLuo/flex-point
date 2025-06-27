package com.flexpoint.core.config;

import com.flexpoint.common.exception.FlexPointConfigException;
import lombok.extern.slf4j.Slf4j;

/**
 * Flex Point配置验证器
 * 提供配置验证和默认值处理
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class FlexPointConfigValidator {
    
    /**
     * 验证并处理配置
     */
    public static FlexPointConfig validateAndProcess(FlexPointConfig config) {
        try {
            if (config == null) {
                log.info("配置为空，使用默认配置");
                return FlexPointConfig.defaultConfig();
            }
            
            log.info("Flex Point框架配置验证开始...");
            
            // 验证主配置
            validateMainConfiguration(config);
            
            if (!config.isEnabled()) {
                log.info("Flex Point框架已禁用");
                return config;
            }
            
            // 验证监控配置
            validateMonitorConfiguration(config.getMonitor());
            
            // 验证注册配置
            validateRegistryConfiguration(config.getRegistry());
            
            // 验证配置一致性
            validateConfigurationConsistency(config);
            
            log.info("Flex Point框架配置验证完成");
            logConfigurationSummary(config);
            
            return config;
        } catch (FlexPointConfigException e) {
            log.error("配置验证失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("配置验证过程中发生未知错误", e);
            throw new FlexPointConfigException("配置验证失败", e);
        }
    }
    
    /**
     * 验证主配置
     */
    private static void validateMainConfiguration(FlexPointConfig config) {
        if (config == null) {
            throw new FlexPointConfigException("配置对象不能为空");
        }
        
        log.debug("主配置验证通过: enabled={}", config.isEnabled());
    }
    
    /**
     * 验证监控配置
     */
    private static void validateMonitorConfiguration(FlexPointConfig.MonitorConfig monitor) {
        if (monitor == null) {
            log.warn("监控配置为空，使用默认配置");
            return;
        }
        
        // 验证监控配置的合理性
        if (monitor.isEnabled()) {
            log.debug("监控已启用，验证监控配置项...");
            
            // 如果启用了性能统计，但禁用了调用日志，给出警告
            if (monitor.isPerformanceStatsEnabled() && !monitor.isLogInvocation()) {
                log.warn("性能统计已启用但调用日志已禁用，可能影响监控效果");
            }
            
            // 如果启用了异常详情日志，但禁用了调用日志，给出警告
            if (monitor.isLogExceptionDetails() && !monitor.isLogInvocation()) {
                log.warn("异常详情日志已启用但调用日志已禁用，异常信息可能不完整");
            }
        } else {
            log.info("扩展点监控已禁用");
            
            // 如果监控被禁用，但其他监控相关配置被启用，给出警告
            if (monitor.isLogInvocation() || monitor.isLogResolution() || 
                monitor.isLogExceptionDetails() || monitor.isPerformanceStatsEnabled()) {
                log.warn("监控已禁用，但部分监控配置项仍被启用，这些配置将被忽略");
            }
        }
        
        log.debug("监控配置验证通过: enabled={}, logInvocation={}, logResolution={}, " +
                 "logExceptionDetails={}, performanceStatsEnabled={}",
                monitor.isEnabled(), monitor.isLogInvocation(), monitor.isLogResolution(),
                monitor.isLogExceptionDetails(), monitor.isPerformanceStatsEnabled());
    }
    
    /**
     * 验证注册配置
     */
    private static void validateRegistryConfiguration(FlexPointConfig.RegistryConfig registry) {
        if (registry == null) {
            log.warn("注册配置为空，使用默认配置");
            return;
        }
        
        // 验证注册配置的合理性
        if (registry.isEnabled()) {
            log.debug("自动注册已启用，验证注册配置项...");
            
            // 如果允许重复注册，给出警告
            if (registry.isAllowDuplicateRegistration()) {
                log.warn("允许重复注册已启用，可能导致扩展点覆盖");
            }
        } else {
            log.info("扩展点自动注册已禁用");
            
            // 如果注册被禁用，但允许重复注册被启用，给出警告
            if (registry.isAllowDuplicateRegistration()) {
                log.warn("自动注册已禁用，但允许重复注册仍被启用，此配置将被忽略");
            }
        }
        
        log.debug("注册配置验证通过: enabled={}, allowDuplicateRegistration={}",
                registry.isEnabled(), registry.isAllowDuplicateRegistration());
    }
    
    /**
     * 验证配置一致性
     */
    private static void validateConfigurationConsistency(FlexPointConfig config) {
        // 检查配置项之间的依赖关系
        FlexPointConfig.MonitorConfig monitor = config.getMonitor();
        FlexPointConfig.RegistryConfig registry = config.getRegistry();

        if (monitor != null && registry != null) {
            // 如果监控被禁用，但注册被启用，给出建议
            if (!monitor.isEnabled() && registry.isEnabled()) {
                log.info("建议：启用监控以便观察扩展点注册和使用情况");
            }

            // 如果注册被禁用，但监控被启用，给出建议
            if (!registry.isEnabled() && monitor.isEnabled()) {
                log.info("建议：启用自动注册以便自动发现和注册扩展点");
            }
        }

        log.debug("配置一致性验证通过");
    }
    
    /**
     * 记录配置摘要
     */
    private static void logConfigurationSummary(FlexPointConfig config) {
        FlexPointConfig.MonitorConfig monitor = config.getMonitor();
        FlexPointConfig.RegistryConfig registry = config.getRegistry();
        
        log.info("=== Flex Point配置摘要 ===");
        log.info("框架状态: {}", config.isEnabled() ? "已启用" : "已禁用");
        
        if (monitor != null) {
            log.info("监控配置:");
            log.info("  启用状态: {}", monitor.isEnabled() ? "已启用" : "已禁用");
            if (monitor.isEnabled()) {
                log.info("  调用日志: {}", monitor.isLogInvocation() ? "已启用" : "已禁用");
                log.info("  解析日志: {}", monitor.isLogResolution() ? "已启用" : "已禁用");
                log.info("  异常详情: {}", monitor.isLogExceptionDetails() ? "已启用" : "已禁用");
                log.info("  性能统计: {}", monitor.isPerformanceStatsEnabled() ? "已启用" : "已禁用");
            }
        }
        
        if (registry != null) {
            log.info("注册配置:");
            log.info("  启用状态: {}", registry.isEnabled() ? "已启用" : "已禁用");
            if (registry.isEnabled()) {
                log.info("  重复注册: {}", registry.isAllowDuplicateRegistration() ? "允许" : "禁止");
            }
        }
        log.info("========================");
    }
    
    /**
     * 快速验证配置（不抛出异常，只记录警告）
     */
    public static boolean quickValidate(FlexPointConfig config) {
        try {
            validateAndProcess(config);
            return true;
        } catch (FlexPointConfigException e) {
            log.warn("配置快速验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证配置是否启用
     */
    public static boolean isEnabled(FlexPointConfig config) {
        return config != null && config.isEnabled();
    }
    
    /**
     * 验证监控是否启用
     */
    public static boolean isMonitorEnabled(FlexPointConfig config) {
        return isEnabled(config) && 
               config.getMonitor() != null && 
               config.getMonitor().isEnabled();
    }
    
    /**
     * 验证注册是否启用
     */
    public static boolean isRegistryEnabled(FlexPointConfig config) {
        return isEnabled(config) && 
               config.getRegistry() != null && 
               config.getRegistry().isEnabled();
    }
} 