package com.flexpoint.example.java.manager;

import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.context.Context;
import com.flexpoint.core.selector.resolves.CodeVersionSelector;
import com.flexpoint.example.java.ability.impl.LogisticsOrderProcessAbility;
import com.flexpoint.example.java.ability.impl.MallOrderProcessAbility;
import com.flexpoint.example.java.context.AppContext;
import com.flexpoint.example.java.service.OrderService;
import lombok.Getter;

/**
 * Java原生环境下的FlexPoint管理器
 */
public class FlexPointManager {
    private static final FlexPointManager INSTANCE = new FlexPointManager();
    private final FlexPoint flexPoint;
    @Getter
    private final OrderService orderService;

    private FlexPointManager() {
        // 初始化FlexPoint
        this.flexPoint = FlexPointBuilder.create().build();
        
        // 注册选择器
        CodeVersionSelector.CodeVersionResolver resolver = new CodeVersionSelector.CodeVersionResolver() {
            @Override
            public String resolveCode(Context context) {
                return AppContext.getAppCode();
            }
            
            @Override
            public String resolveVersion(Context context) {
                return "1.0.0"; // 默认版本
            }
        };
        
        this.flexPoint.registerSelector(new CodeVersionSelector(resolver));
        
        // 注册扩展点实现
        this.flexPoint.register(new LogisticsOrderProcessAbility());
        this.flexPoint.register(new MallOrderProcessAbility());
        this.orderService = new OrderService(flexPoint);
    }

    public static FlexPointManager getInstance() {
        return INSTANCE;
    }

}