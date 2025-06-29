package com.flexpoint.common.utils;

/**
 * 扩展点工具类
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class ExtensionUtil {

    /**
     * 生成扩展点ID
     *
     * @param code 业务标识
     * @param version 版本号
     * @return 扩展点ID
     */
    public static String getExtensionId(String code, String version) {
        if (code != null && !code.trim().isEmpty()) {
            if (version != null && !version.trim().isEmpty()) {
                return code + ":" + version;
            }
            return code;
        }
        return "unknown";
    }

    /**
     * 验证扩展点代码是否有效
     *
     * @param code 业务代码
     * @return 是否有效
     */
    public static boolean isValidCode(String code) {
        return code != null && !code.trim().isEmpty();
    }

    /**
     * 验证版本号是否有效
     *
     * @param version 版本号
     * @return 是否有效
     */
    public static boolean isValidVersion(String version) {
        return version != null && !version.trim().isEmpty();
    }

    /**
     * 从扩展点ID中提取业务代码
     *
     * @param extensionId 扩展点ID
     * @return 业务代码
     */
    public static String extractCodeFromId(String extensionId) {
        if (extensionId == null || extensionId.trim().isEmpty()) {
            return null;
        }
        int colonIndex = extensionId.indexOf(':');
        if (colonIndex > 0) {
            return extensionId.substring(0, colonIndex);
        }
        return extensionId;
    }

    /**
     * 从扩展点ID中提取版本号
     *
     * @param extensionId 扩展点ID
     * @return 版本号
     */
    public static String extractVersionFromId(String extensionId) {
        if (extensionId == null || extensionId.trim().isEmpty()) {
            return null;
        }
        int colonIndex = extensionId.indexOf(':');
        if (colonIndex > 0 && colonIndex < extensionId.length() - 1) {
            return extensionId.substring(colonIndex + 1);
        }
        return null;
    }
}