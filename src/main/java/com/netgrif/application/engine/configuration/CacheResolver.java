package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.CacheProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.NamedCacheResolver;

import java.util.*;

public class CacheResolver implements org.springframework.cache.interceptor.CacheResolver {

    private final CacheManager cacheManager;

    private CacheProperties cacheProperties;

    public CacheResolver(CacheManager cacheManager, CacheProperties cacheProperties) {
        this.cacheManager = cacheManager;
        this.cacheProperties = cacheProperties;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        String key;
        if (this.cacheProperties.getCache() != null && this.cacheProperties.getCache().containsKey(context.getOperation().getCacheNames().stream().findFirst().get())) {
            key = this.cacheProperties.getCache().get(context.getOperation().getCacheNames().stream().findFirst().get());
        } else {
            key = context.getOperation().getCacheNames().stream().findFirst().get();
        }
        Collection<Cache> caches = new ArrayList<Cache>();
        Cache cache = this.cacheManager.getCache(key);
        if (cache != null) {
            caches.add(cache);
        } else {
            ((ConcurrentMapCacheManager) this.cacheManager).setCacheNames(List.of(key));
            caches.add(this.cacheManager.getCache(key));
        }
        return caches;
    }
}
