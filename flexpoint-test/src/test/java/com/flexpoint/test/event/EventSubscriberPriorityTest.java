package com.flexpoint.test.event;

import com.flexpoint.core.event.DefaultEventBus;
import com.flexpoint.core.event.EventContext;
import com.flexpoint.core.event.EventSubscriber;
import com.flexpoint.core.event.EventType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 修复 3 回归：publish 使用按 priority 升序的不可变快照（不再每次排序），
 * 订阅顺序无关，onEvent 始终按 priority 升序；新增订阅者也参与排序。
 *
 * @author xiangganluo
 */
public class EventSubscriberPriorityTest {

    static class RecordingSubscriber implements EventSubscriber {
        private final String id;
        private final int priority;
        private final List<String> sink;

        RecordingSubscriber(String id, int priority, List<String> sink) {
            this.id = id;
            this.priority = priority;
            this.sink = sink;
        }

        @Override public void onEvent(EventContext ctx) { sink.add(id); }
        @Override public String getName() { return id; }
        @Override public int getPriority() { return priority; }
    }

    @Test
    void publish_invokes_subscribers_in_priority_order_regardless_of_subscribe_order() {
        DefaultEventBus bus = new DefaultEventBus();
        List<String> order = new ArrayList<>();

        // 乱序订阅：优先级 30 -> 10 -> 20
        bus.subscribe(new RecordingSubscriber("c", 30, order));
        bus.subscribe(new RecordingSubscriber("a", 10, order));
        bus.subscribe(new RecordingSubscriber("b", 20, order));

        bus.publish(EventContext.create(EventType.EXT_FOUND));

        Assertions.assertEquals(Arrays.asList("a", "b", "c"), order, "应按 priority 升序调用");

        // 新增更高优先级订阅者后，应参与排序并排在最前
        order.clear();
        bus.subscribe(new RecordingSubscriber("z", 5, order));
        bus.publish(EventContext.create(EventType.EXT_FOUND));
        Assertions.assertEquals(Arrays.asList("z", "a", "b", "c"), order, "新增订阅者应参与排序");

        bus.shutdown();
    }
}
