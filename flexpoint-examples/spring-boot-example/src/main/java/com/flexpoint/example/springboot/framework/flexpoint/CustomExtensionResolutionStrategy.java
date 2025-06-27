package com.flexpoint.example.springboot.framework.flexpoint;

import com.flexpoint.core.resolution.AbstractExtensionResolutionStrategy;
import com.flexpoint.example.springboot.framework.flexpoint.context.SysAppContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 自定义扩展点解析策略
 * 根据当前应用上下文中的appCode来解析对应的扩展点实现
 * @author luoxianggan
 */
@Slf4j
@Component
public class CustomExtensionResolutionStrategy extends AbstractExtensionResolutionStrategy {
    
    @Override
    protected String extractCode(Map<String, Object> context) {
        return SysAppContext.getAppCode();
    }

    @Override
    public String getStrategyName() {
        return "ExampleResolutionStrategy";
    }
}