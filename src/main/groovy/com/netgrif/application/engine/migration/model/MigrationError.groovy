package com.netgrif.application.engine.migration.model

import java.time.LocalDateTime

/**
 * Represents an error that occurred during a migration operation.
 * <p>
 * This class captures detailed information about migration failures including timestamp,
 * the helper class involved, the operation being performed, the entity type and ID,
 * error message, and the underlying cause of the error.
 * </p>
 */
class MigrationError {


    /**
     * The timestamp when the error occurred.
     */
    private LocalDateTime timestamp

    /**
     * The name of the helper class where the error occurred.
     */
    private String helper

    /**
     * The operation being performed when the error occurred.
     */
    private String operation

    /**
     * The type of entity involved in the migration.
     */
    private Class<?> entityType

    /**
     * The ID of the entity involved in the migration.
     */
    private String entityId

    /**
     * A descriptive error message.
     */
    private String message

    /**
     * The underlying exception that caused the error, or null if none.
     */
    private Throwable cause

    /**
     * Constructs a new MigrationError with the specified details.
     *
     * @param timestamp the timestamp when the error occurred
     * @param helper the name of the helper class where the error occurred
     * @param operation the operation being performed when the error occurred
     * @param entityType the type of entity involved in the migration
     * @param entityId the ID of the entity involved in the migration
     * @param message a descriptive error message
     * @param cause the underlying exception that caused the error, or null if none
     */
    MigrationError(LocalDateTime timestamp, String helper, String operation, Class<?> entityType, String entityId, String message, Throwable cause) {
        this.timestamp = timestamp
        this.helper = helper
        this.operation = operation
        this.entityType = entityType
        this.entityId = entityId
        this.message = message
        this.cause = cause
    }

    /**
     * Factory method to create a new MigrationError with the current timestamp.
     *
     * @param helper the name of the helper class where the error occurred
     * @param operation the operation being performed when the error occurred
     * @param entityType the type of entity involved in the migration
     * @param entityId the ID of the entity involved in the migration
     * @param message a descriptive error message
     * @param cause the underlying exception that caused the error (optional, defaults to null)
     * @return a new MigrationError instance with the current timestamp
     */
    static MigrationError of(String helper,
                             String operation,
                             Class<?> entityType,
                             String entityId,
                             String message,
                             Throwable cause = null) {
        return new MigrationError(
                LocalDateTime.now(),
                helper,
                operation,
                entityType,
                entityId,
                message,
                cause
        )
    }

    /**
     * Returns the timestamp when the error occurred.
     *
     * @return the timestamp of the error
     */
    LocalDateTime getTimestamp() {
        return timestamp
    }

    /**
     * Returns the name of the helper class where the error occurred.
     *
     * @return the helper class name
     */
    String getHelper() {
        return helper
    }

    /**
     * Returns the operation being performed when the error occurred.
     *
     * @return the operation name
     */
    String getOperation() {
        return operation
    }

    /**
     * Returns the type of entity involved in the migration.
     *
     * @return the entity type
     */
    String getEntityType() {
        return entityType
    }

    /**
     * Returns the ID of the entity involved in the migration.
     *
     * @return the entity ID
     */
    String getEntityId() {
        return entityId
    }

    /**
     * Returns the descriptive error message.
     *
     * @return the error message
     */
    String getMessage() {
        return message
    }

    /**
     * Returns the underlying exception that caused the error.
     *
     * @return the cause of the error, or null if there is no underlying cause
     */
    Throwable getCause() {
        return cause
    }
}
