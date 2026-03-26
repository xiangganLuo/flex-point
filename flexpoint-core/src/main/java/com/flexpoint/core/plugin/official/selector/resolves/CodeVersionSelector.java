package com.flexpoint.core.plugin.official.selector.resolves;

import com.flexpoint.common.constants.FlexPointConstants;
import com.flexpoint.core.ext.ExtAbility;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 官方插件域：Code+Version 选择器实现。
 */
public class CodeVersionSelector extends CodeSelector {

    public static final String VERSION_TAG_KEY = "version";
    public static final String DEFAULT_VERSION = "1.0.0";

    public CodeVersionSelector(CodeVersionResolver codeVersionResolver) {
        super(codeVersionResolver);
    }

    /**
     * 先按 code 再按 version 过滤。
     */
    @Override
    protected <T extends ExtAbility> List<T> filterByCode(List<T> candidates) {
        List<T> codeFiltered = super.filterByCode(candidates);
        if (resolver instanceof CodeVersionResolver) {
            String targetVersion = Optional.ofNullable(((CodeVersionResolver) resolver).resolveVersion())
                    .orElse(DEFAULT_VERSION);
            codeFiltered.removeIf(ability -> {
                String abilityVersion = ability.getTags().getString(VERSION_TAG_KEY, DEFAULT_VERSION);
                return !Objects.equals(targetVersion, abilityVersion);
            });
        }
        return codeFiltered;
    }

    @Override
    public String getName() {
        return FlexPointConstants.CODE_VERSION_SELECTOR_NAME;
    }

    /** 业务方实现：解析 version（可选，默认 1.0.0） */
    public interface CodeVersionResolver extends CodeSelector.CodeResolver {
        default String resolveVersion() { return DEFAULT_VERSION; }
    }
}

