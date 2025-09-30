package com.netgrif.application.engine.configuration.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.Objects;
import java.util.Set;

public class WorkerConcurrentCacheManager extends ConcurrentMapCacheManager {
    private final Set<String> readOnlyCacheNames;

    public WorkerConcurrentCacheManager() {
        super();
        this.readOnlyCacheNames = Set.of();
    }

    public WorkerConcurrentCacheManager(Set<String> readOnlyCacheNames) {
        super();
        this.readOnlyCacheNames = Set.copyOf(
                Objects.requireNonNull(readOnlyCacheNames, "readOnlyCacheNames must not be null.")
        );
    }

    public WorkerConcurrentCacheManager(Set<String> readOnlyCacheNames, String... cacheNames) {
        super(cacheNames);
        this.readOnlyCacheNames = Set.copyOf(
                Objects.requireNonNull(readOnlyCacheNames, "readOnlyCacheNames must not be null.")
        );
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