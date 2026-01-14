package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.CacheConfigurationProperties;
import com.netgrif.application.engine.workflow.domain.CachedFunction;
import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfiguration implements CachingConfigurer {
    private final CacheConfigurationProperties properties;

    @Bean
    @Primary
    @Override
    public CacheManager cacheManager() {
        Set<String> cacheNames = properties.getAllCaches();
        List<Cache> caches = cacheNames.stream()
                .map(ConcurrentMapCache::new)
                .collect(Collectors.toCollection(ArrayList::new));

        caches.add(new GenericMapCache<>(
                CacheMapKeys.ACTIONS,
                Closure.class,
                null,
                properties.getActionCacheSize()
        ));
        caches.add(new GenericMapCache<>(
                CacheMapKeys.FUNCTIONS,
                CachedFunction.class,
                null,
                properties.getFunctionsCacheSize()
        ));
        caches.add(new GenericMapCache<>(
                CacheMapKeys.GLOBAL_FUNCTIONS,
                List.class,
                CachedFunction.class,
                properties.getGlobalFunctionsCacheSize()
        ));

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caches);
        return cacheManager;
    }

    @Bean
    @Primary
    @Override
    public CacheResolver cacheResolver() {
        return new NaeCacheResolver(cacheManager(), properties);
    }
}
