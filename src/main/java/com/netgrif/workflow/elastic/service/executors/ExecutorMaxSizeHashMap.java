package com.netgrif.workflow.elastic.service.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorMaxSizeHashMap extends MaxSizeHashMap<ExecutorService> {

    private final long threadShutdownTimeout;

    ExecutorMaxSizeHashMap(long maxSize, long threadShutdownTimeout) {
        super(16, maxSize, eldest -> {
            try {
                eldest.shutdown();
                eldest.awaitTermination(threadShutdownTimeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Thread was interrupted while waiting for termination: ", e);
            }
        });
        this.threadShutdownTimeout = threadShutdownTimeout;
    }

    public long getThreadShutdownTimeout() {
        return threadShutdownTimeout;
    }
}
