package com.netgrif.application.engine.migration.helpers

import com.netgrif.application.engine.migration.config.properties.MigrationConfigurationProperties
import com.netgrif.application.engine.migration.config.properties.MigrationConfigurationProperties.CaseMigrationProperties
import com.netgrif.application.engine.workflow.domain.Case
import com.querydsl.core.types.Predicate
import groovy.util.logging.Slf4j
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

/**
 * Helper class for managing migrations of Case objects in the application.
 * Provides methods for updating and iterating over case objects, filtered
 * by specified conditions, and applying custom update logic using closures.
 *
 * This class extends {@link AbstractMigrationHelper} and utilizes MongoDB
 * for operations on the data.
 */
@Slf4j
@Component
class CaseMigrationHelper extends AbstractMigrationHelper<Case> {

    /**
     * Configuration properties for case migration.
     */
    private CaseMigrationProperties caseMigrationProperties

    /**
     * Constructs a CaseMigrationHelper instance with
     * the provided MongoTemplate and migration configuration properties.
     *
     * @param mongoTemplate MongoTemplate to interact with MongoDB.
     * @param migrationConfigurationProperties Properties for migration configuration, including cases.
     */
    CaseMigrationHelper(MongoTemplate mongoTemplate,
                        MigrationConfigurationProperties migrationConfigurationProperties) {
        super(Case.class, mongoTemplate)
        this.caseMigrationProperties = migrationConfigurationProperties.cases
    }

    /**
     * Retrieves the configured page size for batch processing of cases.
     *
     * @return the page size for case processing.
     */
    @Override
    int getPageSize() {
        return caseMigrationProperties.pageSize
    }

    /**
     * Prepares bulk operations for updating a case. The provided update closure
     * is executed to modify the case, and a replace operation is created.
     *
     * @param useCase The case object to update.
     * @param update A closure containing the update logic to be applied to the case.
     * @param bulkOperations BulkOperations instance used to queue updates for batch processing.
     */
    @Override
    void prepareOperations(Case useCase, Closure update, BulkOperations bulkOperations) {
        log.debug("Updating case with ID ${useCase.stringId}")
        log.trace("Updating case ${useCase.toString()}")
        update(useCase)
        bulkOperations.replaceOne(Query.query(Criteria.where("_id").is(useCase.get_id())), useCase)
    }

    /**
     * Updates all cases that match the given filter predicate. The update closure
     * is executed for each matched case.
     *
     * @param update A closure containing the code to execute for each matching case.
     * @param filter A QueryDSL Predicate object specifying the conditions to filter the cases.
     */
    void updateCases(Closure update, Predicate filter) {
        log.info("Updating cases with filter ${filter.toString()} and update ${update.toString()}")
        iterate(update, DEFAULT_PROCESS_OPERATIONS, toQuery(filter))
    }

    /**
     * Iterates over all cases that match the given filter predicate. The update closure
     * is executed for each matched case, and the pageProcessed closure is called after each page.
     *
     * @param update A closure containing the code to execute for each matching case.
     * @param pageProcessed A closure executed after processing each page. Defaults to DEFAULT_PROCESS_OPERATIONS.
     * @param sleepFor Optional sleep time (in milliseconds) between processing pages. Default is 0ms.
     * @param filter A QueryDSL Predicate object specifying the conditions to filter the cases.
     */
    void iterateCases(Closure update, Closure pageProcessed = DEFAULT_PROCESS_OPERATIONS, long sleepFor = 0, Predicate filter) {
        iterate(update, pageProcessed, toQuery(filter), sleepFor)
    }

    /**
     * Updates all cases of a specific process identified by its process identifier.
     *
     * @param update A closure containing the code to execute for each matching case.
     * @param processIdentifier The identifier of the PetriNet process.
     * @param pageSize Optional page size for processing cases. Default is 100.0.
     */
    void updateCasesCursor(Closure update, String processIdentifier, double pageSize = 100.0) {
        Query query = new Query(Criteria.where("processIdentifier").is(processIdentifier))
        iterate(update, DEFAULT_PROCESS_OPERATIONS, query, 0, pageSize as int)
    }

    /**
     * Updates all cases in the system. The update closure is executed for each case.
     *
     * @param update A closure containing the code to execute for each case.
     * @param pageSize Optional page size for processing cases. Default is 100.0.
     */
    void updateAllCasesCursor(Closure update, double pageSize = 100.0) {
        iterate(update, DEFAULT_PROCESS_OPERATIONS, new Query(), 0, pageSize as int)
    }
}

