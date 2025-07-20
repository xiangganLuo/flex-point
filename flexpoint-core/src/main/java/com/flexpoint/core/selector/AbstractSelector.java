package com.flexpoint.core.selector;

import com.flexpoint.common.exception.MultipleExtMatchedException;
import com.flexpoint.core.ext.ExtAbility;

import java.util.List;

/**
 * 选择器抽象基类
 * 提供模板方法，子类实现具体的过滤逻辑
 *
 * @author xiangganluo
 * @version 1.0.0
 * @email xiangganluo@gmail.com
 */
public abstract class AbstractSelector implements Selector {

    @Override
    public <T extends ExtAbility> T select(List<T> candidates) {
        List<T> filtered = filter(candidates);
        
        if (filtered.isEmpty()) {
            return null;
        }
        
        if (filtered.size() == 1) {
            return filtered.get(0);
        }
        
        // 有多个匹配结果，抛出专门的异常
        throw new MultipleExtMatchedException(getName(), filtered.size());
    }

    /**
     * 从候选列表中过滤匹配的扩展点
     * 
     * @param candidates 候选扩展点列表
     * @param <T> 扩展点类型
     * @return 匹配的扩展点列表
     */
    protected abstract <T extends ExtAbility> List<T> filter(List<T> candidates);

}
