package com.netgrif.application.engine.migration.throwable

import com.netgrif.application.engine.migration.model.MigrationError

/**
 * Exception thrown when one or more migration errors occur during the migration process.
 * <p>
 * This exception extends {@link RuntimeException} and encapsulates a list of {@link MigrationError}
 * objects that provide detailed information about what went wrong during migration.
 * The error list is immutable once the exception is created.
 * </p>
 */
class MigrationErrorException extends RuntimeException {

    private final List<MigrationError> errors

    /**
     * Constructs a new MigrationErrorException with the specified detail message, list of errors, and cause.
     *
     * @param message the detail message describing the overall migration failure
     * @param errors the list of {@link MigrationError} objects detailing individual migration errors;
     *               if null or empty, an empty unmodifiable list will be used
     * @param cause the cause of this exception (a null value is permitted and indicates that the cause
     *              is nonexistent or unknown); defaults to null if not specified
     */
    MigrationErrorException(String message, List<MigrationError> errors, Throwable cause = null) {
        super(message, cause)
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors ?: []))
    }

    /**
     * Returns an unmodifiable list of migration errors that occurred.
     *
     * @return an unmodifiable {@link List} of {@link MigrationError} objects; never null but may be empty
     */
    List<MigrationError> getErrors() {
        return errors
    }
}