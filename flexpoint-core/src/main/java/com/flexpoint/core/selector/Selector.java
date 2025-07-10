package com.flexpoint.core.selector;

import com.flexpoint.core.context.Context;
import com.flexpoint.core.extension.ExtensionAbility;
import java.util.List;

/**
 * 选择器接口，所有选择器必须实现。
 */
public interface Selector {
    <T extends ExtensionAbility> T select(List<T> candidates, Context context);
    String getName();
}