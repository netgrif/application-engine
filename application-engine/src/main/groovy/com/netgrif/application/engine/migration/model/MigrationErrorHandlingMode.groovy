package com.netgrif.application.engine.migration.model

/**
 * Defines the error handling strategies for migration operations.
 * <p>
 * This enum specifies how errors encountered during migration should be handled,
 * allowing control over whether to fail fast, continue processing, or apply limits.
 */
enum MigrationErrorHandlingMode {
    
    /**
     * Cache/log error and immediately throw.
     */
    THROW_IMMEDIATELY,

    /**
     * Cache/log error and continue migration.
     */
    CONTINUE,

    /**
     * Cache/log error and throw once maxErrors is reached.
     */
    THROW_AFTER_LIMIT,

    /**
     * Cache/log error and continue processing, but throw after the operation finishes if any errors occurred.
     */
    THROW_AFTER_PROCESSING
}