package com.flexpoint.core.utils;

import com.flexpoint.core.extension.ExtensionAbility;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 扩展点工具类
 * 提供扩展点ID生成、标签操作、匹配等实用方法
 *
 * @author xiangganluo
 * @version 1.0.0
 */
public class ExtensionUtil {

    /**
     * 生成扩展点ID（直接使用code）
     *
     * @param code 业务标识
     * @return 扩展点ID
     */
    public static String getExtensionId(String code) {
        if (code != null && !code.trim().isEmpty()) {
            return code.trim();
        }
        return "unknown";
    }

    /**
     * 获取扩展点标签值（字符串类型）
     * 
     * @param extensionAbility 扩展点实例
     * @param tagKey 标签键
     * @return 标签值
     */
    public static String getTag(ExtensionAbility extensionAbility, String tagKey) {
        if (extensionAbility == null || tagKey == null) {
            return null;
        }
        return extensionAbility.getTags().getString(tagKey);
    }

    /**
     * 获取扩展点标签值（字符串类型，带默认值）
     * 
     * @param extensionAbility 扩展点实例
     * @param tagKey 标签键
     * @param defaultValue 默认值
     * @return 标签值
     */
    public static String getTag(ExtensionAbility extensionAbility, String tagKey, String defaultValue) {
        if (extensionAbility == null || tagKey == null) {
            return defaultValue;
        }
        return extensionAbility.getTags().getString(tagKey, defaultValue);
    }

    /**
     * 获取扩展点标签值（整数类型）
     * 
     * @param extensionAbility 扩展点实例
     * @param tagKey 标签键
     * @return 标签值
     */
    public static Integer getIntTag(ExtensionAbility extensionAbility, String tagKey) {
        if (extensionAbility == null || tagKey == null) {
            return null;
        }
        return extensionAbility.getTags().getInt(tagKey);
    }

    /**
     * 获取扩展点标签值（布尔类型）
     * 
     * @param extensionAbility 扩展点实例
     * @param tagKey 标签键
     * @return 标签值
     */
    public static Boolean getBooleanTag(ExtensionAbility extensionAbility, String tagKey) {
        if (extensionAbility == null || tagKey == null) {
            return null;
        }
        return extensionAbility.getTags().getBoolean(tagKey);
    }

    /**
     * 获取扩展点标签值（集合类型）
     * 
     * @param extensionAbility 扩展点实例
     * @param tagKey 标签键
     * @return 标签值集合
     */
    public static Set<String> getSetTag(ExtensionAbility extensionAbility, String tagKey) {
        if (extensionAbility == null || tagKey == null) {
            return Collections.emptySet();
        }
        return extensionAbility.getTags().getSet(tagKey);
    }

    /**
     * 检查扩展点是否包含指定标签
     * 
     * @param extensionAbility 扩展点实例
     * @param tagKey 标签键
     * @return 是否包含标签
     */
    public static boolean hasTag(ExtensionAbility extensionAbility, String tagKey) {
        if (extensionAbility == null || tagKey == null) {
            return false;
        }
        return extensionAbility.getTags().has(tagKey);
    }

    /**
     * 检查扩展点标签是否匹配指定值
     * 
     * @param extensionAbility 扩展点实例
     * @param tagKey 标签键
     * @param expectedValue 期望值
     * @return 是否匹配
     */
    public static boolean matchTag(ExtensionAbility extensionAbility, String tagKey, Object expectedValue) {
        if (extensionAbility == null || tagKey == null) {
            return false;
        }
        return extensionAbility.getTags().has(tagKey, expectedValue);
    }

    /**
     * 从扩展点列表中过滤具有指定标签的扩展点
     * 
     * @param extensions 扩展点列表
     * @param tagKey 标签键
     * @param tagValue 标签值
     * @param <T> 扩展点类型
     * @return 过滤后的扩展点列表
     */
    public static <T extends ExtensionAbility> List<T> filterByTag(
            List<T> extensions, String tagKey, Object tagValue) {
        if (extensions == null || extensions.isEmpty() || tagKey == null) {
            return Collections.emptyList();
        }
        
        return extensions.stream()
            .filter(ext -> matchTag(ext, tagKey, tagValue))
            .collect(Collectors.toList());
    }

    /**
     * 从扩展点列表中查找第一个匹配指定标签的扩展点
     * 
     * @param extensions 扩展点列表
     * @param tagKey 标签键
     * @param tagValue 标签值
     * @param <T> 扩展点类型
     * @return 匹配的扩展点，如果没有则返回null
     */
    public static <T extends ExtensionAbility> T findByTag(
            List<T> extensions, String tagKey, Object tagValue) {
        List<T> filtered = filterByTag(extensions, tagKey, tagValue);
        return filtered.isEmpty() ? null : filtered.get(0);
    }

}