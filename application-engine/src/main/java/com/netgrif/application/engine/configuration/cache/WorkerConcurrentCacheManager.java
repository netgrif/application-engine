package com.netgrif.application.engine.configuration.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.ArrayList;
import java.util.List;

public class WorkerConcurrentCacheManager extends ConcurrentMapCacheManager {
    private List<String> readOnlyCacheNames;

    public WorkerConcurrentCacheManager() {
        super();
        this.readOnlyCacheNames = new ArrayList<>();
    }

    public WorkerConcurrentCacheManager(List<String> readOnlyCacheNames) {
        super();
        this.readOnlyCacheNames = readOnlyCacheNames;
    }

    public WorkerConcurrentCacheManager(List<String> readOnlyCacheNames, String... cacheNames) {
        super(cacheNames);
        this.readOnlyCacheNames = readOnlyCacheNames;
    }

    @Override
    protected Cache createConcurrentMapCache(String name) {
        Cache base = super.createConcurrentMapCache(name);
        if (readOnlyCacheNames != null && readOnlyCacheNames.contains(name)) {
            return new ReadOnlyCache(base);
        }
        return base;
    }
}