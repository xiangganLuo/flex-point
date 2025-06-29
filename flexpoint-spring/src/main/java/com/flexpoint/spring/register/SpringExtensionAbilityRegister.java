package com.flexpoint.spring.register;

import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.extension.ExtensionAbilityRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
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
public class SpringExtensionAbilityRegister implements ApplicationContextAware {

    private final ExtensionAbilityRegistry extensionAbilityRegistry;
    private ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void initAbilities() {
        Map<String, ExtensionAbility> beans = applicationContext.getBeansOfType(ExtensionAbility.class);
        
        int registeredCount = 0;
        for (Map.Entry<String, ExtensionAbility> entry : beans.entrySet()) {
            ExtensionAbility ability = entry.getValue();
            String beanName = entry.getKey();
            
            Class<? extends ExtensionAbility> abilityClass = ability.getClass();
            Class<?>[] interfaces = abilityClass.getInterfaces();
            
            for (Class<?> anInterface : interfaces) {
                if (ExtensionAbility.class.isAssignableFrom(anInterface)) {
                    extensionAbilityRegistry.register(ability);
                    registeredCount++;
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

} 