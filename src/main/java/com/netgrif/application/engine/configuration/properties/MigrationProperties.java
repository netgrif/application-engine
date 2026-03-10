package com.netgrif.application.engine.configuration.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.migration")
public class MigrationProperties {

    /**
     * A list of migration process identifiers or names that should be skipped when applying migration logic.
     * This property allows you to configure specific migrations that should be ignored,
     * typically useful for excluding unnecessary or problematic migrations.
     */
    private Set<String> skip = new LinkedHashSet<>();

    /**
     * Indicates whether caches should be evicted as part of the migration process.
     * This property allows enabling or disabling the cache eviction mechanism, which
     * is useful in ensuring consistency and up-to-date data during migration operations.
     * Default value is {@code true}.
     */
    private boolean evictCaches = true;

    /**
     * Specifies whether the application should automatically shut down once the migration process is completed.
     * This property can be used to terminate the application after the migration, ensuring a clean exit
     * if no further operations are intended post-migration.
     * Default value is {@code false}.
     */
    private boolean shutdownAfterMigration = false;

}
