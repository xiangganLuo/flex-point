package com.flexpoint.example.java.manager;

import com.flexpoint.common.constants.FlexPointConstants;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.context.Context;
import com.flexpoint.core.selector.resolves.CodeVersionSelector;
import com.flexpoint.example.java.ability.impl.LogisticsOrderProcessAbility;
import com.flexpoint.example.java.ability.impl.MallOrderProcessAbility;
import com.flexpoint.example.java.context.AppContext;
import com.flexpoint.example.java.service.OrderService;

/**
 * Java原生环境下的FlexPoint管理器
 */
public class FlexPointManager {
    private static final FlexPointManager INSTANCE = new FlexPointManager();
    private final FlexPoint flexPoint;
    private final OrderService orderService;

    private FlexPointManager() {
        // 初始化FlexPoint并注册选择器链
        this.flexPoint = FlexPointBuilder.create().build();
        // 注册解析器
        this.flexPoint.registerSelector(FlexPointConstants.DEFAULT_SELECTOR_CHAIN_NAME, new CodeVersionSelector(new CodeVersionSelector.CodeVersionResolver() {
            @Override
            public String resolveCode(Context context) {
                return AppContext.getAppCode();
            }
        }));
        // 注册扩展器
        this.flexPoint.register(new LogisticsOrderProcessAbility());
        this.flexPoint.register(new MallOrderProcessAbility());
        this.orderService = new OrderService(flexPoint);
    }

    public static FlexPointManager getInstance() {
        return INSTANCE;
    }

    public OrderService getOrderService() {
        return orderService;
    }
}