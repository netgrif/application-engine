package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.CacheProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
public class CacheConfiguration extends CachingConfigurerSupport {

    private final CacheProperties properties;

    public CacheConfiguration(CacheProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Primary
    @Override
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(properties.getAllCaches().toArray(String[]::new));
    }

    @Bean
    @Primary
    @Override
    public CacheResolver cacheResolver() {
        return new NaeCacheResolver(cacheManager(), properties);
    }
}
