package com.flexpoint.core.resolution;

import com.flexpoint.common.ExtensionAbility;
import com.flexpoint.common.constants.FlexPointConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 默认扩展点解析策略
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public class DefaultExtensionResolutionStrategy implements ExtensionResolutionStrategy {

    public static final String DEFAULT_RESOLVER_NAME = "DefaultExtensionResolutionStrategy";

    @Override
    public <T extends ExtensionAbility> T resolve(List<T> extensions, Map<String, Object> context) {
        if (extensions == null || extensions.isEmpty()) {
            log.warn("扩展点列表为空");
            return null;
        }
        
        String code = getCode(context);
        if (code == null || code.trim().isEmpty()) {
            log.warn("code为空，无法解析扩展点");
            return null;
        }
        
        Optional<T> matched = extensions.stream()
                .filter(extension -> code.equals(extension.getCode()))
                .findFirst();
        
        if (matched.isPresent()) {
            log.debug("找到匹配的扩展点: code={}, class={}",
                    code, matched.get().getClass().getName());
            return matched.get();
        } else {
            log.warn("未找到匹配的扩展点: code={}", code);
            return null;
        }
    }
    
    @Override
    public String getStrategyName() {
        return "DefaultExtensionResolutionStrategy";
    }
    
    private String getCode(Map<String, Object> context) {
        if (context != null && context.containsKey(FlexPointConstants.CODE)) {
            return (String) context.get(FlexPointConstants.CODE);
        }
        return null;
    }
} 