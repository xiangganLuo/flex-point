package com.flexpoint.common.utils;

import com.flexpoint.common.constants.FlexPointConstants;

import java.util.Map;

/**
 * 扩展点工具类
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class ExtensionUtil {

    /**
     * 获取扩展点ID
     *
     * @param extensionType 扩展点类型
     * @param context 上下文
     * @return 扩展点ID
     */
    public static String getExtensionId(Class<?> extensionType, Map<String, Object> context) {
        if (context != null && context.containsKey(FlexPointConstants.CODE)) {
            return extensionType.getSimpleName() + "_" + context.get(FlexPointConstants.CODE);
        }
        return extensionType.getSimpleName();
    }

}