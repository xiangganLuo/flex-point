package com.flexpoint.core.selector;

import com.flexpoint.core.ext.ExtAbility;

import java.util.List;

/**
 * 选择器 SPI：从候选扩展点中选出目标。
 *
 * <p>一次 {@link #select(List)} 同时产出「命中实现 + 决策解释 + 结论」（{@link SelectionResult}），
 * 避免选择与解释重复计算，并统一 命中/未命中/歧义 语义。</p>
 *
 * <p>直接实现本接口的选择器可用 {@link SelectionResult#of(String, List, ExtAbility)} 一行返回；
 * 继承 {@link AbstractSelector} 的选择器只需实现 {@code filter(...)}。</p>
 *
 * @author xiangganluo
 */
public interface Selector {

    /**
     * 获取选择器名称（用于注册与查找，全局唯一）。
     */
    String getName();

    /**
     * 从候选列表中选择匹配的扩展点，返回结果对象（含命中、解释与结论）。
     *
     * @param candidates 候选扩展点列表
     * @param <T> 扩展点类型
     * @return 选择结果（不可为 null）
     */
    <T extends ExtAbility> SelectionResult<T> select(List<T> candidates);
}
