package com.netgrif.application.engine.migration.helpers

import com.mongodb.BulkWriteError
import com.mongodb.BulkWriteException
import com.mongodb.bulk.BulkWriteResult
import com.netgrif.application.engine.migration.config.properties.MigrationConfigurationProperties
import com.netgrif.application.engine.migration.config.properties.MigrationConfigurationProperties.CaseMigrationProperties
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.querydsl.core.types.Predicate
import groovy.util.logging.Slf4j
import lombok.RequiredArgsConstructor
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.util.CloseableIterator
import org.springframework.stereotype.Component

@Slf4j
@Component
@RequiredArgsConstructor
class CaseMigrationHelper {

    public static final String CASE_COLLECTION_NAME = "case"

    private MongoTemplate mongoTemplate

    private CaseRepository caseRepository

    private CaseMigrationProperties caseMigrationProperties

    @Autowired
    void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate
    }

    @Autowired
    void setCaseRepository(CaseRepository caseRepository) {
        this.caseRepository = caseRepository
    }

    @Autowired
    void setCaseMigrationProperties(MigrationConfigurationProperties migrationConfigurationProperties) {
        this.caseMigrationProperties = migrationConfigurationProperties.cases
    }
/**
     * Updates all cases filtered by filter Predicate. Update closure is called on each filtered case.
     * @param update Instance of Closure, which should contain code that will be executed for every Case matched by filter
     * @param filter Instance of Predicate, to filter which cases should be updated
     */
    void updateCases(Closure update, Predicate filter) {
        log.info("Updating cases with filter ${filter.toString()} and update ${update.toString()}")
        iterateCases(update, { Page<Case> cases -> caseRepository.saveAll(cases) }, filter)
    }

    /**
     * Iterates all cases filtered by filter Predicate. Update closure is called on each filtered case. PageProcessed closure is called after each page iteration.
     * @param update Instance of Closure, which should contain code that will be executed for every Case matched by filter (changes made to Case will not be saved automatically, for that use updateCases method)
     * @param sleepFor Optional attribute to set sleep time (in milliseconds) to sleep for after each iterated page. Default 0ms
     * @param filter Instance of Predicate, to filter which cases should be iterated
     */
    void iterateCases(Closure update, Closure pageProcessed = {}, long sleepFor = 0, Predicate filter) {
        long caseCount = caseRepository.count(filter)
        long numOfPages = ((caseCount / caseMigrationProperties.pageSize) + 1) as long
        log.info("Processing cases with filter ${filter.toString()}: $numOfPages pages")
        numOfPages.times { page ->
            log.info("Page $page / $numOfPages")

            Page<Case> cases = caseRepository.findAll(filter, PageRequest.of(page, caseMigrationProperties.pageSize))

            cases.each {
                log.debug("Processing case with id ${it.stringId}")
                log.trace("Processing case ${it.toString()}")
                update(it)
            }
            pageProcessed(cases)
            if (sleepFor != 0) {
                log.debug("Pausing migration for ${sleepFor} milliseconds")
                sleep(sleepFor)
            }
        }
    }

    /**
     * Updates all cases of a given process.
     * @param update Instance of Closure, which should contain code that will be executed for every Case matched by filter
     * @param processIdentifier identifier of PetriNet, to filter which cases should be updated
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateCasesCursor(Closure update, String processIdentifier, int pageSize = caseMigrationProperties.pageSize) {
        Query query = Query.query(Criteria.where("processIdentifier").is(processIdentifier))
        long caseCount = mongoTemplate.count(query, Case.class)

        if (caseCount > 0) {
            long numOfPages = ((caseCount / pageSize) + 1) as long
            long page = 1, currentBatchSize = 0;
            log.info("Migrating process $processIdentifier")
            log.info("Page size: $pageSize")
            log.info("Processing cases: $numOfPages pages")

            query.cursorBatchSize(pageSize)
            query.with(Sort.by(Sort.Direction.ASC, "_id"))
            BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Case.class)

            try (CloseableIterator<Case> cursor = mongoTemplate.stream(query, Case.class)) {
                while (cursor.hasNext()) {
                    prepareUpdateOperation(cursor.next(), update, bulkOps)
                    if (++currentBatchSize == pageSize as long || !cursor.hasNext()) {
                        log.info("Updated case page {} / {}", page, numOfPages)
                        handleBulkOps(bulkOps)
                        bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Case.class)
                        currentBatchSize = 0
                        page++
                    }
                }
            }
        }
    }

    /**
     * Update all cases.
     * @param update Instance of Closure, which should contain code that will be executed for every Case
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateAllCasesCursor(Closure update, double pageSize = 100.0) {
        long caseCount = caseRepository.count()
        long numOfPages = ((caseCount / pageSize) + 1) as long
        log.info("Page size: $pageSize")
        log.info("Processing cases: $numOfPages pages")
        ObjectId lastId = null
        if (caseCount > 0) {
            for (int p = 0; p < numOfPages; p++) {
                try {
                    log.info("Page " + (p + 1) + " / $numOfPages")

                    Query query = new Query()
                    if (lastId == null) {
                        query.skip(0)
                    } else {
                        query.addCriteria(Criteria.where("_id").gt(lastId))
                    }
                    query.limit(pageSize as Integer)

                    List<Case> cases = mongoTemplate.find(query, Case.class)
                    cases.each { update(it) }
                    cases = caseRepository.saveAll(cases)

                    lastId = cases.get(cases.size() - 1).get_id()
                } catch (ArrayIndexOutOfBoundsException e) {
                    log.error("Failed to iterate page " + (p + 1))
                    break
                }
            }
        }
    }

    private static void prepareUpdateOperation(Case useCase, Closure update, BulkOperations bulkOps) {
        log.debug("Updating case with ID ${useCase.stringId}")
        log.trace("Updating case ${useCase.toString()}")
        update(useCase)
        bulkOps.replaceOne(Query.query(Criteria.where("_id").is(useCase.get_id())), useCase)
    }

    private static void handleBulkOps(BulkOperations bulkOps) {
        try {
            BulkWriteResult bulkWriteResult = bulkOps.execute()
            log.debug("Processed bulk write of ${bulkWriteResult.modifiedCount}")
        } catch (BulkWriteException e) {
            log.error("Failed to write bulk operation", e.getMessage())
            e.getWriteErrors().forEach {
                log.error("Error writing document with ID ${it.toString()}. Cause: ${it.getMessage()}")
            }
        }
    }
}
