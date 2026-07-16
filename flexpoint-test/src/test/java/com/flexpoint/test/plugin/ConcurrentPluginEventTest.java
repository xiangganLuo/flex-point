package com.flexpoint.test.plugin;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.common.constants.FlexPointConstants;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.ExtMetrics;
import com.flexpoint.core.plugin.Plugin;
import com.flexpoint.core.plugin.official.observability.ObservabilityPlugin;
import com.flexpoint.core.plugin.official.selector.CodeSelectorPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 并发发布事件下插件稳定性用例（Phase B E3）。
 *
 * <p>在 ObservabilityPlugin 激活下并发调用扩展点，验证事件转监控无异常，
 * 且指标在并发下正确累计（原子计数）。</p>
 *
 * @author xiangganluo
 */
public class ConcurrentPluginEventTest {

    @FpSelector(FlexPointConstants.CODE_SELECTOR_NAME)
    interface Job extends ExtAbility {
        String exec();
    }

    static class JobA implements Job {
        @Override public String getCode() { return "a"; }
        @Override public String exec() { return "ok"; }
    }

    @Test
    void concurrent_invocations_record_metrics_without_error() throws Exception {
        List<Plugin> ps = new ArrayList<>();
        ps.add(new ObservabilityPlugin());
        ps.add(new CodeSelectorPlugin(() -> "a"));
        FlexPoint fp = FlexPointBuilder.create().withPlugins(ps).build();

        JobA a = new JobA();
        fp.register(a);
        final Job proxy = fp.findAbility(Job.class);

        final int threads = 8;
        final int perThread = 250;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        proxy.exec();
                    }
                } catch (Throwable e) {
                    failure.compareAndSet(null, e);
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        Assertions.assertTrue(done.await(30, TimeUnit.SECONDS), "并发调用应在超时前完成");
        pool.shutdown();
        Assertions.assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

        Assertions.assertNull(failure.get(), () -> "并发调用不应抛异常: " + failure.get());

        ExtMetrics metrics = fp.getExtMetrics(a);
        Assertions.assertNotNull(metrics);
        Assertions.assertEquals(threads * perThread, metrics.getTotalInvocations(),
                "并发调用下指标应正确累计");
        Assertions.assertEquals(threads * perThread, metrics.getSuccessInvocations());

        fp.shutdown();
    }
}
