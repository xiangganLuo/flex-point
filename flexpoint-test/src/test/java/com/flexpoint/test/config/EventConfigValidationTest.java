package com.flexpoint.test.config;

import com.flexpoint.common.exception.FlexPointConfigException;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.config.FlexPointConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 事件总线配置校验用例（覆盖 Phase A 工作包 E 配置治理）。
 *
 * @author xiangganluo
 */
public class EventConfigValidationTest {

    @Test
    void default_event_config_passes_validation() {
        Assertions.assertDoesNotThrow(() -> FlexPointBuilder.create(FlexPointConfig.defaultConfig()).build());
    }

    @Test
    void non_positive_queue_size_is_rejected() {
        FlexPointConfig config = new FlexPointConfig();
        config.getEvent().setAsyncQueueSize(0);
        Assertions.assertThrows(FlexPointConfigException.class,
                () -> FlexPointBuilder.create(config));
    }

    @Test
    void max_pool_smaller_than_core_is_rejected() {
        FlexPointConfig config = new FlexPointConfig();
        config.getEvent().setAsyncCorePoolSize(8);
        config.getEvent().setAsyncMaxPoolSize(4);
        Assertions.assertThrows(FlexPointConfigException.class,
                () -> FlexPointBuilder.create(config));
    }

    @Test
    void negative_core_pool_size_is_rejected() {
        FlexPointConfig config = new FlexPointConfig();
        config.getEvent().setAsyncCorePoolSize(-1);
        Assertions.assertThrows(FlexPointConfigException.class,
                () -> FlexPointBuilder.create(config));
    }
}
