package com.netgrif.application.engine.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthCachePropertiesTest {

    private static final String PREFIX = "netgrif.engine.auth.cache";

    @Test
    void defaultsMatchPreviousHardcodedValues() {
        AuthCacheProperties properties = new AuthCacheProperties();

        assertEquals(Duration.ofSeconds(30), properties.getProcessRole().getTtl());
        assertEquals(10_000, properties.getProcessRole().getMaxSize());
        assertEquals(Duration.ofSeconds(30), properties.getAuthority().getTtl());
        assertEquals(1_000, properties.getAuthority().getMaxSize());
    }

    @Test
    void bindsOverridesFromProperties() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                PREFIX + ".process-role.ttl", "5m",
                PREFIX + ".process-role.max-size", "50",
                PREFIX + ".authority.ttl", "0s"
        ));

        AuthCacheProperties properties = new Binder(source).bind(PREFIX, AuthCacheProperties.class)
                .orElseGet(AuthCacheProperties::new);

        assertEquals(Duration.ofMinutes(5), properties.getProcessRole().getTtl());
        assertEquals(50, properties.getProcessRole().getMaxSize());
        assertEquals(Duration.ZERO, properties.getAuthority().getTtl());
        // not overridden, keeps its default
        assertEquals(1_000, properties.getAuthority().getMaxSize());
    }
}
