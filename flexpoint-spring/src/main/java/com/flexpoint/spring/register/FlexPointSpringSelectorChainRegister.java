package com.flexpoint.spring.register;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.config.FlexPointConfig;
import com.flexpoint.core.selector.Selector;
import com.flexpoint.core.selector.SelectorChain;
import com.flexpoint.core.selector.SelectorRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.flexpoint.common.constants.FlexPointConstants.DEFAULT_SELECTOR_CHAIN_NAME;

/**
 * Spring选择器链自动注册器
 * 启动时根据配置自动组装并注册SelectorChain
 * 如果未配置选择器链，则使用默认链名称
 * @author luoxianggan
 */
@Slf4j
@RequiredArgsConstructor
public class FlexPointSpringSelectorChainRegister implements InitializingBean, ApplicationContextAware {

    private final FlexPoint flexPoint;
    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() {
        SelectorRegistry selectorRegistry = flexPoint.getSelectorRegistry();
        FlexPointConfig flexPointConfig = flexPoint.getFlexPointConfig();
        Map<String, List<String>> selectorChainsConfig = flexPointConfig.getSelector().getChains();
        Map<String, Selector> selectorBeans = applicationContext.getBeansOfType(Selector.class);
        
        if (selectorBeans.isEmpty()) {
            log.warn("未找到任何Selector Bean，跳过选择器链注册");
            return;
        }
        
        // 记录已配置的选择器，用于后续判断哪些选择器需要走默认链
        Set<String> configuredSelectors = new HashSet<>();
        
        if (selectorChainsConfig != null && !selectorChainsConfig.isEmpty()) {
            // 注册配置的选择器链
            configuredSelectors = registerConfiguredSelectorChains(selectorRegistry, selectorChainsConfig, selectorBeans);
        }
        
        // 将未配置的选择器注册到默认链
        registerUnconfiguredSelectorsToDefaultChain(selectorRegistry, selectorBeans, configuredSelectors);
    }

    /**
     * 注册配置的选择器链
     * 
     * @return 已配置的选择器名称集合
     */
    private Set<String> registerConfiguredSelectorChains(SelectorRegistry selectorRegistry, 
                                                        Map<String, List<String>> selectorChainsConfig,
                                                        Map<String, Selector> selectorBeans) {
        Set<String> configuredSelectors = new HashSet<>();
        
        for (Map.Entry<String, List<String>> entry : selectorChainsConfig.entrySet()) {
            String chainName = entry.getKey();
            List<String> selectorNames = entry.getValue();
            SelectorChain chain = new SelectorChain(chainName);
            
            for (String selectorBeanName : selectorNames) {
                Selector selector = selectorBeans.get(selectorBeanName);
                if (selector != null) {
                    chain.addSelector(selector);
                    configuredSelectors.add(selectorBeanName);
                    log.info("Selector[{}] 已加入链路[{}]", selectorBeanName, chainName);
                } else {
                    log.warn("未找到Selector Bean: {}，跳过", selectorBeanName);
                }
            }
            
            selectorRegistry.registerSelectorChain(chain);
            log.info("SelectorChain[{}] 注册完成，包含Selector: {}", chainName, selectorNames);
        }
        
        return configuredSelectors;
    }

    /**
     * 将未配置的选择器注册到默认链
     * 
     * @param selectorRegistry 选择器注册表
     * @param selectorBeans 所有选择器Bean
     * @param configuredSelectors 已配置的选择器名称集合
     */
    private void registerUnconfiguredSelectorsToDefaultChain(SelectorRegistry selectorRegistry, 
                                                            Map<String, Selector> selectorBeans,
                                                            Set<String> configuredSelectors) {
        // 找出未配置的选择器
        List<String> unconfiguredSelectors = new ArrayList<>();
        for (Map.Entry<String, Selector> entry : selectorBeans.entrySet()) {
            String beanName = entry.getKey();
            if (!configuredSelectors.contains(beanName)) {
                unconfiguredSelectors.add(beanName);
            }
        }
        
        if (unconfiguredSelectors.isEmpty()) {
            log.info("所有选择器都已配置到指定链中，无需创建默认链");
            return;
        }
        
        // 检查是否已存在默认链
        SelectorChain defaultChain = selectorRegistry.getSelectorChain(DEFAULT_SELECTOR_CHAIN_NAME);
        if (defaultChain == null) {
            defaultChain = new SelectorChain(DEFAULT_SELECTOR_CHAIN_NAME);
            selectorRegistry.registerSelectorChain(defaultChain);
            log.info("创建默认选择器链: {}", DEFAULT_SELECTOR_CHAIN_NAME);
        }
        
        // 将未配置的选择器添加到默认链
        for (String beanName : unconfiguredSelectors) {
            Selector selector = selectorBeans.get(beanName);
            defaultChain.addSelector(selector);
            log.info("未配置的Selector[{}] 已加入默认链路[{}]", beanName, DEFAULT_SELECTOR_CHAIN_NAME);
        }
        
        log.info("默认SelectorChain[{}] 注册完成，包含{}个未配置的Selector: {}", 
                DEFAULT_SELECTOR_CHAIN_NAME, unconfiguredSelectors.size(), unconfiguredSelectors);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}