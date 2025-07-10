package com.flexpoint.spring.register;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.context.ContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * FlexPoint上下文提供者初始化器
 * 自动注册所有上下文提供者到FlexPoint
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class FlexPointContextProviderRegister implements InitializingBean {

    private final FlexPoint flexPoint;
    private final List<ContextProvider> providers;

    @Override
    public void afterPropertiesSet() {
        if (providers != null && !providers.isEmpty()) {
            for (ContextProvider provider : providers) {
                flexPoint.getContextManager().registerProvider(provider);
                log.info("注册上下文提供者到FlexPoint: name={}, priority={}", provider.getName(), provider.getPriority());
            }
            log.info("FlexPoint上下文提供者初始化完成，共注册{}个提供者", providers.size());
        } else {
            log.warn("未找到任何上下文提供者");
        }
    }
} 