package com.flexpoint.spring.register;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.selector.Selector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * Spring选择器自动注册器
 * 启动时自动扫描并注册所有选择器Bean
 * @author luoxianggan
 */
@Slf4j
@RequiredArgsConstructor
public class FlexPointSpringSelectorRegister implements InitializingBean, ApplicationContextAware {

    private final FlexPoint flexPoint;
    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() {
        Map<String, Selector> selectorBeans = applicationContext.getBeansOfType(Selector.class);
        
        if (selectorBeans.isEmpty()) {
            return;
        }
        
        // 注册所有选择器Bean
        for (Map.Entry<String, Selector> entry : selectorBeans.entrySet()) {
            String beanName = entry.getKey();
            Selector selector = entry.getValue();
            
            flexPoint.registerSelector(selector);
            log.info("注册选择器Bean[{}] -> 选择器[{}]", beanName, selector.getName());
        }
        
        log.info("完成选择器自动注册，共注册{}个选择器", selectorBeans.size());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}