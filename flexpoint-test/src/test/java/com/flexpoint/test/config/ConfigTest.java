package com.flexpoint.test.config;

import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.config.FlexPointConfigValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigTest {
    @Test
    public void testDefaultConfig() {
        FlexPointConfig config = new FlexPointConfig();
        Assertions.assertTrue(config.isEnabled());
        Assertions.assertTrue(config.getMonitor().isEnabled());
        Assertions.assertTrue(config.getRegistry().isEnabled());
    }

    @Test
    public void testConfigValidatorPass() {
        FlexPointConfig config = new FlexPointConfig();
        config.setEnabled(true);
        FlexPointConfig validated = FlexPointConfigValidator.validateAndProcess(config);
        Assertions.assertTrue(validated.isEnabled());
    }

    @Test
    public void testConfigValidatorDisabled() {
        FlexPointConfig config = new FlexPointConfig();
        config.setEnabled(false);
        FlexPointConfig validated = FlexPointConfigValidator.validateAndProcess(config);
        Assertions.assertFalse(validated.isEnabled());
    }
} 