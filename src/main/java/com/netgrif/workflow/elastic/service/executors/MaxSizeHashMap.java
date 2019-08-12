package com.netgrif.workflow.elastic.service.executors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class MaxSizeHashMap extends LinkedHashMap<String, ExecutorService> {

    private final long maxSize;

    MaxSizeHashMap(long maxSize) {
        super(16, 0.75f, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, ExecutorService> eldest) {
        return size() > maxSize;
    }
}