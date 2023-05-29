package com.netgrif.application.engine.elastic.service.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorMaxSizeHashMap extends MaxSizeHashMap<ExecutorService> {

    private final long threadShutdownTimeout;

    ExecutorMaxSizeHashMap(long maxSize, long threadShutdownTimeout) {
        super(16, maxSize, eldest -> {
            try {
                eldest.shutdown();
                if (!eldest.awaitTermination(threadShutdownTimeout, TimeUnit.SECONDS)) {
                    eldest.shutdownNow();
                    if (!eldest.awaitTermination(threadShutdownTimeout, TimeUnit.SECONDS)) {
                        log.error("Executor did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                log.error("Thread was interrupted while waiting for termination: ", e);
                eldest.shutdownNow();
            }
        });
        this.threadShutdownTimeout = threadShutdownTimeout;
    }

    public long getThreadShutdownTimeout() {
        return threadShutdownTimeout;
    }
}
