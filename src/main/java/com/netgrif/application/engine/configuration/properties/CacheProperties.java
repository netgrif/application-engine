package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae")
public class CacheProperties {

    private Map<String, String> cache;

    @Value("${nae.cache.petriNetById:petriNetById}")
    private String petriNetById;

    @Value("${nae.cache.petriNetByIdentifier:petriNetByIdentifier}")
    private String petriNetByIdentifier;

    @Value("${nae.cache.petriNetNewest:petriNetNewest}")
    private String petriNetNewest;

    @Value("${nae.cache.petriNetCache:petriNetCache}")
    private String petriNetCache;
}
