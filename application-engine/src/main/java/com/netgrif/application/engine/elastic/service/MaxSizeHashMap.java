package com.netgrif.application.engine.elastic.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Override
    public ExecutorService get(Object o) {
        ExecutorService executor = super.get(o);
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
            put((String) o, executor);
        }
        return executor;
    }
}