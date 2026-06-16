package com.netgrif.application.engine.migration.model

import com.netgrif.application.engine.configuration.properties.MigrationProperties.ErrorPolicy

/**
 * Configuration class that defines how errors should be handled during migration processes.
 * <p>
 * This policy allows fine-grained control over error handling behavior including:
 * <ul>
 *   <li>When to throw exceptions (immediately, after a limit, after processing, or continue)</li>
 *   <li>Whether to cache encountered errors</li>
 *   <li>Maximum number of errors to tolerate before throwing</li>
 *   <li>Whether to rethrow original exceptions or wrap them</li>
 * </ul>
 * <p>
 * Factory methods are provided for common error handling scenarios.
 *
 * @see MigrationErrorHandlingMode
 */

class MigrationErrorPolicy {

    /**
     * The error handling mode that determines when exceptions should be thrown.
     * Defaults to {@link MigrationErrorHandlingMode#CONTINUE}.
     */
    private MigrationErrorHandlingMode mode = MigrationErrorHandlingMode.CONTINUE

    /**
     * Maximum number of cached errors before throwing.
     * Used when mode is THROW_AFTER_LIMIT.
     */
    private int maxErrors = 0

    /**
     * Whether encountered errors should be stored in the migration error cache.
     */
    private boolean cacheErrors = true

    /**
     * Whether to rethrow the original exception where possible.
     * If false, throw MigrationErrorException with cached errors.
     * Defaults to false.
     */
    private boolean throwOriginal = false

    /**
     * Creates a default error policy based on application configuration properties.
     * This factory method reads error handling settings from the provided migration properties
     * and constructs a MigrationErrorPolicy with those settings.
     *
     * @param migrationProperties the migration configuration properties containing error policy settings
     * @return a new MigrationErrorPolicy configured according to the application properties
     */
    static MigrationErrorPolicy defaultErrorPolicy(ErrorPolicy props) {
        if (props == null || props.mode == null || props.mode.trim().isEmpty()) {
            return new MigrationErrorPolicy()
        }
        MigrationErrorHandlingMode parsedMode
        try {
            parsedMode = MigrationErrorHandlingMode.valueOf(props.mode.trim().toUpperCase(Locale.ROOT))
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid nae.migration.error-policy.mode '${props.mode}'. Supported values: ${MigrationErrorHandlingMode.values()*.name().join(', ')}", ex)
        }
        return new MigrationErrorPolicy(
                mode: parsedMode,
                maxErrors: props.maxErrors,
                cacheErrors: props.cacheErrors,
                throwOriginal: props.throwOriginal
        )
    }

    /**
     * Creates a policy that continues processing even when errors occur.
     * Errors will be cached but will not stop the migration process.
     *
     * @return a new MigrationErrorPolicy configured to continue on error
     */
    static MigrationErrorPolicy continueOnError() {
        return new MigrationErrorPolicy(mode: MigrationErrorHandlingMode.CONTINUE)
    }

    /**
     * Creates a policy that throws an exception immediately when the first error is encountered.
     * This stops the migration process as soon as any error occurs.
     *
     * @return a new MigrationErrorPolicy configured to throw immediately on error
     */
    static MigrationErrorPolicy throwImmediately() {
        return new MigrationErrorPolicy(mode: MigrationErrorHandlingMode.THROW_IMMEDIATELY)
    }

    /**
     * Creates a policy that throws an exception after a specified number of errors have been encountered.
     * This allows the migration to tolerate a limited number of errors before failing.
     *
     * @param maxErrors the maximum number of errors to cache before throwing an exception
     * @return a new MigrationErrorPolicy configured to throw after reaching the error limit
     */
    static MigrationErrorPolicy throwAfterLimit(int maxErrors) {
        if (maxErrors < 0) {
            throw new IllegalArgumentException("maxErrors must be >= 0 for THROW_AFTER_LIMIT")
        }
        return new MigrationErrorPolicy(
                mode: MigrationErrorHandlingMode.THROW_AFTER_LIMIT,
                maxErrors: maxErrors
        )
    }

    /**
     * Creates a policy that completes the migration process and throws an exception afterward if any errors occurred.
     * This allows all migration steps to be attempted before reporting failures.
     *
     * @return a new MigrationErrorPolicy configured to throw after processing completes
     */
    static MigrationErrorPolicy throwAfterProcessing() {
        return new MigrationErrorPolicy(mode: MigrationErrorHandlingMode.THROW_AFTER_PROCESSING)
    }

    /**
     * Gets the current error handling mode.
     *
     * @return the configured error handling mode
     */
    MigrationErrorHandlingMode getMode() {
        return mode
    }

    /**
     * Sets the error handling mode.
     *
     * @param mode the error handling mode to use
     */
    void setMode(MigrationErrorHandlingMode mode) {
        this.mode = mode
    }

    /**
     * Gets the maximum number of errors allowed before throwing an exception.
     * Only relevant when mode is {@link MigrationErrorHandlingMode#THROW_AFTER_LIMIT}.
     *
     * @return the maximum error count threshold
     */
    int getMaxErrors() {
        return maxErrors
    }

    /**
     * Sets the maximum number of errors allowed before throwing an exception.
     *
     * @param maxErrors the maximum error count threshold
     */
    void setMaxErrors(int maxErrors) {
        if (maxErrors < 0) {
            throw new IllegalArgumentException("maxErrors cannot be negative")
        }
        this.maxErrors = maxErrors
    }

    /**
     * Checks whether errors should be cached during migration.
     *
     * @return true if errors should be cached, false otherwise
     */
    boolean getCacheErrors() {
        return cacheErrors
    }

    /**
     * Sets whether errors should be cached during migration.
     *
     * @param cacheErrors true to cache errors, false otherwise
     */
    void setCacheErrors(boolean cacheErrors) {
        this.cacheErrors = cacheErrors
    }

    /**
     * Checks whether original exceptions should be rethrown.
     *
     * @return true if original exceptions should be rethrown, false to wrap them in MigrationErrorException
     */
    boolean getThrowOriginal() {
        return throwOriginal
    }

    /**
     * Sets whether original exceptions should be rethrown.
     *
     * @param throwOriginal true to rethrow original exceptions, false to wrap them in MigrationErrorException
     */
    void setThrowOriginal(boolean throwOriginal) {
        this.throwOriginal = throwOriginal
    }
}
