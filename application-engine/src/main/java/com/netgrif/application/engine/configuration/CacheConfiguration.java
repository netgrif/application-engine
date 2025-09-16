package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.CacheConfigurationProperties;
import com.netgrif.application.engine.configuration.properties.RunnerConfigurationProperties;
import com.netgrif.application.engine.elastic.service.executors.MaxSizeHashMap;
import com.netgrif.application.engine.workflow.domain.CachedFunction;
import groovy.lang.Closure;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
public class CacheConfiguration extends CachingConfigurerSupport {
    private final RunnerConfigurationProperties.FieldRunnerProperties fieldRunnerProperties;
    private final CacheConfigurationProperties properties;

    public CacheConfiguration(RunnerConfigurationProperties.FieldRunnerProperties fieldRunnerProperties, CacheConfigurationProperties properties) {
        this.fieldRunnerProperties = fieldRunnerProperties;
        this.properties = properties;
    }

    @Bean
    @Primary
    @Override
    public CacheManager cacheManager() {
        Set<String> cacheNames = properties.getAllCaches();
        List<Cache> caches = cacheNames.stream()
                .map(ConcurrentMapCache::new)
                .collect(Collectors.toCollection(ArrayList::new));


        Supplier<Map<String, Closure>> actionsFactory =
                () -> Collections.synchronizedMap(new MaxSizeHashMap<>(fieldRunnerProperties.getActionCacheSize()));

        caches.add(new GenericMapCache<>(
                CacheMapKeys.ACTIONS,
                Closure.class,
                actionsFactory
        ));

        Supplier<Map<String, CachedFunction>> functionsFactory =
                () -> Collections.synchronizedMap(new MaxSizeHashMap<>(fieldRunnerProperties.getFunctionsCacheSize()));

        caches.add(new GenericMapCache<>(
                CacheMapKeys.FUNCTIONS,
                CachedFunction.class,
                functionsFactory
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
