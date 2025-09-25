package com.netgrif.application.engine.configuration.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

public class WorkerConcurrentCacheManager extends ConcurrentMapCacheManager {
    public WorkerConcurrentCacheManager() {
        super();
    }

    public WorkerConcurrentCacheManager(String... cacheNames) {
        super(cacheNames);
    }

    @Override
    protected Cache createConcurrentMapCache(String name) {
        Cache base = super.createConcurrentMapCache(name);
        if (CacheMapKeys.NAMESPACE_FUNCTIONS.equals(name)) {
            return new ReadOnlyCache(base);
        }
        return base;
    }
}