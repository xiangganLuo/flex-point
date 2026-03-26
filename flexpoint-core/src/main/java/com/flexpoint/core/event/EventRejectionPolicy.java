package com.flexpoint.core.event;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 事件总线线程池拒绝策略枚举。
 */
public enum EventRejectionPolicy {
    ABORT,
    DISCARD,
    DISCARD_OLDEST,
    CALLER_RUNS;

    public RejectedExecutionHandler toHandler() {
        switch (this) {
            case ABORT: return new ThreadPoolExecutor.AbortPolicy();
            case DISCARD: return new ThreadPoolExecutor.DiscardPolicy();
            case DISCARD_OLDEST: return new ThreadPoolExecutor.DiscardOldestPolicy();
            case CALLER_RUNS:
            default: return new ThreadPoolExecutor.CallerRunsPolicy();
        }
    }
}

