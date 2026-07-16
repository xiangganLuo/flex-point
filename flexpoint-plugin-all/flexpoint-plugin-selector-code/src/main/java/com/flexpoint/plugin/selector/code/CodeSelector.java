package com.flexpoint.plugin.selector.code;

import com.flexpoint.common.constants.FlexPointConstants;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.selector.AbstractSelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 官方插件：Code 选择器实现。
 * <p>按业务方提供的 {@link CodeResolver} 解析出的 code 过滤候选扩展点。</p>
 *
 * @author xiangganluo
 */
@Slf4j
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
        log.debug("[{}] 解析上下文 code={}", getName(), code);
        if (code == null) {
            log.debug("[{}] code 为空，返回空候选", getName());
            return Collections.emptyList();
        }
        List<T> result = candidates.stream().filter(ext -> code.equals(ext.getCode())).collect(Collectors.toList());
        log.debug("[{}] 按 code={} 过滤: 候选={}, 命中={}", getName(), code,
                candidates == null ? 0 : candidates.size(), result.size());
        return result;
    }

    @Override
    public String getName() {
        return FlexPointConstants.CODE_SELECTOR_NAME;
    }

    /** 业务方实现用于解析 code 的接口 */
    public interface CodeResolver {
        String resolveCode();
    }
}
