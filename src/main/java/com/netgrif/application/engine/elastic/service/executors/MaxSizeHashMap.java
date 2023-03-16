package com.netgrif.application.engine.elastic.service.executors;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class MaxSizeHashMap<T> extends LinkedHashMap<String, T> {

    private final long maxSize;
    private final Consumer<T> removeEldest;

    public MaxSizeHashMap(long maxSize) {
        this(maxSize, null);
    }

    public MaxSizeHashMap(long maxSize, Consumer<T> removeEldest) {
        super();
        this.maxSize = maxSize;
        this.removeEldest = removeEldest;
    }

    public MaxSizeHashMap(int initialCapacity, long maxSize, Consumer<T> removeEldest) {
        super(initialCapacity, 0.75f, true);
        this.maxSize = maxSize;
        this.removeEldest = removeEldest;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, T> eldest) {
        boolean removeEntry = size() > maxSize;
        if (removeEntry && removeEldest != null) {
            removeEldest.accept(eldest.getValue());
        }
        return removeEntry;
    }
}