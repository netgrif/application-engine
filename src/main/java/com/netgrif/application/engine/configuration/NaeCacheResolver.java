package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.CacheProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class NaeCacheResolver implements CacheResolver {

    private final CacheManager cacheManager;

    private final CacheProperties cacheProperties;

    public NaeCacheResolver(CacheManager cacheManager, CacheProperties cacheProperties) {
        this.cacheManager = cacheManager;
        this.cacheProperties = cacheProperties;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Set<String> cacheIds = cacheProperties.getAllCaches();
        return context.getOperation().getCacheNames().stream()
                .filter(cacheIds::contains)
                .map(cacheManager::getCache)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
