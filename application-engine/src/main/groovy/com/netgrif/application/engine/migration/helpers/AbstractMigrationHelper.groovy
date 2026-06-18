package com.netgrif.application.engine.migration.helpers

import com.mongodb.BulkWriteException
import com.mongodb.bulk.BulkWriteResult
import com.netgrif.application.engine.configuration.properties.MigrationProperties
import com.netgrif.application.engine.migration.model.MigrationError
import com.netgrif.application.engine.migration.model.MigrationErrorHandlingMode
import com.netgrif.application.engine.migration.model.MigrationErrorPolicy
import com.netgrif.application.engine.migration.throwable.MigrationErrorException
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.utils.MongodbUtils
import com.querydsl.core.types.Predicate
import groovy.util.logging.Slf4j
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.util.CloseableIterator

import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Stream

/**
 * AbstractMigrationHelper is an abstract utility class to facilitate the bulk migration of
 * MongoDB documents. The class provides mechanisms for iterating over documents, preparing 
 * bulk migration operations, and executing those operations efficiently using Spring Data MongoDB's
 * BulkOperations. It is generic and requires the subtype (document type) to be specified.
 *
 * @param <T>   The type of documents this helper will operate on.
 */
@Slf4j
abstract class AbstractMigrationHelper<T> {

    /**
     * The type of the documents this helper is operating on.
     * It is expected to be provided by subclasses, as the class itself is generic and requires
     * specific document type initialization to perform the corresponding operations.
     */
    protected final Class<T> type

    /**
     * The {@link MongoTemplate} used for interacting with the MongoDB database.
     * This is the core dependency of the helper class, allowing it to execute queries,
     * bulk operations, and other database operations on the specified document type.
     */
    protected final MongoTemplate mongoTemplate

    /**
     * Configuration properties for migration operations, providing settings such as error handling policies,
     * page sizes, and other migration-related parameters used throughout the migration process.
     */
    protected final MigrationProperties migrationProperties

    /**
     * A thread-safe list of migration errors that occurred during the migration process.
     * This list stores all errors encountered while processing documents, allowing the migration
     * to continue execution while collecting errors for later review and reporting.
     * The list uses {@link CopyOnWriteArrayList} to ensure thread-safety during concurrent
     * migration operations.
     */
    private final List<MigrationError> migrationErrors

    /**
     * Constructs a new AbstractMigrationHelper with the specified MongoTemplate.
     *
     * @param mongoTemplate the {@link MongoTemplate} to use for interacting with MongoDB
     */
    AbstractMigrationHelper(Class<T> type,
                            MongoTemplate mongoTemplate,
                            MigrationProperties migrationProperties) {
        this.type = type
        this.mongoTemplate = mongoTemplate
        this.migrationProperties = migrationProperties
        this.migrationErrors = new CopyOnWriteArrayList<>()
    }

    /**
     * Returns the page size that should be used for iterating over documents.
     *
     * @return the number of documents per page
     */
    abstract int getPageSize()

    /**
     * Prepares bulk operations on a single document.
     * This method must be implemented by subclasses to define the specific bulk operations 
     * to perform on each document.
     *
     * @param document the document to process
     * @param update the Closure defining the update operation
     * @param bulkOperations the {@link BulkOperations} instance to add operations to
     */
    abstract void prepareOperations(T document, Closure update, BulkOperations bulkOperations)

    /**
     * Resolves and extracts the unique identifier from the given document.
     * This method must be implemented by subclasses to provide the logic for determining
     * the document's ID, which is used for error reporting and logging during migration operations.
     * The implementation should handle the specific ID field structure of the document type.
     *
     * @param document the document from which to resolve the identifier
     * @return the unique identifier of the document as a String, or null if the ID cannot be resolved
     */
    abstract String resolveId(T document)

    /**
     * Caches a migration error into the thread-safe error list for later retrieval and reporting.
     * This method is typically called when an error occurs during document migration operations,
     * allowing the migration process to continue while collecting all errors for review.
     *
     * @param helper the name or identifier of the migration helper where the error occurred
     * @param operation the specific operation being performed when the error occurred
     * @param entityType the type of entity (document type) being migrated
     * @param entityId the unique identifier of the entity that caused the error
     * @param message a descriptive message explaining the error
     * @param cause the optional {@link Throwable} that caused the error; defaults to null
     */
    void cacheError(String helper,
                           String operation,
                           Class<?> entityType,
                           String entityId,
                           String message,
                           Throwable cause = null) {
        migrationErrors.add(MigrationError.of(helper, operation, entityType, entityId, message, cause))
    }

    /**
     * Returns an unmodifiable view of all migration errors collected during the migration process.
     * The returned list is a snapshot of the current errors and will not reflect any subsequent
     * changes to the error cache.
     *
     * @return an unmodifiable {@link List} of {@link MigrationError} objects
     */
    List<MigrationError> getErrors() {
        return Collections.unmodifiableList(new ArrayList<>(migrationErrors))
    }

    /**
     * Retrieves all cached migration errors from this helper instance and clears the error cache.
     * This method is useful for retrieving errors for reporting purposes while simultaneously
     * resetting the error cache for a new migration operation.
     *
     * @return a {@link List} of all {@link MigrationError} objects that were cached
     */
    List<MigrationError> popErrors() {
        synchronized (migrationErrors) {
            List<MigrationError> errors = new ArrayList<>(migrationErrors)
            migrationErrors.clear()
            return errors
        }
    }

/**
 * Clears all cached migration errors from this helper instance.
 * This method should be called to reset this helper's error cache before starting a new migration
 * operation or after errors have been processed and reported.
 */
    void clearErrors() {
        migrationErrors.clear()
    }

    /**
     * Checks whether any migration errors have been cached by this helper instance.
     * This method is useful for quickly determining if any errors occurred during
     * the migration process without retrieving the full error list.
     *
     * @return {@code true} if one or more errors are cached, {@code false} otherwise
     */
    boolean hasErrors() {
        return !migrationErrors.isEmpty()
    }

    /**
     * Handles the execution of bulk operations.
     * It executes the given {@link BulkOperations} instance and logs the results or any errors.
     *
     * @param bulkOps the bulk operations to execute
     */
    void handleBulkOps(BulkOperations bulkOps, Class<?> type) {
        try {
            BulkWriteResult bulkWriteResult = bulkOps.execute()
            log.debug("Processed bulk write of ${bulkWriteResult.modifiedCount}")
        } catch (BulkWriteException e) {
            log.error("Failed to write bulk operation", e)
            e.getWriteErrors().forEach {
                String message = "Error writing document with ID ${it.toString()}. Cause: ${it.getMessage()}"
                log.error(message)
                cacheError(this.class.simpleName, "bulkWrite", type, it.toString(), message, e)
            }
            throw e
        }
    }

    /**
     Iterates over the documents in the collection, applies updates, and executes bulk operations.     * The iteration is paginated based on the provided or default page size, and supports customizable
     * bulk operation processing and optional sleep intervals between pages.
     *
     * @param update a {@link Closure} defining the update to apply to documents
     * @param processOperations an optional {@link Closure} to process bulk operations; defaults 
     *                          to null
     * @param query an optional MongoDB {@link Query} to filter documents; defaults to an empty query
     * @param sleepFor the optional number of milliseconds to sleep between processing pages; defaults to 0
     * @param pageSize the size of each page (number of documents); defaults to the result of {@link #getPageSize()}
     */
    void iterate(Closure update, Closure processOperations = null,
                 Query query = new Query(), long sleepFor = 0, int pageSize = getPageSize(),
                 MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be > 0")
        }
        Closure effectiveProcessOperations = processOperations ?: { BulkOperations bulkOperations, Class<?> entityType ->
            handleBulkOps(bulkOperations, entityType)
        }

        long count = mongoTemplate.count(query, type)
        if (count <= 0) {
            return
        }
        long numOfPages = Math.ceil(count / pageSize) as long
        log.info("Processing ${type.getSimpleName()} documents with filter ${query.toString()}: $numOfPages pages")

        long page = 1, currentBatchSize = 0, currentBulkOpsSize = 0
        query.cursorBatchSize(pageSize)
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, type)

        try (Stream<T> cursorStream = mongoTemplate.stream(query, type)) {
            Iterator<T> cursor = cursorStream.iterator()
            while (cursor.hasNext()) {
                T document = cursor.next()

                try {
                    prepareOperations(document, update, bulkOps)
                    currentBulkOpsSize++
                } catch (Exception e) {
                    String entityId = resolveId(document)
                    String message = "Failed to prepare migration operation for ${type.simpleName} ${entityId}"
                    log.error(message, e)
                    handleMigrationError(errorPolicy, "iterate", type, entityId, message, e)
                }

                if (++currentBatchSize == pageSize as long || !cursor.hasNext()) {
                    log.debug("Processed ${type.getSimpleName()} document page {} / {}", page, numOfPages)

                    try {
                        if (currentBulkOpsSize > 0) {
                            effectiveProcessOperations(bulkOps, type)
                        }
                    } catch (Exception e) {
                        String message = "Failed to process ${type.simpleName} bulk operations on page ${page}"
                        log.error(message, e)
                        handleMigrationError(errorPolicy, "bulkWrite", type, null, message, e)
                    }

                    bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, type)
                    currentBatchSize = 0
                    currentBulkOpsSize = 0
                    page++
                    if (sleepFor > 0) {
                        log.debug("Pausing migration for ${sleepFor} milliseconds")
                        sleep(sleepFor)
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof MigrationErrorException) {
                throw e
            }
            String message = "Failed to iterate ${type.simpleName} documents with filter ${query}"
            log.error(message, e)
            handleMigrationError(errorPolicy, "iterate", type, null, message, e)
            throw e
        } finally {
            finishMigrationErrorPolicy(errorPolicy)
        }

    }

    /**
     * Returns the default migration error policy configured in the application properties.
     * This policy determines how errors should be handled during migration operations,
     * including whether to cache errors, throw exceptions immediately, or continue processing.
     *
     * @return a {@link MigrationErrorPolicy} instance based on the configured migration properties
     */
    MigrationErrorPolicy defaultErrorPolicy() {
        return MigrationErrorPolicy.defaultErrorPolicy(migrationProperties.errorPolicy)
    }

    /**
     * Converts a QueryDSL {@link Predicate} to a MongoDB {@link Query}.
     * This method delegates to the {@link MongodbUtils} utility to perform the conversion,
     * using the current MongoTemplate and document type.
     *
     * @param predicate the QueryDSL predicate to convert
     * @return a MongoDB Query object representing the predicate
     */
    protected Query toQuery(Predicate predicate) {
        return MongodbUtils.toQuery(mongoTemplate, type, predicate)
    }

    /**
     * Handles migration errors according to the specified error policy.
     * This method implements different error handling strategies based on the policy mode,
     * including caching errors, throwing exceptions immediately, throwing after reaching an error limit,
     * or continuing processing to throw after all operations complete.
     *
     * @param policy the {@link MigrationErrorPolicy} defining how to handle the error
     * @param operation the name of the operation being performed when the error occurred
     * @param type the class type of the entity being migrated
     * @param entityId the unique identifier of the entity that caused the error, or null if not applicable
     * @param message a descriptive message explaining the error
     * @param cause the optional {@link Throwable} that caused the error; defaults to null
     * @throws MigrationErrorException if the error policy requires throwing an exception
     */
    protected void handleMigrationError(MigrationErrorPolicy policy, String operation, Class<?> type, String entityId,
                                        String message, Throwable cause = null) {
        if (policy.cacheErrors) {
            cacheError(this.class.simpleName, operation, type, entityId, message, cause)
        }

        switch (policy.mode) {
            case MigrationErrorHandlingMode.THROW_IMMEDIATELY:
                throwError(policy, message, cause)
                break
            case MigrationErrorHandlingMode.THROW_AFTER_LIMIT:
                if (policy.maxErrors > 0 && getErrors().size() >= policy.maxErrors) {
                    throwError(policy, "Migration failed after reaching error limit ${policy.maxErrors}", cause)
                }
                break
            case MigrationErrorHandlingMode.CONTINUE:
                break
            case MigrationErrorHandlingMode.THROW_AFTER_PROCESSING:
                break
        }
    }

    /**
     * Throws an exception based on the specified error policy and error details.
     * If the policy specifies throwing the original exception and the cause is a RuntimeException,
     * the original exception is re-thrown. Otherwise, a new {@link MigrationErrorException} is thrown
     * containing the message, all cached errors, and the original cause.
     *
     * @param policy the {@link MigrationErrorPolicy} defining how to throw the error
     * @param message a descriptive message explaining the error
     * @param cause the optional {@link Throwable} that caused the error; defaults to null
     * @throws RuntimeException or {@link MigrationErrorException} depending on the policy and cause
     */
    protected void throwError(MigrationErrorPolicy policy, String message, Throwable cause = null) {
        if (policy.throwOriginal && cause instanceof RuntimeException) {
            throw cause
        }

        throw new MigrationErrorException(
                message,
                getErrors(),
                cause
        )
    }

    /**
     * Finalizes the migration error policy after processing is complete.
     * If the error policy mode is {@link MigrationErrorHandlingMode#THROW_AFTER_PROCESSING}
     * and errors were collected during processing, throws a {@link MigrationErrorException}
     * containing all cached errors. This method should be called in the finally block
     * of migration operations to ensure proper error handling.
     *
     * @param policy the {@link MigrationErrorPolicy} defining the error handling behavior
     * @throws MigrationErrorException if the policy requires throwing after processing and errors exist
     */
    protected void finishMigrationErrorPolicy(MigrationErrorPolicy policy) {
        if ((policy.mode == MigrationErrorHandlingMode.THROW_AFTER_PROCESSING || (policy.mode == MigrationErrorHandlingMode.THROW_AFTER_LIMIT && policy.maxErrors == 0)) && hasErrors()) {
            throw new MigrationErrorException(
                    "Migration finished with ${getErrors().size()} errors",
                    getErrors()
            )
        }
    }
}
