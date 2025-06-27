package com.flexpoint.common.utils;

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
     * @param code 上下文标识
     * @return 扩展点ID
     */
    public static String getExtensionId(Class<?> extensionType, String code) {
        if (code != null && !code.trim().isEmpty()) {
            return extensionType.getSimpleName() + "_" + code;
        }
        return extensionType.getSimpleName();
    }

}