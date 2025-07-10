package com.flexpoint.core.selector.resolves;

import com.flexpoint.core.extension.ExtensionAbility;
import com.flexpoint.core.context.Context;
import com.flexpoint.core.selector.Selector;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.flexpoint.common.constants.FlexPointConstants.DEFAULT_EXTENSION_VERSION;

/**
 * code+version 选择器，强制业务方实现 CodeVersionResolver。
 * @author luoxianggan
 */
@RequiredArgsConstructor
public class CodeVersionSelector implements Selector {

    private final CodeVersionResolver resolver;

    @Override
    public <T extends ExtensionAbility> T select(List<T> candidates, Context context) {
        if (resolver == null) {
            throw new IllegalStateException("CodeVersionResolver 未实现：请通过 CodeVersionSelector.setResolver() 注册业务自定义实现");
        }
        String code = resolver.resolveCode(context);
        String version = resolver.resolveVersion(context);
        for (T ext : candidates) {
            if (code != null && code.equals(ext.getCode())) {
                if (version == null || version.equals(ext.version())) {
                    return ext;
                }
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "codeVersionSelector";
    }

    /**
     * 业务方必须实现此接口自定义 code 获取逻辑
     */
    public interface CodeVersionResolver {

        String resolveCode(Context context);

        default String resolveVersion(Context context) {
            return DEFAULT_EXTENSION_VERSION;
        };
    }
} 