package com.flexpoint.plugin.observe.slowcall;

/**
 * 慢调用回调接口。
 *
 * <p>当某次扩展点调用耗时超过阈值时被触发，供使用方扩展（如上报监控系统、发送告警）。
 * 默认实现仅输出日志，见 {@link SlowCallPlugin}。</p>
 *
 * @author xiangganluo
 */
@FunctionalInterface
public interface SlowCallListener {

    /**
     * 慢调用发生时回调。
     *
     * @param extId       扩展点唯一标识
     * @param methodName  调用方法名
     * @param durationMs  实际耗时（毫秒）
     * @param thresholdMs 触发阈值（毫秒）
     */
    void onSlowCall(String extId, String methodName, long durationMs, long thresholdMs);
}
