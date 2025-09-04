package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.CacheConfigurationProperties;
import com.netgrif.application.engine.configuration.properties.RunnerConfigurationProperties;
import com.netgrif.application.engine.elastic.service.executors.MaxSizeHashMap;
import com.netgrif.application.engine.event.IGroovyShellFactory;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.domain.CachedFunction;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
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
    private final IFieldActionsCacheService fieldActionsCacheService;
    private final IPetriNetService petriNetService;
    private final IGroovyShellFactory groovyShellFactory;

    public CacheConfiguration(RunnerConfigurationProperties.FieldRunnerProperties fieldRunnerProperties, CacheConfigurationProperties properties, IFieldActionsCacheService fieldActionsCacheService, IPetriNetService petriNetService, IGroovyShellFactory shellFactory) {
        this.fieldRunnerProperties = fieldRunnerProperties;
        this.properties = properties;
        this.fieldActionsCacheService = fieldActionsCacheService;
        this.petriNetService = petriNetService;
        this.groovyShellFactory = shellFactory;
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

        caches.add(new ActionsMapCache(
                CacheMapKeys.ACTIONS,
                actionsFactory,
                fieldActionsCacheService,
                petriNetService
        ));

        Supplier<Map<String, CachedFunction>> functionsFactory =
                () -> Collections.synchronizedMap(new MaxSizeHashMap<>(fieldRunnerProperties.getFunctionsCacheSize()));

        caches.add(new FunctionsMapCache(
                CacheMapKeys.FUNCTIONS,
                functionsFactory,
                fieldActionsCacheService,
                petriNetService,
                groovyShellFactory
        ));

        Supplier<Map<String, List<CachedFunction>>> nsFactory =
                () -> Collections.synchronizedMap(new MaxSizeHashMap<>(fieldRunnerProperties.getNamespaceCacheSize()));

        caches.add(new FunctionsNamespaceMapCache(
                CacheMapKeys.NAMESPACE_FUNCTIONS,
                nsFactory,
                fieldActionsCacheService,
                petriNetService
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
