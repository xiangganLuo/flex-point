package com.flexpoint.spring.register;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.ext.ExtAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * Spring扩展点注册器
 * 集成元数据管理和自动注册功能
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class FlexPointSpringExtAbilityRegister implements InitializingBean, ApplicationContextAware {

    private final FlexPoint flexPoint;

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() {
        Map<String, ExtAbility> extAbilityBeans = applicationContext.getBeansOfType(ExtAbility.class);
        if (extAbilityBeans.isEmpty()) {
            return;
        }
        // 注册所有扩展点Bean
        for (Map.Entry<String, ExtAbility> entry : extAbilityBeans.entrySet()) {
            String beanName = entry.getKey();
            ExtAbility extAbility = entry.getValue();

            flexPoint.register(extAbility);
            log.info("注册扩展点: code={}, tags={}, class={}", extAbility.getCode(), extAbility.getTags(), extAbility.getClass().getName());
        }
        log.info("完成选择器自动注册，共注册{}个选择器", extAbilityBeans.size());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}