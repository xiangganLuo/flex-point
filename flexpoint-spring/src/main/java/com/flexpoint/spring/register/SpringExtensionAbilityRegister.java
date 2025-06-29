package com.flexpoint.spring.register;

import com.flexpoint.core.registry.ExtensionAbility;
import com.flexpoint.common.annotations.ExtensionInfo;
import com.flexpoint.core.registry.metadata.DefaultExtensionMetadata;
import com.flexpoint.core.registry.metadata.ExtensionMetadata;
import com.flexpoint.core.registry.ExtensionAbilityRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.Optional;

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
        log.info("开始扫描和注册扩展点...");

        Map<String, ExtensionAbility> beans = applicationContext.getBeansOfType(ExtensionAbility.class);
        
        int registeredCount = 0;
        for (Map.Entry<String, ExtensionAbility> entry : beans.entrySet()) {
            ExtensionAbility ability = entry.getValue();
            String beanName = entry.getKey();
            
            Class<? extends ExtensionAbility> abilityClass = ability.getClass();
            Class<?>[] interfaces = abilityClass.getInterfaces();
            
            for (Class<?> anInterface : interfaces) {
                if (ExtensionAbility.class.isAssignableFrom(anInterface)) {
                    Class<? extends ExtensionAbility> extensionAbilityInterface = (Class<? extends ExtensionAbility>) anInterface;
                    
                    // 创建扩展点元数据
                    ExtensionMetadata metadata = createExtensionMetadata(ability, beanName, extensionAbilityInterface);
                    
                    // 注册扩展点
                    extensionAbilityRegistry.register(extensionAbilityInterface, ability, metadata);
                    registeredCount++;
                    
                    log.debug("扩展点注册成功: interface={}, implementation={}, code={}", 
                            extensionAbilityInterface.getSimpleName(), abilityClass.getSimpleName(), ability.getCode());
                }
            }
        }
        
        log.info("扩展点扫描完成，共注册 {} 个扩展点", registeredCount);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    /**
     * 创建扩展点元数据
     *
     * @param ability 扩展点实例
     * @param beanName Bean名称
     * @param interfaceClass 接口类型
     * @return 扩展点元数据
     */
    public static ExtensionMetadata createExtensionMetadata(ExtensionAbility ability, String beanName,
                                                            Class<? extends ExtensionAbility> interfaceClass) {

        Class<?> clazz = ability.getClass();
        ExtensionInfo info = clazz.getAnnotation(ExtensionInfo.class);

        String extensionId = clazz.getSimpleName();
        String version = "1.0.0";
        int priority = 100;
        String description = "Spring Bean: " + beanName;
        boolean enabled = true;
        String extensionType = interfaceClass.getSimpleName();

        // 使用 Optional 优雅处理注解信息
        Optional<ExtensionInfo> infoOpt = Optional.ofNullable(info);
        extensionId = infoOpt.map(ExtensionInfo::id).filter(s -> !s.isEmpty()).orElse(extensionId);
        version = infoOpt.map(ExtensionInfo::version).filter(s -> !s.isEmpty()).orElse(version);
        priority = infoOpt.map(ExtensionInfo::priority).orElse(priority);
        description = infoOpt.map(ExtensionInfo::description).filter(s -> !s.isEmpty()).orElse(description);
        enabled = infoOpt.map(ExtensionInfo::enabled).orElse(enabled);

        return DefaultExtensionMetadata.builder()
                .extensionId(extensionId)
                .version(version)
                .priority(priority)
                .description(description)
                .enabled(enabled)
                .extensionType(extensionType)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .build();
    }
} 