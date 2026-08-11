package com.netgrif.application.engine.configuration.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Configuration properties class for managing migration-related settings in the application.
 * This class is bound to the configuration prefix "nae.migration" and provides various options
 * to control the behavior of migration processes, including skipping specific migrations,
 * cache eviction, and automatic shutdown after migration completion.
 * It also contains nested configuration classes for entity-specific migration settings.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "netgrif.engine.migration")
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

    /**
     * Configuration properties specific to case migration.
     * Contains settings that control how cases are migrated, including pagination options.
     */
    private CaseMigrationProperties cases = new CaseMigrationProperties();

    /**
     * Configuration properties specific to task migration.
     * Contains settings that control how tasks are migrated, including pagination options.
     */
    private TaskMigrationProperties tasks = new TaskMigrationProperties();

    /**
     * Configuration properties specific to Petri net migration.
     * Contains settings that control how Petri nets are migrated, including pagination options.
     */
    private PetriNetMigrationProperties petriNets = new PetriNetMigrationProperties();

    /**
     * Default error handling policy used by migration helpers.
     */
    private ErrorPolicy errorPolicy = new ErrorPolicy();

    /**
     * Configuration properties for case-specific migration settings.
     * This nested configuration class allows fine-tuning of the case migration process.
     */
    @Data
    public static class CaseMigrationProperties {

        /**
         * The number of cases to process in a single page during migration.
         * This controls the batch size for paginated case migration operations.
         * Default value is {@code 100}.
         */
        private int pageSize = 100;
    }

    /**
     * Configuration properties for task-specific migration settings.
     * This nested configuration class allows fine-tuning of the task migration process.
     */
    @Data
    public static class TaskMigrationProperties {

        /**
         * The number of tasks to process in a single page during migration.
         * This controls the batch size for paginated task migration operations.
         * Default value is {@code 100}.
         */
        private int pageSize = 100;
    }

    /**
     * Configuration properties for Petri net-specific migration settings.
     * This nested configuration class allows fine-tuning of the Petri net migration process.
     */
    @Data
    public static class PetriNetMigrationProperties {

        /**
         * The number of Petri nets to process in a single page during migration.
         * This controls the batch size for paginated Petri net migration operations.
         * Default value is {@code 100}.
         */
        private int pageSize = 100;
    }

    /**
     * Configuration properties for error handling policy during migration operations.
     * This nested configuration class defines how errors encountered during migration helper execution
     * should be handled, including whether to throw exceptions immediately, continue processing,
     * or apply error thresholds before terminating the migration process.
     */
    @Data
    public static class ErrorPolicy {

        /**
         * Defines the error handling mode for migration helper operations.
         * This property controls the behavior when errors are encountered during migration.
         * <p>
         * Supported values:
         * <ul>
         *   <li><b>THROW_IMMEDIATELY</b> - Throws an exception as soon as the first error occurs, halting migration immediately.</li>
         *   <li><b>CONTINUE</b> - Continues processing despite errors, logging them without interrupting the migration flow.</li>
         *   <li><b>THROW_AFTER_LIMIT</b> - Continues processing until the number of errors reaches the threshold specified by {@code maxErrors}, then throws an exception.</li>
         *   <li><b>THROW_AFTER_PROCESSING</b> - Completes the entire migration process and throws an exception at the end if any errors were encountered.</li>
         * </ul>
         * Default value is {@code "CONTINUE"}.
         */
        private String mode = "CONTINUE";

        /**
         * The maximum number of errors allowed before throwing an exception during migration.
         * This property is only applicable when the {@code mode} is set to {@code THROW_AFTER_LIMIT}.
         * When the number of encountered errors reaches this threshold, an exception will be thrown
         * to halt further processing. A value of {@code 0} means no limit is enforced (though this
         * effectively makes THROW_AFTER_LIMIT behave like THROW_AFTER_PROCESSING).
         * Default value is {@code 0}.
         */
        private int maxErrors = 0;

        /**
         * Indicates whether errors encountered during migration should be cached for later analysis or processing.
         * When enabled, all migration helper errors are stored in memory, allowing developers to review
         * and analyze the errors after the migration completes. This is particularly useful for debugging
         * and post-migration validation, as it provides a complete error history without interrupting the migration flow.
         * Default value is {@code true}.
         */
        private boolean cacheErrors = true;

        /**
         * Indicates whether the original exception should be rethrown instead of a wrapped exception.
         * When set to {@code true}, the migration framework will attempt to rethrow the original exception
         * that occurred during helper execution, preserving the original stack trace and exception type.
         * When {@code false}, exceptions may be wrapped in a migration-specific exception type.
         * This property is useful for maintaining exception transparency and facilitating easier debugging.
         * Default value is {@code false}.
         */
        private boolean throwOriginal = false;
    }
}
