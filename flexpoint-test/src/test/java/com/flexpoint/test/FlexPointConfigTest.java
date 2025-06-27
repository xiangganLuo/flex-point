package com.flexpoint.test;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.config.FlexPointConfigValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FlexPoint配置功能测试
 *
 * @author xiangganluo
 * @version 1.0.0
 */
class FlexPointConfigTest {

    @Test
    void testDefaultConfig() {
        FlexPointConfig config = new FlexPointConfig();
        
        // 验证默认配置
        assertTrue(config.isEnabled());
        assertTrue(config.getMonitor().isEnabled());
        assertTrue(config.getRegistry().isEnabled());
        assertTrue(config.getRegistry().isAllowDuplicateRegistration());
        assertTrue(config.getMonitor().isLogInvocation());
        assertTrue(config.getMonitor().isLogResolution());
        assertTrue(config.getMonitor().isLogExceptionDetails());
        assertTrue(config.getMonitor().isPerformanceStatsEnabled());
    }

    @Test
    void testConfigBuilder() {
        FlexPointConfig config = new FlexPointConfig();
        
        // 设置配置
        config.setEnabled(false);
        config.getMonitor().setEnabled(false);
        config.getRegistry().setEnabled(false);
        config.getRegistry().setAllowDuplicateRegistration(false);
        config.getMonitor().setLogInvocation(false);
        config.getMonitor().setLogResolution(false);
        config.getMonitor().setLogExceptionDetails(false);
        config.getMonitor().setPerformanceStatsEnabled(false);
        
        // 验证配置
        assertFalse(config.isEnabled());
        assertFalse(config.getMonitor().isEnabled());
        assertFalse(config.getRegistry().isEnabled());
        assertFalse(config.getRegistry().isAllowDuplicateRegistration());
        assertFalse(config.getMonitor().isLogInvocation());
        assertFalse(config.getMonitor().isLogResolution());
        assertFalse(config.getMonitor().isLogExceptionDetails());
        assertFalse(config.getMonitor().isPerformanceStatsEnabled());
    }

    @Test
    void testConfigValidation() {
        FlexPointConfig config = new FlexPointConfig();
        
        // 验证有效配置
        FlexPointConfigValidator.validateAndProcess(config);
        
        // 测试无效配置
        config.setEnabled(false);
        FlexPointConfigValidator.validateAndProcess(config);
    }

    @Test
    void testConfigValidationWithNull() {
        // 测试null配置
        FlexPointConfigValidator.validateAndProcess(null);
    }

    @Test
    void testConfigValidationWithDisabled() {
        FlexPointConfig config = new FlexPointConfig();
        config.setEnabled(false);

        // 禁用状态下，其他配置应该被忽略
        FlexPointConfigValidator.validateAndProcess(config);
    }

    @Test
    void testConfigValidationWithDisabledMonitor() {
        FlexPointConfig config = new FlexPointConfig();
        config.getMonitor().setEnabled(false);
        
        // 监控禁用时，其他监控配置应该被忽略
        FlexPointConfigValidator.validateAndProcess(config);
    }

    @Test
    void testConfigValidationWithDisabledRegistry() {
        FlexPointConfig config = new FlexPointConfig();
        config.getRegistry().setEnabled(false);
        
        // 注册中心禁用时，其他注册配置应该被忽略
        FlexPointConfigValidator.validateAndProcess(config);
    }

    @Test
    void testConfigCopy() {
        FlexPointConfig original = new FlexPointConfig();
        original.setEnabled(false);
        original.getMonitor().setEnabled(false);
        original.getRegistry().setEnabled(false);
        
        // 创建副本
        FlexPointConfig copy = new FlexPointConfig();
        copy.setEnabled(original.isEnabled());
        copy.getMonitor().setEnabled(original.getMonitor().isEnabled());
        copy.getRegistry().setEnabled(original.getRegistry().isEnabled());
        
        // 验证副本
        assertEquals(original.isEnabled(), copy.isEnabled());
        assertEquals(original.getMonitor().isEnabled(), copy.getMonitor().isEnabled());
        assertEquals(original.getRegistry().isEnabled(), copy.getRegistry().isEnabled());
    }

    @Test
    void testConfigToString() {
        FlexPointConfig config = new FlexPointConfig();
        String configString = config.toString();
        
        // 验证toString包含关键信息
        assertTrue(configString.contains("enabled"));
        assertTrue(configString.contains("monitor"));
        assertTrue(configString.contains("registry"));
    }

    @Test
    void testMonitorConfig() {
        FlexPointConfig.MonitorConfig monitorConfig = new FlexPointConfig.MonitorConfig();
        
        // 验证默认值
        assertTrue(monitorConfig.isEnabled());
        assertTrue(monitorConfig.isLogInvocation());
        assertTrue(monitorConfig.isLogResolution());
        assertTrue(monitorConfig.isLogExceptionDetails());
        assertTrue(monitorConfig.isPerformanceStatsEnabled());
        
        // 设置值
        monitorConfig.setEnabled(false);
        monitorConfig.setLogInvocation(false);
        monitorConfig.setLogResolution(false);
        monitorConfig.setLogExceptionDetails(false);
        monitorConfig.setPerformanceStatsEnabled(false);
        
        // 验证设置的值
        assertFalse(monitorConfig.isEnabled());
        assertFalse(monitorConfig.isLogInvocation());
        assertFalse(monitorConfig.isLogResolution());
        assertFalse(monitorConfig.isLogExceptionDetails());
        assertFalse(monitorConfig.isPerformanceStatsEnabled());
    }

    @Test
    void testRegistryConfig() {
        FlexPointConfig.RegistryConfig registryConfig = new FlexPointConfig.RegistryConfig();
        
        // 验证默认值
        assertTrue(registryConfig.isEnabled());
        assertTrue(registryConfig.isAllowDuplicateRegistration());
        
        // 设置值
        registryConfig.setEnabled(false);
        registryConfig.setAllowDuplicateRegistration(false);
        
        // 验证设置的值
        assertFalse(registryConfig.isEnabled());
        assertFalse(registryConfig.isAllowDuplicateRegistration());
    }

    @Test
    void testConfigEquality() {
        FlexPointConfig config1 = new FlexPointConfig();
        FlexPointConfig config2 = new FlexPointConfig();
        
        // 默认配置应该相等
        assertEquals(config1, config2);
        
        // 修改配置后应该不相等
        config1.setEnabled(false);
        assertNotEquals(config1, config2);
    }

    @Test
    void testConfigHashCode() {
        FlexPointConfig config1 = new FlexPointConfig();
        FlexPointConfig config2 = new FlexPointConfig();
        
        // 默认配置的hashCode应该相等
        assertEquals(config1.hashCode(), config2.hashCode());
        
        // 修改配置后hashCode应该不相等
        config1.setEnabled(false);
        assertNotEquals(config1.hashCode(), config2.hashCode());
    }
} 