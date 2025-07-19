package com.flexpoint.core.selector.resolves;

import com.flexpoint.core.context.Context;
import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.selector.AbstractSelector;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * code 选择器，强制业务方实现 CodeResolver。
 * 选择匹配指定code的第一个扩展点
 * @author xiangganluo
 */
@RequiredArgsConstructor
public class CodeSelector extends AbstractSelector {

    protected final CodeResolver resolver;

    @Override
    protected <T extends ExtensionAbility> List<T> filter(List<T> candidates, Context context) {
        return filterByCode(candidates, context);
    }

    /**
     * 按code过滤候选者，子类可以覆盖此方法进行进一步过滤
     */
    protected <T extends ExtensionAbility> List<T> filterByCode(List<T> candidates, Context context) {
        if (resolver == null) {
            throw new IllegalStateException(getName() + "Selector中的 Resolver 不能为null，请注册业务自定义实现！");
        }
        String code = resolver.resolveCode(context);
        if (code == null) {
            return Collections.emptyList();
        }
        return candidates.stream().filter(ext -> code.equals(ext.getCode())).collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "codeSelector";
    }

    /**
     * 业务方必须实现此接口自定义 code 获取逻辑
     */
    public interface CodeResolver {

        String resolveCode(Context context);

    }
} 