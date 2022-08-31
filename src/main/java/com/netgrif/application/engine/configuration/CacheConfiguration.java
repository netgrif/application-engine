package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.CacheProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableCaching
public class CacheConfiguration extends CachingConfigurerSupport {

    @Autowired
    private CacheProperties properties;

    @Bean
    @Primary
    @Override
    public CacheManager cacheManager() {
        Set<String> names = new HashSet<>();
        if (properties.getCache() != null) {
            names.addAll(properties.getCache().values());
        }
        names.addAll(List.of(properties.getPetriNetById(), properties.getPetriNetCache(), properties.getPetriNetNewest(), properties.getPetriNetByIdentifier()));
        return new ConcurrentMapCacheManager(names.toArray(String[]::new));
    }

    @Bean
    @Primary
    @Override
    public CacheResolver cacheResolver() {
        return new NaeCacheResolver(cacheManager(), properties);
    }
}
