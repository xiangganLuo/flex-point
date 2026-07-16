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

    /**
     * 产出本次选择的决策解释（v1，调试级）。
     *
     * <p>用于回答「为什么命中 / 为什么未命中」：包含候选快照、过滤链路与命中原因，
     * 供框架在 Debug 级别输出以便排查路由问题。</p>
     *
     * <p>继承 {@link AbstractSelector} 的选择器已提供通用实现；直接实现本接口的选择器
     * 可复用 {@link DecisionExplanation#fromSelection(String, List, ExtAbility)} 一行完成：
     * <pre>{@code
     * @Override
     * public <T extends ExtAbility> DecisionExplanation explain(List<T> candidates) {
     *     return DecisionExplanation.fromSelection(getName(), candidates, select(candidates));
     * }
     * }</pre></p>
     *
     * @param candidates 候选扩展点列表
     * @param <T> 扩展点类型
     * @return 决策解释对象（不可为 null）
     */
    <T extends ExtAbility> DecisionExplanation explain(List<T> candidates);
}