package com.flexpoint.test.registry;

import com.flexpoint.common.annotations.FpSelector;
import com.flexpoint.core.FlexPoint;
import com.flexpoint.core.FlexPointBuilder;
import com.flexpoint.core.ext.ExtAbility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 注册中心并发一致性冒烟测试（覆盖 Phase A 工作包 B）。
 *
 * <p>目标：在并发注册与遍历读取同时进行时，
 * 不出现数据竞争异常（如 ConcurrentModificationException），
 * 且最终注册总数与写入次数一致（快照读取语义）。</p>
 *
 * @author xiangganluo
 */
public class ConcurrentRegistryTest {

    @FpSelector("concurrentSelector")
    interface ConcurrentAbility extends ExtAbility {
    }

    static class DemoAbility implements ConcurrentAbility {
        private final String code;
        DemoAbility(String code) { this.code = code; }
        @Override public String getCode() { return code; }
    }

    @Test
    void concurrent_register_and_iterate_are_consistent() throws Exception {
        final FlexPoint flexPoint = FlexPointBuilder.create().build();

        final int writerThreads = 8;
        final int perThread = 500;
        final int readerThreads = 4;

        ExecutorService pool = Executors.newFixedThreadPool(writerThreads + readerThreads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(writerThreads);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicInteger readIterations = new AtomicInteger();
        final AtomicInteger activeWriters = new AtomicInteger(writerThreads);

        // 写线程：并发注册
        for (int t = 0; t < writerThreads; t++) {
            final int base = t;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        flexPoint.register(new DemoAbility("code-" + base + "-" + i));
                    }
                } catch (Throwable e) {
                    failure.compareAndSet(null, e);
                } finally {
                    activeWriters.decrementAndGet();
                    done.countDown();
                }
            });
        }

        // 读线程：并发遍历，验证快照读取不抛异常
        for (int r = 0; r < readerThreads; r++) {
            pool.submit(() -> {
                try {
                    start.await();
                    while (activeWriters.get() > 0) {
                        List<ConcurrentAbility> all = flexPoint.getAllExt(ConcurrentAbility.class);
                        // 触发遍历，快照不应被并发修改影响
                        for (ConcurrentAbility a : all) {
                            a.getCode();
                        }
                        readIterations.incrementAndGet();
                    }
                } catch (Throwable e) {
                    failure.compareAndSet(null, e);
                }
            });
        }

        start.countDown();
        Assertions.assertTrue(done.await(30, TimeUnit.SECONDS), "写线程应在超时前完成");
        pool.shutdown();
        Assertions.assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

        Assertions.assertNull(failure.get(),
                () -> "并发读写不应抛出异常: " + failure.get());
        Assertions.assertEquals(writerThreads * perThread, flexPoint.getExtCount(),
                "最终注册总数应与写入次数一致");
        Assertions.assertTrue(readIterations.get() > 0, "读线程应至少完成一次遍历");

        flexPoint.shutdown();
    }
}
