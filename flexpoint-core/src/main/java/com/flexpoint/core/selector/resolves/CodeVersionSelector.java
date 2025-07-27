package com.flexpoint.core.selector.resolves;

import com.flexpoint.common.constants.FlexPointConstants;
import com.flexpoint.core.ext.ExtAbility;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * code+version 选择器，强制业务方实现 CodeVersionResolver。
 * 选择匹配指定code和version的第一个扩展点
 * @author xiangganluo
 */
public class CodeVersionSelector extends CodeSelector {

    public static final String VERSION_TAG_KEY = "version";
    public static final String DEFAULT_VERSION = "1.0.0";

    public CodeVersionSelector(CodeVersionResolver codeVersionResolver) {
        super(codeVersionResolver);
    }

    /**
     * 重写过滤逻辑，先按code过滤，再按version过滤
     */
    @Override
    protected <T extends ExtAbility> List<T> filterByCode(List<T> candidates) {
        // 先按code过滤
        List<T> codeFiltered = super.filterByCode(candidates);
        
        // 如果resolver是CodeVersionResolver，则进一步按版本过滤
        if (resolver instanceof CodeVersionResolver) {
            String targetVersion = Optional.ofNullable(((CodeVersionResolver) resolver).resolveVersion())
                .orElse(DEFAULT_VERSION);
            
            // 安全地过滤版本号
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

    /**
     * 业务方必须实现此接口自定义 code 获取逻辑
     */
    public interface CodeVersionResolver extends CodeSelector.CodeResolver {

        default String resolveVersion() {
            return DEFAULT_VERSION;
        }
    }
}