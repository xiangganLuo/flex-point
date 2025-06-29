package com.flexpoint.example.springboot.framework.flexpoint;

import com.flexpoint.core.resolution.AbstractExtensionResolutionStrategy;
import com.flexpoint.core.resolution.ResolutionContext;
import com.flexpoint.example.springboot.framework.flexpoint.context.SysAppContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 自定义扩展点解析策略
 * 根据当前应用上下文中的appCode来解析对应的扩展点实现
 * @author luoxianggan
 */
@Slf4j
@Component
public class CustomExtensionResolutionStrategy extends AbstractExtensionResolutionStrategy {

    @Override
    protected ResolutionContext extractContext() {
        return new ResolutionContext(SysAppContext.getAppCode(), null);
    }

    @Override
    public String getStrategyName() {
        return "ExampleResolutionStrategy";
    }
}