package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.cache.CacheMapKeys;
import com.netgrif.application.engine.configuration.cache.WorkerConcurrentCacheManager;
import com.netgrif.application.engine.configuration.properties.CacheConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
@EnableCaching
public class CacheConfiguration extends CachingConfigurerSupport {

    private final CacheConfigurationProperties properties;

    public CacheConfiguration(CacheConfigurationProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Primary
    @Override
    public CacheManager cacheManager() {
        return new WorkerConcurrentCacheManager(List.of(CacheMapKeys.NAMESPACE_FUNCTIONS), properties.getAllCaches().toArray(String[]::new));
    }

    @Bean
    @Primary
    @Override
    public CacheResolver cacheResolver() {
        return new NaeCacheResolver(cacheManager(), properties);
    }
}
