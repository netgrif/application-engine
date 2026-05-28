package com.netgrif.application.engine.migration.helpers

import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.migration.config.properties.MigrationConfigurationProperties
import com.netgrif.application.engine.migration.config.properties.MigrationConfigurationProperties.CaseMigrationProperties
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue
import com.netgrif.application.engine.petrinet.domain.dataset.FileListFieldValue
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.DataField
import com.querydsl.core.types.Predicate
import groovy.util.logging.Slf4j
import org.junit.Assert
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

import java.time.LocalDateTime
import java.util.stream.Collectors 

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
    protected final CaseMigrationProperties caseMigrationProperties

    /**
     * Service for managing PetriNet operations.
     */
    protected final IPetriNetService petriNetService

    /**
     * Service for indexing and managing cases in Elasticsearch.
     */
    protected final IElasticCaseService elasticCaseService

    /**
     * Service for mapping Case objects to Elasticsearch documents.
     */
    protected final IElasticCaseMappingService elasticCaseMappingService

    /**
     * Helper for managing task migrations associated with cases.
     */
    protected final TaskMigrationHelper taskMigrationHelper

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
                        IElasticCaseMappingService elasticCaseMappingService,
                        TaskMigrationHelper taskMigrationHelper) {
        super(Case.class, mongoTemplate)
        this.caseMigrationProperties = migrationConfigurationProperties.cases
        this.petriNetService = petriNetService
        this.elasticCaseService = elasticCaseService
        this.elasticCaseMappingService = elasticCaseMappingService
        this.taskMigrationHelper = taskMigrationHelper
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
     * @param pageSize Optional page size for processing cases. Default is 100.
     */
    void updateCasesCursor(Closure update, String processIdentifier, int pageSize = 100) {
        Query query = new Query(Criteria.where("processIdentifier").is(processIdentifier))
        iterate(update, DEFAULT_PROCESS_OPERATIONS, query, 0, pageSize as int)
    }

    /**
     * Updates all cases in the system. The update closure is executed for each case.
     *
     * @param update A closure containing the code to execute for each case.
     * @param pageSize Optional page size for processing cases. Default is 100.
     */
    void updateAllCasesCursor(Closure update, int pageSize = 100) {
        iterate(update, DEFAULT_PROCESS_OPERATIONS, new Query(), 0, pageSize as int)
    }

    /**
     * Indexes provided case in elasticsearch
     * handles useCase.petriNet internally
     * @param useCase Instance of Case that will be indexed into elasticsearch index
     */
    void elasticIndex(Case useCase) {
        try {
            PetriNetMigrationHelper.setPetriNet(useCase, petriNetService.get(useCase.petriNetObjectId))
            if (!useCase.petriNet) {
                log.error("Failed to set petriNet for case $useCase.stringId")
                return
            }
            elasticCaseService.indexNow(elasticCaseMappingService.transform(useCase))
        } catch (Exception ex) {
            if (useCase.lastModified == null) {
                log.warn("Creating new lastModified date for $useCase.stringId")
                useCase.lastModified = LocalDateTime.now()
                try {
                    elasticCaseService.indexNow(elasticCaseMappingService.transform(useCase))
                } catch (Exception retryEx) {
                    log.error("Failed to index $useCase.stringId after setting lastModified", retryEx)
                }
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
    static void deleteDataFields(Case useCase, Set<String> toDelete) {
        toDelete.each { dataFieldID ->
            useCase.dataSet.remove(dataFieldID)
        }
    }

    /**
     * Changes value of given data fields from number to text
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    static void changeDataFieldsValueFromNumberToText(Case useCase, Set<String> toChange) {
        toChange.each { dataFieldID ->
            DataField dataField = useCase.dataSet[dataFieldID]
            if (dataField?.value != null && dataField.value != "") {
                double value = dataField.value as double
                dataField.value = value as String
            }
        }
    }

    /**
     * Changes value of given data fields from text to number
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    static void changeDataFieldsValueFromTextToNumber(Case useCase, Set<String> toChange) {
        toChange.each { dataFieldID ->
            DataField dataField = useCase.dataSet[dataFieldID]
            if (dataField.value && dataField.value != "") {
                try {
                    dataField.value = dataField.value as double
                } catch (Exception e) {
                    def originalValue = dataField.value
                    dataField.value = null
                    log.error("[${useCase.stringId}] could not convert value ${originalValue} in field ${dataFieldID}", e)
                }
            }
        }
    }

    /**
     * Adds new data fields with their init value into useCase
     * @param useCase Instance of Case
     * @param toAdd Map<field id, init value of field>
     */
    static void addTextDataFields(Case useCase, Map<String, String> toAdd) {
        toAdd.each { dataFieldID, value ->
            useCase.dataSet[dataFieldID] = new DataField(value)
        }
    }

    /**
     * Changes value of given data fields from enumeration to multichoice
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    static void changeDataFieldsValueFromEnumerationToMultichoice(Case useCase, Set<String> toChange) {
        toChange.each { dataFieldID ->
            DataField dataField = useCase.dataSet[dataFieldID]
            if (dataField.value && dataField.value != null) {
                def value
                if (dataField.value instanceof I18nString) {
                    value = dataField.value as I18nString
                } else {
                    value = new I18nString(dataField.value as String)
                }

                def newSet = new HashSet<I18nString>()
                newSet.add(value)
                dataField.value = newSet
            }
        }
    }

    /**
     * Adds new choices into enumeration or multichoice field
     * @param useCase Instance of Case
     * @param toAdd Map<field id, list of choices to add into data data field>
     */
    static void addChoices(Case useCase, Map<String, List<String>> toAdd) {
        toAdd.each { dataFieldID, newChoices ->
            DataField dataField = useCase.dataSet[dataFieldID]
            if (dataField.choices == null) {
                dataField.setChoices(new HashSet<I18nString>())
            }

            newChoices.each {
                dataField.choices.add(new I18nString(it))
            }
        }
    }

    /**
     * Removes choices from enumeration or multichoice field
     * @param useCase Instance of Case
     * @param toAdd Map<field id, list of choices to add into data field>
     */
    static void removeChoices(Case useCase, Map<String, List<String>> toRemove) {
        toRemove.each { dataFieldID, choicesToRemove ->
            DataField dataField = useCase.dataSet[dataFieldID]
            if (dataField.value != null) {
                (dataField.value as Set).removeAll(choicesToRemove)
            }

            if (dataField.choices != null) {
                dataField.choices.removeAll(choicesToRemove)
            }
        }
    }

    /**
     * Changes value from FileFieldValue to FileListFieldValue
     * @param useCase Instance of Case
     * @param fieldId Field ID for value change
     */
    static void changeFileFieldToFileList(Case useCase, String fieldId) {
        FileListFieldValue fileListFieldValue = new FileListFieldValue()
        DataField dataField = useCase.dataSet[fieldId]
        def existingValue = dataField?.value
        if (existingValue != null) {
            fileListFieldValue.namesPaths.add(existingValue as FileFieldValue)
        }
        dataField.value = fileListFieldValue
    }

    /**
     * Update dataField and dataRef components of given case
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    static void updateCaseComponents(Case useCase, PetriNet net) {
        Map<String, com.netgrif.application.engine.petrinet.domain.Component> components = PetriNetMigrationHelper.createComponentsMap(net)
        Map<String, Map<String, com.netgrif.application.engine.petrinet.domain.Component>> dataRefComponents = PetriNetMigrationHelper.createDataRefComponentsMap(net)

        useCase.dataSet.each {dataField ->
            if (components[dataField.key]) {
                useCase.dataSet[dataField.key].component = components[dataField.key]
            }
            if (dataRefComponents[dataField.key]) {
                useCase.dataSet[dataField.key].dataRefComponents = dataRefComponents[dataField.key]
            }
        }
    }

    /**
     * Updates case permissions from PetriNet
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void updateCasePermissionsFromNet(Case useCase, PetriNet net, boolean updateTasks = false) {
        useCase.permissions = net.getPermissions().entrySet().stream()
                .filter(role -> role.getValue().containsKey("delete") || role.getValue().containsKey("view"))
                .map(role -> {
                    Map<String, Boolean> permissionMap = new HashMap<>()
                    if (role.getValue().containsKey("delete"))
                        permissionMap.put("delete", role.getValue().get("delete"))
                    if (role.getValue().containsKey("view")) {
                        permissionMap.put("view", role.getValue().get("view"))
                    }
                    return new AbstractMap.SimpleEntry<>(role.getKey(), permissionMap)
                })
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue))
        useCase.resolveViewRoles()
        useCase.setEnabledRoles(net.getRoles().keySet())
        if (updateTasks) {
            useCase.tasks.each { taskPair ->
                taskMigrationHelper.updateTaskPermissions(useCase, taskPair, net)
            }
        }
    }

    /**
     * Changes PetriNet reference in useCase
     * @param useCase Instance of Case
     * @param newNet Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    static void migratePetriNet(Case useCase, PetriNet newNet) {
        useCase.setPetriNetObjectId(newNet.objectId)
    }
}

