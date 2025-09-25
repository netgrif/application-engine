package com.netgrif.application.engine.configuration.cache;

import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

public class ReadOnlyCache implements Cache {

    private final Cache cacheDelegate;

    public ReadOnlyCache(Cache cacheDelegate) {
        this.cacheDelegate = cacheDelegate;
    }

    @Override
    public String getName() { return cacheDelegate.getName(); }

    @Override
    public Object getNativeCache() { return cacheDelegate.getNativeCache(); }

    @Override
    public ValueWrapper get(Object key) { return cacheDelegate.get(key); }

    @Override
    public <T> T get(Object key, Class<T> type) { return cacheDelegate.get(key, type); }

    @Override
    public <T> T get(Object key, Callable<T> loader) { return cacheDelegate.get(key, loader); }

    @Override
    public void put(Object key, Object value) { cacheDelegate.put(key, value); }

    @Override
    public void evict(Object key) {
        throw new UnsupportedOperationException("Evict not allowed on worker for " + getName());
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Clear not allowed on worker for " + getName());
    }
}
