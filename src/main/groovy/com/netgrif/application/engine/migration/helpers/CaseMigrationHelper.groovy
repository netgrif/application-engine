package com.netgrif.application.engine.migration.helpers

import com.netgrif.application.engine.elastic.service.ElasticCaseMappingService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.migration.config.properties.MigrationConfigurationProperties
import com.netgrif.application.engine.migration.config.properties.MigrationConfigurationProperties.CaseMigrationProperties
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.DataField
import com.querydsl.core.types.Predicate
import groovy.util.logging.Slf4j
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

import java.time.LocalDateTime

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

    private IPetriNetService petriNetService

    private IElasticCaseService elasticCaseService

    private IElasticCaseMappingService elasticCaseMappingService

    /**
     * Constructs a CaseMigrationHelper instance with
     * the provided MongoTemplate and migration configuration properties.
     *
     * @param mongoTemplate MongoTemplate to interact with MongoDB.
     * @param migrationConfigurationProperties Properties for migration configuration, including cases.
     */
    CaseMigrationHelper(MongoTemplate mongoTemplate,
                        MigrationConfigurationProperties migrationConfigurationProperties,
                        IPetriNetService petriNetService,
                        IElasticCaseService elasticCaseService,
                        IElasticCaseMappingService elasticCaseMappingService) {
        super(Case.class, mongoTemplate)
        this.caseMigrationProperties = migrationConfigurationProperties.cases
        this.petriNetService = petriNetService
        this.elasticCaseService = elasticCaseService
        this.elasticCaseMappingService = elasticCaseMappingService
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

    /**
     * Indexes provided case in elasticsearch
     * handles useCase.petriNet internally
     * @param useCase Instance of Case that will be indexed into elasticsearch index
     */
    void elasticIndex(Case useCase) {
        try {
            PetriNetMigrationHelper.setPetriNet(useCase, petriNetService.getNewestVersionByIdentifier(useCase.processIdentifier))
            assert useCase.petriNet
            elasticCaseService.indexNow(elasticCaseMappingService.transform(useCase))
        } catch (Exception ex) {
            if (useCase.lastModified == null) {
                log.error("Creating new lastModified date for $useCase.stringId")
                useCase.lastModified = LocalDateTime.now()
                elasticCaseService.indexNow(elasticCaseMappingService.transform(useCase))
            } else {
                log.error("Failed to index $useCase.stringId", ex)
            }
        }
    }

    /**
     * Delete given data fields from useCase
     * @param useCase Instance of Case
     * @param toDelete List of field IDs that will be deleted from useCase
     */
    void deleteDataFields(Case useCase, List<String> toDelete) {
        toDelete.each { dataFieldID ->
            useCase.dataSet.remove(dataFieldID)
        }
    }

    /**
     * Changes value of given data fields from number to text
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    void changeDataFieldsValueFromNumberToText(Case useCase, List<String> toChange) {
        toChange.each { dataFieldID ->
            if (useCase.dataSet[dataFieldID].value && (useCase.dataSet[dataFieldID].value != null || useCase.dataSet[dataFieldID].value != "")) {
                double value = useCase.dataSet[dataFieldID].value as double
                useCase.dataSet[dataFieldID].value = value as String
            }
        }
    }

    /**
     * Changes value of given data fields from text to number
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    void changeDataFieldsValueFromTextToNumber(Case useCase, List<String> toChange) {
        toChange.each { dataFieldID ->
            if (useCase.dataSet[dataFieldID].value && useCase.dataSet[dataFieldID].value != "") {
                try {
                    useCase.dataSet[dataFieldID].value = useCase.dataSet[dataFieldID].value as double
                } catch (Exception e) {
                    useCase.dataSet[dataFieldID].value = null
                    log.error("[${useCase.stringId}] could not convert value ${useCase.dataSet[dataFieldID].value} in field ${dataFieldID}", e)
                }
            }
        }
    }

    /**
     * Adds new data fields with their init value into useCase
     * @param useCase Instance of Case
     * @param toAdd Map<field id, init value of field>
     */
    void addTextDataFields(Case useCase, Map<String, String> toAdd) {
        toAdd.each { dataFieldID, value ->
            useCase.dataSet[dataFieldID] = new DataField(value)
        }
    }

    /**
     * Changes value of given data fields from enumeration to multichoice
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    void changeDataFieldsValueFromEnumerationToMultichoice(Case useCase, List<String> toChange) {
        toChange.each { dataFieldID ->
            if (useCase.dataSet[dataFieldID].value && useCase.dataSet[dataFieldID].value != null) {
                def value
                if (useCase.dataSet[dataFieldID].value instanceof I18nString) {
                    value = useCase.dataSet[dataFieldID].value as I18nString
                } else {
                    value = new I18nString(useCase.dataSet[dataFieldID].value as String)
                }

                def newSet = new HashSet<I18nString>()
                newSet.add(value)
                useCase.dataSet[dataFieldID].value = newSet
            }
        }
    }

    /**
     * Adds new choices into enumeration or multichoice field
     * @param useCase Instance of Case
     * @param toAdd Map<field id, list of choices to add into data data field>
     */
    void addChoices(Case useCase, Map<String, List<String>> toAdd) {
        toAdd.each { dataFieldID, newChoices ->
            if (useCase.dataSet[dataFieldID].choices == null) {
                useCase.dataSet[dataFieldID].setChoices(new HashSet<I18nString>())
            }

            newChoices.each {
                useCase.dataSet[dataFieldID].choices.add(new I18nString(it))
            }
        }
    }

    /**
     * Removes choices from enumeration or multichoice field
     * @param useCase Instance of Case
     * @param toAdd Map<field id, list of choices to add into data field>
     */
    void removeChoices(Case useCase, Map<String, List<String>> toRemove) {
        toRemove.each { dataFieldID, choicesToRemove ->
            if (useCase.dataSet[dataFieldID].value != null) {
                (useCase.dataSet[dataFieldID].value as Set).removeAll(choicesToRemove)
            }

            if (useCase.dataSet[dataFieldID].choices != null) {
                useCase.dataSet[dataFieldID].choices.removeAll(choicesToRemove)
            }
        }
    }
}

