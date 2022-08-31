package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.CacheProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.*;

public class NaeCacheResolver implements CacheResolver {

    private final CacheManager cacheManager;

    private CacheProperties cacheProperties;

    public NaeCacheResolver(CacheManager cacheManager, CacheProperties cacheProperties) {
        this.cacheManager = cacheManager;
        this.cacheProperties = cacheProperties;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Collection<Cache> caches = new ArrayList<>();
        for(String name : context.getOperation().getCacheNames()) {
            String key;
            if (this.cacheProperties.getCache() != null && this.cacheProperties.getCache().containsKey(name)) {
                key = this.cacheProperties.getCache().get(context.getOperation().getCacheNames().stream().findFirst().get());
            } else {
                key = context.getOperation().getCacheNames().stream().findFirst().get();
            }
            Cache cache = this.cacheManager.getCache(key);
            if (cache != null) {
                caches.add(cache);
            } else {
                ((ConcurrentMapCacheManager) this.cacheManager).setCacheNames(List.of(key));
                caches.add(this.cacheManager.getCache(key));
            }
        }
        return caches;
    }
}
