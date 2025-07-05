package com.flexpoint.example.springboot.framework.flexpoint;

import com.flexpoint.core.selector.AbstractSelector;
import com.flexpoint.core.selector.SelectionContext;
import com.flexpoint.example.springboot.framework.flexpoint.context.SysAppContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 自定义扩展点选择器
 * 根据当前应用上下文中的appCode来选择对应的扩展点实现
 * @author luoxianggan
 */
@Slf4j
@Component
public class CustomSelector extends AbstractSelector {

    @Override
    protected SelectionContext extractContext() {
        return new SelectionContext(SysAppContext.getAppCode(), null);
    }

    @Override
    public String getName() {
        return "ExampleSelectorStrategy";
    }
}