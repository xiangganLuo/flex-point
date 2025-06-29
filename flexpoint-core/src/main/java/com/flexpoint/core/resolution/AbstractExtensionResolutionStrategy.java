package com.flexpoint.core.resolution;

import com.flexpoint.core.extension.ExtensionAbility;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 抽象扩展点解析策略
 * 提供通用的扩展点选择逻辑，子类可以重写特定方法来自定义选择策略
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@Slf4j
public abstract class AbstractExtensionResolutionStrategy implements ExtensionResolutionStrategy {

    @Override
    public <T extends ExtensionAbility> T resolve(List<T> extensions) {
        // 1. 参数校验
        if (extensions == null || extensions.isEmpty()) {
            log.warn("扩展点列表为空");
            return null;
        }

        // 2. 获取扩展点上下文信息
        ResolutionContext context = extractContext();
        if (context == null || context.getCode() == null) {
            log.warn("扩展点上下文信息为空，无法解析扩展点");
            return null;
        }

        // 3. 匹配扩展点
        return selectByCodeAndVersion(extensions, context);
    }

    /**
     * 提取扩展点上下文信息
     *
     * @return 扩展点上下文信息
     */
    protected abstract ResolutionContext extractContext();

    /**
     * 按业务代码和版本选择扩展点候选者
     * 子类可以调用此方法来按业务代码和版本选择扩展点
     *
     * @param extensions 扩展点候选者列表
     * @param context 上下文
     * @return 匹配的扩展点候选者
     */
    protected <T extends ExtensionAbility> T selectByCodeAndVersion(List<T> extensions, ResolutionContext context) {
        if (context == null || context.getCode() == null || context.getCode().trim().isEmpty()) {
            return null;
        }

        return extensions.stream()
                .filter(e -> e.getCode().equals(context.getCode()))
                .filter(e -> context.getVersion() == null || e.version().equals(context.getVersion()))
                .findFirst()
                .orElse(null);
    }
    
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