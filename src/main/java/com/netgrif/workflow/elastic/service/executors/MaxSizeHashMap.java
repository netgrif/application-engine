package com.netgrif.workflow.elastic.service.executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class MaxSizeHashMap extends LinkedHashMap<String, ExecutorService> {

    public static final Logger log = LoggerFactory.getLogger(MaxSizeHashMap.class);

    private final long threadShutdownTimeout;
    private final long maxSize;

    MaxSizeHashMap(long maxSize, long threadTimeout) {
        super(16, 0.75f, true);
        this.maxSize = maxSize;
        this.threadShutdownTimeout = threadTimeout;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, ExecutorService> eldest) {
        boolean removeEntry = size() > maxSize;
        if (removeEntry) {
            try {
                eldest.getValue().shutdown();
                eldest.getValue().awaitTermination(threadShutdownTimeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Thread was interrupted while waiting for termination: ", e);
            }
        }
        return removeEntry;
    }
}