package com.flexpoint.core.resolution;

import com.flexpoint.common.constants.FlexPointConstants;

import java.util.Map;

/**
 * 默认扩展点解析策略
 * 从上下文中提取code进行扩展点匹配
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class DefaultExtensionResolutionStrategy extends AbstractExtensionResolutionStrategy {

    @Override
    protected String extractCode(Map<String, Object> context) {
        if (context != null && context.containsKey(FlexPointConstants.CODE)) {
            return (String) context.get(FlexPointConstants.CODE);
        }
        return null;
    }
    
    @Override
    public String getStrategyName() {
        return DefaultExtensionResolutionStrategy.DEFAULT_RESOLVER_NAME;
    }
} 