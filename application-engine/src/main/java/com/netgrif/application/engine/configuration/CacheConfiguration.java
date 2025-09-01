package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.CacheConfigurationProperties;
import com.netgrif.application.engine.workflow.service.FieldActionsCacheService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
public class CacheConfiguration extends CachingConfigurerSupport {

    private final CacheConfigurationProperties properties;
    private final FieldActionsCacheService fieldActionsCacheService;

    public CacheConfiguration(CacheConfigurationProperties properties, FieldActionsCacheService fieldActionsCacheService) {
        this.properties = properties;
        this.fieldActionsCacheService = fieldActionsCacheService;
    }

    @Bean
    @Primary
    @Override
    public CacheManager cacheManager() {
        Set<String> cacheNames = properties.getAllCaches();

        List<Cache> caches = cacheNames.stream()
                .map(ConcurrentMapCache::new)
                .collect(Collectors.toCollection(ArrayList::new));

        caches.add(new ActionsCacheWrapper(new ConcurrentMapCache("actionsCache"), fieldActionsCacheService));

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
