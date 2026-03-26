package com.flexpoint.core.plugin.official.selector.resolves;

import com.flexpoint.common.constants.FlexPointConstants;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.selector.AbstractSelector;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 官方插件域：Code 选择器实现。
 * 与旧版路径不同（不再位于 core.selector.resolves）。
 */
@RequiredArgsConstructor
public class CodeSelector extends AbstractSelector {

    protected final CodeResolver resolver;

    @Override
    protected <T extends ExtAbility> List<T> filter(List<T> candidates) {
        return filterByCode(candidates);
    }

    /**
     * 按 code 过滤候选者，子类可覆盖。
     */
    protected <T extends ExtAbility> List<T> filterByCode(List<T> candidates) {
        if (resolver == null) {
            throw new IllegalStateException(getName() + " Selector 的 Resolver 不能为空，请注册业务实现！");
        }
        String code = resolver.resolveCode();
        if (code == null) {
            return Collections.emptyList();
        }
        return candidates.stream().filter(ext -> code.equals(ext.getCode())).collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return FlexPointConstants.CODE_SELECTOR_NAME;
    }

    /** 业务方实现用于解析 code 的接口 */
    public interface CodeResolver { String resolveCode(); }
}

