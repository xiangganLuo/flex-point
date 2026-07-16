package com.flexpoint.plugin.observe.metrics;

import com.flexpoint.core.ext.ExtAbility;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MetricsSummaryPlugin} 单元测试。
 */
class MetricsSummaryPluginTest {

    /** 测试用扩展点。 */
    static class TestExt implements ExtAbility {
        private final String id;

        TestExt(String id) {
            this.id = id;
        }

        @Override
        public String getCode() {
            return "test-code";
        }

        @Override
        public String getExtId() {
            return id;
        }
    }

    @Test
    void accumulatesInvocationStatsPerExtId() {
        MetricsSummaryPlugin plugin = new MetricsSummaryPlugin(1);
        TestExt a = new TestExt("ExtA");
        TestExt b = new TestExt("ExtB");

        plugin.handleInvocation(a, 100L, true, null);
        plugin.handleInvocation(a, 300L, true, null);
        plugin.handleInvocation(a, 200L, false, null);
        plugin.handleException(a, new RuntimeException("boom"), null);
        plugin.handleInvocation(b, 50L, true, null);

        Map<String, ExtStatSnapshot> snapshot = plugin.getSnapshot();
        assertEquals(2, snapshot.size());

        ExtStatSnapshot sa = snapshot.get("ExtA");
        assertNotNull(sa);
        assertEquals(3, sa.getTotal());
        assertEquals(2, sa.getSuccess());
        assertEquals(1, sa.getFailure());
        assertEquals(1, sa.getException());
        // 平均耗时 = (100 + 300 + 200) / 3 = 200.0
        assertEquals(200.0, sa.getAverageDurationMs(), 0.0001);
        // 成功率 = 2/3
        assertEquals(2.0 / 3.0, sa.getSuccessRate(), 0.0001);

        ExtStatSnapshot sb = snapshot.get("ExtB");
        assertNotNull(sb);
        assertEquals(1, sb.getTotal());
        assertEquals(1, sb.getSuccess());
        assertEquals(50.0, sb.getAverageDurationMs(), 0.0001);
    }

    @Test
    void snapshotIsIndependentCopy() {
        MetricsSummaryPlugin plugin = new MetricsSummaryPlugin();
        TestExt a = new TestExt("ExtA");
        plugin.handleInvocation(a, 100L, true, null);

        Map<String, ExtStatSnapshot> first = plugin.getSnapshot();
        assertEquals(1, first.get("ExtA").getTotal());

        // 之后的调用不应影响先前取出的快照
        plugin.handleInvocation(a, 100L, true, null);
        assertEquals(1, first.get("ExtA").getTotal());
        assertEquals(2, plugin.getSnapshot().get("ExtA").getTotal());
    }

    @Test
    void ignoresNullExtAbility() {
        MetricsSummaryPlugin plugin = new MetricsSummaryPlugin();
        plugin.handleInvocation(null, 100L, true, null);
        plugin.handleException(null, new RuntimeException(), null);
        assertTrue(plugin.getSnapshot().isEmpty());
    }

    @Test
    void summarizeDoesNotThrowWhenEmptyOrPopulated() {
        MetricsSummaryPlugin plugin = new MetricsSummaryPlugin();
        // 空统计
        plugin.summarize();
        // 有统计
        plugin.handleInvocation(new TestExt("ExtA"), 100L, true, null);
        plugin.summarize();
    }

    @Test
    void invalidIntervalFallsBackToDefault() {
        // 通过行为验证：非法 interval 不影响统计逻辑
        MetricsSummaryPlugin plugin = new MetricsSummaryPlugin(0);
        plugin.handleInvocation(new TestExt("ExtA"), 10L, true, null);
        assertEquals(1, plugin.getSnapshot().get("ExtA").getTotal());
    }
}
