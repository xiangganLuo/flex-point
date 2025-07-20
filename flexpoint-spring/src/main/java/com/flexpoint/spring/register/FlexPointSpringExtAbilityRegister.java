package com.flexpoint.spring.register;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.ext.ExtAbilityRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * Spring扩展点注册器
 * 集成元数据管理和自动注册功能
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class FlexPointSpringExtAbilityRegister implements InitializingBean {

    private final ExtAbilityRegistry extAbilityRegistry;
    private final List<ExtAbility> abilities;

    @Override
    public void afterPropertiesSet() {
        if (abilities == null || abilities.isEmpty()) {
            return;
        }
        abilities.forEach(ability -> {
            extAbilityRegistry.register(ability);
            log.info("注册扩展点: code={}, tags={}, class={}", ability.getCode(), ability.getTags(), ability.getClass().getName());
        });
    }
}