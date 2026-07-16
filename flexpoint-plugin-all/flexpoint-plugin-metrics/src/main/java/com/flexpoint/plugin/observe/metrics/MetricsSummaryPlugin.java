package com.flexpoint.plugin.observe.metrics;

import com.flexpoint.core.ext.ExtAbility;
import com.flexpoint.core.monitor.ExtMetrics;
import com.flexpoint.core.monitor.ExtMonitor;
import com.flexpoint.core.monitor.handler.MonitorHandler;
import com.flexpoint.core.plugin.AbstractPlugin;
import com.flexpoint.core.plugin.PluginContext;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 官方内置：指标汇总插件。
 *
 * <p>作为 {@link MonitorHandler} 挂入监控责任链，累计各 extId 的调用统计
 * （总数/成功/失败/异常/平均耗时），并周期性（{@code intervalSeconds}，默认 60 秒）
 * 用 slf4j info 输出一份汇总。使用守护线程的 {@link ScheduledExecutorService}，
 * 在 {@link #stop()}/{@link #destroy()} 中关闭线程池。</p>
 *
 * <p>提供 {@link #getSnapshot()} 返回当前统计（供测试/查询）。</p>
 *
 * @author xiangganluo
 */
@Slf4j
public final class MetricsSummaryPlugin extends AbstractPlugin implements MonitorHandler {

    public static final String PLUGIN_ID = "observe.metrics";

    /** 默认汇总周期（秒）。 */
    public static final int DEFAULT_INTERVAL_SECONDS = 60;

    private final int intervalSeconds;
    private final ConcurrentHashMap<String, Stat> stats = new ConcurrentHashMap<>();

    private ExtMonitor monitor;
    private ScheduledExecutorService scheduler;

    public MetricsSummaryPlugin() {
        this(DEFAULT_INTERVAL_SECONDS);
    }

    public MetricsSummaryPlugin(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds > 0 ? intervalSeconds : DEFAULT_INTERVAL_SECONDS;
    }

    @Override
    public String getId() {
        return PLUGIN_ID;
    }

    @Override
    public void init(PluginContext context) {
        this.monitor = context.monitor();
        if (this.monitor == null) {
            throw new IllegalStateException("MetricsSummaryPlugin requires ExtMonitor from PluginContext");
        }
        log.debug("[{}] init: intervalSeconds={}", PLUGIN_ID, intervalSeconds);
    }

    @Override
    public void start() {
        monitor.addHandler(this);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "flexpoint-metrics-summary");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(this::summarize, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        log.debug("[{}] start: 已注册监控处理链并启动周期汇总，interval={}s", PLUGIN_ID, intervalSeconds);
    }

    @Override
    public void stop() {
        if (monitor != null) {
            monitor.removeHandler(this);
        }
        shutdownScheduler();
        log.debug("[{}] stop: 已移除监控处理链并关闭汇总线程", PLUGIN_ID);
    }

    @Override
    public void destroy() {
        shutdownScheduler();
        stats.clear();
        this.monitor = null;
    }

    private void shutdownScheduler() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    // ==================== MonitorHandler ====================

    @Override
    public void handleInvocation(ExtAbility extAbility, long duration, boolean success, ExtMetrics metrics) {
        String extId = extId(extAbility);
        if (extId == null) {
            return;
        }
        Stat stat = stats.computeIfAbsent(extId, k -> new Stat());
        stat.total.incrementAndGet();
        stat.totalDuration.addAndGet(duration);
        if (success) {
            stat.success.incrementAndGet();
        } else {
            stat.failure.incrementAndGet();
        }
    }

    @Override
    public void handleException(ExtAbility extAbility, Throwable exception, ExtMetrics metrics) {
        String extId = extId(extAbility);
        if (extId == null) {
            return;
        }
        stats.computeIfAbsent(extId, k -> new Stat()).exception.incrementAndGet();
    }

    private String extId(ExtAbility extAbility) {
        return extAbility != null ? extAbility.getExtId() : null;
    }

    // ==================== 汇总与快照 ====================

    /**
     * 输出一份汇总日志。周期任务调用，也可手动触发（例如测试）。
     */
    public void summarize() {
        Map<String, ExtStatSnapshot> snapshot = getSnapshot();
        if (snapshot.isEmpty()) {
            log.info("METRICS_SUMMARY interval={}s (无调用统计)", intervalSeconds);
            return;
        }
        log.info("METRICS_SUMMARY interval={}s extPoints={}", intervalSeconds, snapshot.size());
        for (ExtStatSnapshot s : snapshot.values()) {
            log.info("METRICS_SUMMARY {}", s);
        }
    }

    /**
     * 获取当前统计快照（不可变副本）。
     *
     * @return extId -> 统计快照
     */
    public Map<String, ExtStatSnapshot> getSnapshot() {
        Map<String, ExtStatSnapshot> result = new LinkedHashMap<>();
        for (Map.Entry<String, Stat> e : stats.entrySet()) {
            Stat s = e.getValue();
            long total = s.total.get();
            long totalDuration = s.totalDuration.get();
            double avg = total == 0 ? 0d : (double) totalDuration / total;
            result.put(e.getKey(), new ExtStatSnapshot(
                    e.getKey(), total, s.success.get(), s.failure.get(), s.exception.get(), avg));
        }
        return result;
    }

    /** 内部可变统计。 */
    private static final class Stat {
        final AtomicLong total = new AtomicLong();
        final AtomicLong success = new AtomicLong();
        final AtomicLong failure = new AtomicLong();
        final AtomicLong exception = new AtomicLong();
        final AtomicLong totalDuration = new AtomicLong();
    }
}
