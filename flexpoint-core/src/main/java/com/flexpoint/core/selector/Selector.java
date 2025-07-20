package com.flexpoint.core.selector;

import com.flexpoint.core.ext.ExtAbility;
import java.util.List;

/**
 * 选择器接口，所有选择器必须实现。
 * 选择器可以返回多个候选者，由调用方决定如何处理
 * @author xiangganluo
 */
public interface Selector {
    
    /**
     * 从候选列表中选择匹配的扩展点
     * 
     * @param candidates 候选扩展点列表
     * @param <T> 扩展点类型
     * @return 匹配的扩展点
     */
    <T extends ExtAbility> T select(List<T> candidates);

    /**
     * 获取选择器名称
     * 
     * @return 选择器名称，用于注册和查找
     */
    String getName();
}