package com.netgrif.application.engine.auth.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Settings of the short-lived in-memory caches used when resolving users, groups and logged users.
 * <p>
 * Converting a user into a {@code LoggedUser} (and loading any actor from the database) resolves every process role and
 * authority by id, one query each. These lookups are cached briefly to avoid repeating them many times per request.
 * Writes done through the owning services clear the cache; the time to live only bounds how long a change made
 * elsewhere (e.g. directly through a repository) can stay invisible.
 * </p>
 * <p>
 * Example:
 * <pre>
 * netgrif.engine.auth.cache.process-role.ttl=30s
 * netgrif.engine.auth.cache.process-role.max-size=10000
 * netgrif.engine.auth.cache.authority.ttl=0s    # a zero or negative ttl disables the cache
 * </pre>
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "netgrif.engine.auth.cache")
public class AuthCacheProperties {

    /**
     * Cache of process roles resolved by id.
     */
    private CacheSettings processRole = new CacheSettings(Duration.ofSeconds(30), 10_000);

    /**
     * Cache of authorities resolved by id.
     */
    private CacheSettings authority = new CacheSettings(Duration.ofSeconds(30), 1_000);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheSettings {

        /**
         * How long an entry stays valid after it was loaded. A zero or negative value disables the cache.
         */
        private Duration ttl;

        /**
         * Maximum number of entries. When the limit is reached the cache is cleared as a whole.
         */
        private int maxSize;
    }
}
