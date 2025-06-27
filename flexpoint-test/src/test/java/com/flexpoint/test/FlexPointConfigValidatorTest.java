package com.flexpoint.test;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.config.FlexPointConfigValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FlexPoint配置验证器测试
 *
 * @author xiangganluo
 * @version 1.0.0
 */
class FlexPointConfigValidatorTest {

    @Test
    void testValidateNullConfig() {
        // 测试空配置
        FlexPointConfig config = FlexPointConfigValidator.validateAndProcess(null);
        assertNotNull(config);
        assertTrue(config.isEnabled());
        assertNotNull(config.getMonitor());
        assertNotNull(config.getRegistry());
    }

    @Test
    void testValidateDefaultConfig() {
        // 测试默认配置
        FlexPointConfig config = FlexPointConfig.defaultConfig();
        FlexPointConfig validatedConfig = FlexPointConfigValidator.validateAndProcess(config);
        
        assertNotNull(validatedConfig);
        assertTrue(validatedConfig.isEnabled());
        assertTrue(validatedConfig.getMonitor().isEnabled());
        assertTrue(validatedConfig.getRegistry().isEnabled());
    }

    @Test
    void testValidateDisabledConfig() {
        // 测试禁用配置
        FlexPointConfig config = new FlexPointConfig();
        config.setEnabled(false);
        
        FlexPointConfig validatedConfig = FlexPointConfigValidator.validateAndProcess(config);
        assertFalse(validatedConfig.isEnabled());
    }

    @Test
    void testValidateMonitorConfig() {
        // 测试监控配置
        FlexPointConfig config = new FlexPointConfig();
        config.getMonitor().setEnabled(true);
        config.getMonitor().setLogInvocation(false);
        config.getMonitor().setPerformanceStatsEnabled(true);
        
        // 应该能通过验证，但会记录警告
        FlexPointConfig validatedConfig = FlexPointConfigValidator.validateAndProcess(config);
        assertNotNull(validatedConfig);
        assertTrue(validatedConfig.getMonitor().isEnabled());
        assertFalse(validatedConfig.getMonitor().isLogInvocation());
        assertTrue(validatedConfig.getMonitor().isPerformanceStatsEnabled());
    }

    @Test
    void testValidateRegistryConfig() {
        // 测试注册配置
        FlexPointConfig config = new FlexPointConfig();
        config.getRegistry().setEnabled(true);
        config.getRegistry().setAllowDuplicateRegistration(true);
        
        // 应该能通过验证，但会记录警告
        FlexPointConfig validatedConfig = FlexPointConfigValidator.validateAndProcess(config);
        assertNotNull(validatedConfig);
        assertTrue(validatedConfig.getRegistry().isEnabled());
        assertTrue(validatedConfig.getRegistry().isAllowDuplicateRegistration());
    }

    @Test
    void testQuickValidate() {
        // 测试快速验证
        FlexPointConfig config = new FlexPointConfig();
        assertTrue(FlexPointConfigValidator.quickValidate(config));
        
        // 测试无效配置的快速验证
        FlexPointConfig invalidConfig = new FlexPointConfig();
        invalidConfig.setMonitor(null);
        assertTrue(FlexPointConfigValidator.quickValidate(invalidConfig)); // 快速验证不会抛出异常
    }

    @Test
    void testIsEnabled() {
        // 测试启用状态检查
        FlexPointConfig config = new FlexPointConfig();
        assertTrue(FlexPointConfigValidator.isEnabled(config));
        
        config.setEnabled(false);
        assertFalse(FlexPointConfigValidator.isEnabled(config));
        
        assertFalse(FlexPointConfigValidator.isEnabled(null));
    }

    @Test
    void testIsMonitorEnabled() {
        // 测试监控启用状态检查
        FlexPointConfig config = new FlexPointConfig();
        assertTrue(FlexPointConfigValidator.isMonitorEnabled(config));
        
        config.getMonitor().setEnabled(false);
        assertFalse(FlexPointConfigValidator.isMonitorEnabled(config));
        
        config.setEnabled(false);
        assertFalse(FlexPointConfigValidator.isMonitorEnabled(config));
        
        assertFalse(FlexPointConfigValidator.isMonitorEnabled(null));
    }

    @Test
    void testIsRegistryEnabled() {
        // 测试注册启用状态检查
        FlexPointConfig config = new FlexPointConfig();
        assertTrue(FlexPointConfigValidator.isRegistryEnabled(config));
        
        config.getRegistry().setEnabled(false);
        assertFalse(FlexPointConfigValidator.isRegistryEnabled(config));
        
        config.setEnabled(false);
        assertFalse(FlexPointConfigValidator.isRegistryEnabled(config));
        
        assertFalse(FlexPointConfigValidator.isRegistryEnabled(null));
    }

    @Test
    void testConfigurationConsistency() {
        // 测试配置一致性
        FlexPointConfig config = new FlexPointConfig();
        config.getMonitor().setEnabled(false);
        config.getRegistry().setEnabled(true);
        
        // 应该能通过验证，但会给出建议
        FlexPointConfig validatedConfig = FlexPointConfigValidator.validateAndProcess(config);
        assertNotNull(validatedConfig);
        assertFalse(validatedConfig.getMonitor().isEnabled());
        assertTrue(validatedConfig.getRegistry().isEnabled());
    }

    @Test
    void testComplexConfiguration() {
        // 测试复杂配置
        FlexPointConfig config = new FlexPointConfig();
        config.setEnabled(true);
        
        // 监控配置
        config.getMonitor().setEnabled(true);
        config.getMonitor().setLogInvocation(true);
        config.getMonitor().setLogResolution(true);
        config.getMonitor().setLogExceptionDetails(true);
        config.getMonitor().setPerformanceStatsEnabled(true);
        
        // 注册配置
        config.getRegistry().setEnabled(true);
        config.getRegistry().setAllowDuplicateRegistration(false);
        
        FlexPointConfig validatedConfig = FlexPointConfigValidator.validateAndProcess(config);
        assertNotNull(validatedConfig);
        assertTrue(validatedConfig.isEnabled());
        assertTrue(validatedConfig.getMonitor().isEnabled());
        assertTrue(validatedConfig.getRegistry().isEnabled());
    }
} 