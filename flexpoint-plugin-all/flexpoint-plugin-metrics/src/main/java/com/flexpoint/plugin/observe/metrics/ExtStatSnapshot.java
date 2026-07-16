package com.flexpoint.plugin.observe.metrics;

/**
 * 单个扩展点的统计快照（不可变）。
 *
 * @author xiangganluo
 */
public final class ExtStatSnapshot {

    private final String extId;
    private final long total;
    private final long success;
    private final long failure;
    private final long exception;
    private final double averageDurationMs;

    public ExtStatSnapshot(String extId, long total, long success, long failure,
                           long exception, double averageDurationMs) {
        this.extId = extId;
        this.total = total;
        this.success = success;
        this.failure = failure;
        this.exception = exception;
        this.averageDurationMs = averageDurationMs;
    }

    public String getExtId() {
        return extId;
    }

    public long getTotal() {
        return total;
    }

    public long getSuccess() {
        return success;
    }

    public long getFailure() {
        return failure;
    }

    public long getException() {
        return exception;
    }

    public double getAverageDurationMs() {
        return averageDurationMs;
    }

    /** 成功率（0~1），无调用时返回 0。 */
    public double getSuccessRate() {
        return total == 0 ? 0d : (double) success / total;
    }

    @Override
    public String toString() {
        return "extId=" + extId
                + " total=" + total
                + " success=" + success
                + " failure=" + failure
                + " exception=" + exception
                + " avgMs=" + String.format("%.2f", averageDurationMs)
                + " successRate=" + String.format("%.2f%%", getSuccessRate() * 100);
    }
}
