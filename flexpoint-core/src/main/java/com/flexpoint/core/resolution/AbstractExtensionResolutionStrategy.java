package com.flexpoint.core.resolution;

import com.flexpoint.common.ExtensionAbility;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 抽象扩展点解析策略
 * 抽象了code解析的通用流程，接入方只需要实现code获取逻辑
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public abstract class AbstractExtensionResolutionStrategy implements ExtensionResolutionStrategy {

    public static final String DEFAULT_RESOLVER_NAME = "DefaultExtensionResolutionStrategy";

    @Override
    public <T extends ExtensionAbility> T resolve(List<T> extensions, Map<String, Object> context) {
        // 1. 参数校验
        if (extensions == null || extensions.isEmpty()) {
            log.warn("扩展点列表为空");
            return null;
        }
        
        // 2. 获取code（由子类实现）
        String code = extractCode(context);
        if (code == null || code.trim().isEmpty()) {
            log.warn("code为空，无法解析扩展点");
            return null;
        }
        
        // 3. 根据code匹配扩展点
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
    
    /**
     * 提取code
     * 子类需要实现此方法来定义如何从上下文中提取code
     *
     * @param context 上下文信息
     * @return 提取的code
     */
    protected abstract String extractCode(Map<String, Object> context);
    
    /**
     * 获取策略名称
     * 子类可以重写此方法来提供自定义的策略名称
     *
     * @return 策略名称
     */
    @Override
    public String getStrategyName() {
        return this.getClass().getSimpleName();
    }
} 