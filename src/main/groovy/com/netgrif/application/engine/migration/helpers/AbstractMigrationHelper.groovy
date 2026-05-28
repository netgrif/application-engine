package com.netgrif.application.engine.migration.helpers

import com.mongodb.BulkWriteException
import com.mongodb.bulk.BulkWriteResult
import com.netgrif.application.engine.utils.MongodbUtils
import com.netgrif.application.engine.workflow.domain.Case
import com.querydsl.core.types.Predicate
import groovy.util.logging.Slf4j
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.util.CloseableIterator

/**
 * AbstractMigrationHelper is an abstract utility class to facilitate the bulk migration of
 * MongoDB documents. The class provides mechanisms for iterating over documents, preparing 
 * bulk migration operations, and executing those operations efficiently using Spring Data MongoDB's
 * BulkOperations. It is generic and requires the subtype (document type) to be specified.
 *
 * @param <T>  The type of documents this helper will operate on.
 */
@Slf4j
abstract class AbstractMigrationHelper<T> {

    /**
     * Default Closure used to process bulk operations. It uses the {@link #handleBulkOps} method 
     * to safely execute the bulk operations and log results or errors.
     */
    static final Closure DEFAULT_PROCESS_OPERATIONS = { BulkOperations bulkOperations -> handleBulkOps(bulkOperations) }


    /**
     * The type of the documents this helper is operating on.
     * It is expected to be provided by subclasses, as the class itself is generic and requires
     * specific document type initialization to perform the corresponding operations.
     */
    private final Class<T> type

    /**
     * The {@link MongoTemplate} used for interacting with the MongoDB database.
     * This is the core dependency of the helper class, allowing it to execute queries,
     * bulk operations, and other database operations on the specified document type.
     */
    private final MongoTemplate mongoTemplate

    /**
     * Constructs a new AbstractMigrationHelper with the specified MongoTemplate.
     *
     * @param mongoTemplate the {@link MongoTemplate} to use for interacting with MongoDB
     */
    AbstractMigrationHelper(Class<T> type, MongoTemplate mongoTemplate) {
        this.type = type
        this.mongoTemplate = mongoTemplate
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
     * A static method to handle the execution of bulk operations.
     * It executes the given {@link BulkOperations} instance and logs the results or any errors.
     *
     * @param bulkOps the bulk operations to execute
     */
    static void handleBulkOps(BulkOperations bulkOps) {
        try {
            BulkWriteResult bulkWriteResult = bulkOps.execute()
            log.debug("Processed bulk write of ${bulkWriteResult.modifiedCount}")
        } catch (BulkWriteException e) {
            log.error("Failed to write bulk operation", e.getMessage())
            e.getWriteErrors().forEach {
                log.error("Error writing document with ID ${it.toString()}. Cause: ${it.getMessage()}")
            }
            throw e
        }
    }

    /**
     * Converts a QueryDSL {@link Predicate} to a MongoDB {@link Query}.
     *
     * @param predicate the QueryDSL predicate to convert
     * @return a MongoDB Query object representing the predicate
     */
    protected Query toQuery(Predicate predicate) {
        return MongodbUtils.toQuery(mongoTemplate, type, predicate)
    }

    /**
     * Iterates over the documents in the collection, applies updates, and executes bulk operations.
     * The iteration is paginated based on the provided or default page size, and supports customizable 
     * bulk operation processing and optional sleep intervals between pages.
     *
     * @param update a {@link Closure} defining the update to apply to documents
     * @param processOperations an optional {@link Closure} to process bulk operations; defaults 
     *                          to {@link #DEFAULT_PROCESS_OPERATIONS}
     * @param query an optional MongoDB {@link Query} to filter documents; defaults to an empty query
     * @param sleepFor the optional number of milliseconds to sleep between processing pages; defaults to 0
     * @param pageSize the size of each page (number of documents); defaults to the result of {@link #getPageSize()}
     */
    void iterate(Closure update, Closure processOperations = DEFAULT_PROCESS_OPERATIONS,
                 Query query = new Query(), long sleepFor = 0, int pageSize = getPageSize()) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be > 0")
        }
        long count = mongoTemplate.count(query, type)
        if (count > 0) {
            long numOfPages = Math.ceil(count / pageSize) as long
            log.info("Processing ${type.getSimpleName()} documents with filter ${query.toString()}: $numOfPages pages")

            long page = 1, currentBatchSize = 0
            query.cursorBatchSize(pageSize)
            BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, type)

            try (CloseableIterator<T> cursor = mongoTemplate.stream(query, type)) {
                while (cursor.hasNext()) {
                    prepareOperations(cursor.next(), update, bulkOps)
                    if (++currentBatchSize == pageSize as long || !cursor.hasNext()) {
                        log.info("Processed ${type.getSimpleName()} document page {} / {}", page, numOfPages)
                        processOperations(bulkOps)
                        bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, type)
                        currentBatchSize = 0
                        page++
                        if (sleepFor > 0) {
                            log.debug("Pausing migration for ${sleepFor} milliseconds")
                            sleep(sleepFor)
                        }
                    }
                }
            }
        }
    }
}
