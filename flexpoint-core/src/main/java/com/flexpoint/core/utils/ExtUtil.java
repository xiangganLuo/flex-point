package com.flexpoint.core.utils;

import com.flexpoint.core.ext.ExtAbility;

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
public class ExtUtil {

    /**
     * 生成扩展点ID
     *
     * @param extAbility 扩展点实例（规则：扩展点定义类名#子类实现全限定类名)
     * @return 扩展点ID
     */
    public static String getExtId(ExtAbility extAbility) {
        if (extAbility == null) {
            return "unknown";
        }
        Class<? extends ExtAbility> extType = getExtType(extAbility);
        return extType.getSimpleName() + "#" + extAbility.getClass().getName();
    }

    /**
     * 获取扩展点类型
     * 查找实例实现的第一个ExtAbility接口
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends ExtAbility> getExtType(ExtAbility instance) {
        Class<?> clazz = instance.getClass();

        // 检查直接实现的接口
        for (Class<?> iface : clazz.getInterfaces()) {
            if (ExtAbility.class.isAssignableFrom(iface) && iface != ExtAbility.class) {
                return (Class<? extends ExtAbility>) iface;
            }
        }

        // 检查父类的接口
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            for (Class<?> iface : superClass.getInterfaces()) {
                if (ExtAbility.class.isAssignableFrom(iface) && iface != ExtAbility.class) {
                    return (Class<? extends ExtAbility>) iface;
                }
            }
            superClass = superClass.getSuperclass();
        }

        return ExtAbility.class;
    }

    /**
     * 获取扩展点标签值（字符串类型）
     * 
     * @param extAbility 扩展点实例
     * @param tagKey 标签键
     * @return 标签值
     */
    public static String getTag(ExtAbility extAbility, String tagKey) {
        if (extAbility == null || tagKey == null) {
            return null;
        }
        return extAbility.getTags().getString(tagKey);
    }

    /**
     * 获取扩展点标签值（字符串类型，带默认值）
     * 
     * @param extAbility 扩展点实例
     * @param tagKey 标签键
     * @param defaultValue 默认值
     * @return 标签值
     */
    public static String getTag(ExtAbility extAbility, String tagKey, String defaultValue) {
        if (extAbility == null || tagKey == null) {
            return defaultValue;
        }
        return extAbility.getTags().getString(tagKey, defaultValue);
    }

    /**
     * 获取扩展点标签值（整数类型）
     * 
     * @param extAbility 扩展点实例
     * @param tagKey 标签键
     * @return 标签值
     */
    public static Integer getIntTag(ExtAbility extAbility, String tagKey) {
        if (extAbility == null || tagKey == null) {
            return null;
        }
        return extAbility.getTags().getInt(tagKey);
    }

    /**
     * 获取扩展点标签值（布尔类型）
     * 
     * @param extAbility 扩展点实例
     * @param tagKey 标签键
     * @return 标签值
     */
    public static Boolean getBooleanTag(ExtAbility extAbility, String tagKey) {
        if (extAbility == null || tagKey == null) {
            return null;
        }
        return extAbility.getTags().getBoolean(tagKey);
    }

    /**
     * 获取扩展点标签值（集合类型）
     * 
     * @param extAbility 扩展点实例
     * @param tagKey 标签键
     * @return 标签值集合
     */
    public static Set<String> getSetTag(ExtAbility extAbility, String tagKey) {
        if (extAbility == null || tagKey == null) {
            return Collections.emptySet();
        }
        return extAbility.getTags().getSet(tagKey);
    }

    /**
     * 检查扩展点是否包含指定标签
     * 
     * @param extAbility 扩展点实例
     * @param tagKey 标签键
     * @return 是否包含标签
     */
    public static boolean hasTag(ExtAbility extAbility, String tagKey) {
        if (extAbility == null || tagKey == null) {
            return false;
        }
        return extAbility.getTags().has(tagKey);
    }

    /**
     * 检查扩展点标签是否匹配指定值
     * 
     * @param extAbility 扩展点实例
     * @param tagKey 标签键
     * @param expectedValue 期望值
     * @return 是否匹配
     */
    public static boolean matchTag(ExtAbility extAbility, String tagKey, Object expectedValue) {
        if (extAbility == null || tagKey == null) {
            return false;
        }
        return extAbility.getTags().has(tagKey, expectedValue);
    }

    /**
     * 从扩展点列表中过滤具有指定标签的扩展点
     * 
     * @param exts 扩展点列表
     * @param tagKey 标签键
     * @param tagValue 标签值
     * @param <T> 扩展点类型
     * @return 过滤后的扩展点列表
     */
    public static <T extends ExtAbility> List<T> filterByTag(
            List<T> exts, String tagKey, Object tagValue) {
        if (exts == null || exts.isEmpty() || tagKey == null) {
            return Collections.emptyList();
        }
        
        return exts.stream()
            .filter(ext -> matchTag(ext, tagKey, tagValue))
            .collect(Collectors.toList());
    }

    /**
     * 从扩展点列表中查找第一个匹配指定标签的扩展点
     * 
     * @param exts 扩展点列表
     * @param tagKey 标签键
     * @param tagValue 标签值
     * @param <T> 扩展点类型
     * @return 匹配的扩展点，如果没有则返回null
     */
    public static <T extends ExtAbility> T findByTag(
            List<T> exts, String tagKey, Object tagValue) {
        List<T> filtered = filterByTag(exts, tagKey, tagValue);
        return filtered.isEmpty() ? null : filtered.get(0);
    }

}