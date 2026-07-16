package com.flexpoint.test;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.common.exception.SelectorNotFoundException;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.event.EventType;
import com.flexpoint.core.selector.SelectionResult;
import com.flexpoint.core.selector.Selector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 查找事件语义收敛用例：
 * - 纯读取 getAllExt 不发布 EXT_FOUND/EXT_NOT_FOUND；
 * - findAbility 命中发布 EXT_FOUND（无 EXT_NOT_FOUND），未命中仅发布一次 EXT_NOT_FOUND；
 * - 选择器不存在仅发布 SELECTOR_NOT_FOUND，存在才发布 SELECTOR_FOUND。
 *
 * @author xiangganluo
 */
public class EventSemanticsTest {

    @FpSelector("es")
    interface EsAbility extends ExtAbility {}

    @FpSelector("es")
    interface EsEmptyAbility extends ExtAbility {}

    @FpSelector("missingSelector")
    interface NoSelectorAbility extends ExtAbility {}

    static class EsImpl implements EsAbility {
        @Override public String getCode() { return "a"; }
    }

    static class EsSelector implements Selector {
        @Override public String getName() { return "es"; }
        @Override public <T extends ExtAbility> SelectionResult<T> select(List<T> candidates) {
            T picked = candidates.isEmpty() ? null : candidates.get(0);
            return SelectionResult.of(getName(), candidates, picked);
        }
    }

    private long count(List<EventType> events, EventType type) {
        return events.stream().filter(e -> e == type).count();
    }

    @Test
    void found_path_emits_ext_found_and_selected_no_not_found() {
        FlexPoint fp = FlexPointBuilder.create().build();
        fp.registerSelector(new EsSelector());
        fp.register(new EsImpl());

        List<EventType> events = Collections.synchronizedList(new ArrayList<>());
        fp.getEventBus().subscribe(ctx -> events.add(ctx.getEventType()));

        EsAbility a = fp.findAbility(EsAbility.class);
        Assertions.assertNotNull(a);

        Assertions.assertEquals(1, count(events, EventType.SELECTOR_FOUND));
        Assertions.assertEquals(0, count(events, EventType.SELECTOR_NOT_FOUND));
        Assertions.assertEquals(1, count(events, EventType.EXT_FOUND));
        Assertions.assertEquals(0, count(events, EventType.EXT_NOT_FOUND), "命中路径不应出现 EXT_NOT_FOUND");
        Assertions.assertEquals(1, count(events, EventType.EXT_SELECTED));

        fp.shutdown();
    }

    @Test
    void not_found_path_emits_single_ext_not_found_no_found() {
        FlexPoint fp = FlexPointBuilder.create().build();
        fp.registerSelector(new EsSelector());
        // 不注册 EsEmptyAbility 的实现

        List<EventType> events = Collections.synchronizedList(new ArrayList<>());
        fp.getEventBus().subscribe(ctx -> events.add(ctx.getEventType()));

        EsEmptyAbility a = fp.findAbility(EsEmptyAbility.class);
        Assertions.assertNull(a);

        Assertions.assertEquals(1, count(events, EventType.EXT_NOT_FOUND), "未命中应且仅应发布一次 EXT_NOT_FOUND");
        Assertions.assertEquals(0, count(events, EventType.EXT_FOUND), "未命中不应出现 EXT_FOUND");

        fp.shutdown();
    }

    @Test
    void plain_getter_getAllExt_emits_no_lookup_events() {
        FlexPoint fp = FlexPointBuilder.create().build();
        fp.register(new EsImpl());

        List<EventType> events = Collections.synchronizedList(new ArrayList<>());
        fp.getEventBus().subscribe(ctx -> events.add(ctx.getEventType()));

        fp.getAllExt(EsAbility.class);

        Assertions.assertEquals(0, count(events, EventType.EXT_FOUND), "纯读取不应发布 EXT_FOUND");
        Assertions.assertEquals(0, count(events, EventType.EXT_NOT_FOUND), "纯读取不应发布 EXT_NOT_FOUND");

        fp.shutdown();
    }

    @Test
    void missing_selector_emits_only_selector_not_found() {
        FlexPoint fp = FlexPointBuilder.create().build();

        List<EventType> events = Collections.synchronizedList(new ArrayList<>());
        fp.getEventBus().subscribe(ctx -> events.add(ctx.getEventType()));

        Assertions.assertThrows(SelectorNotFoundException.class,
                () -> fp.findAbility(NoSelectorAbility.class));

        Assertions.assertEquals(1, count(events, EventType.SELECTOR_NOT_FOUND));
        Assertions.assertEquals(0, count(events, EventType.SELECTOR_FOUND), "选择器不存在时不应发布 SELECTOR_FOUND");

        fp.shutdown();
    }
}
