package com.netgrif.application.engine.migration.helpers

import com.mongodb.client.result.DeleteResult
import com.netgrif.application.engine.configuration.properties.MigrationProperties
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.migration.model.MigrationErrorPolicy
import com.netgrif.application.engine.objects.petrinet.domain.I18nString
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileFieldValue
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileListFieldValue
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.objects.workflow.domain.DataField
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.querydsl.core.types.Predicate
import groovy.util.logging.Slf4j
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.FindAndReplaceOptions
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
    CaseMigrationHelper(@Qualifier("migrationMongoTemplate") MongoTemplate mongoTemplate,
                        MigrationProperties migrationProperties,
                        IPetriNetService petriNetService,
                        IElasticCaseService elasticCaseService,
                        IElasticCaseMappingService elasticCaseMappingService,
                        TaskMigrationHelper taskMigrationHelper) {
        super(Case.class, mongoTemplate, migrationProperties)
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
        return migrationProperties.cases.pageSize
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
        bulkOperations.replaceOne(Query.query(Criteria.where("_id").is(useCase.get_id())), useCase, FindAndReplaceOptions.options().upsert())
    }

    /**
     * Resolves and retrieves the string representation of the ID for the given Case document.
     *
     * @param document The Case document whose ID should be resolved.
     * @return The string representation of the case's ID.
     */
    @Override
    String resolveId(Case document) {
        return document.getStringId()
    }

    /**
     * Updates all cases that match the given filter predicate. The update closure
     * is executed for each matched case.
     *
     * @param update A closure containing the code to execute for each matching case.
     * @param filter A QueryDSL Predicate object specifying the conditions to filter the cases.
     */
    void updateCases(Closure update, Predicate filter, MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Updating cases with filter ${filter.toString()} and update ${update.toString()}")
        iterate(update, null, toQuery(filter), 0, getPageSize(), errorPolicy)
    }

    /**
     * Iterates over all cases that match the given filter predicate. The update closure
     * is executed for each matched case, and the pageProcessed closure is called after each page.
     *
     * @param update A closure containing the code to execute for each matching case.
     * @param pageProcessed A closure executed after processing each page. Defaults to null.
     * @param sleepFor Optional sleep time (in milliseconds) between processing pages. Default is 0ms.
     * @param filter A QueryDSL Predicate object specifying the conditions to filter the cases.
     */
    void iterateCases(Closure update, Closure pageProcessed = null, long sleepFor = 0,
                      Predicate filter, MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting iterateCases with filter: ${filter.toString()}, sleepFor: ${sleepFor}ms")
        iterate(update, pageProcessed, toQuery(filter), sleepFor, getPageSize(), errorPolicy)
    }

    /**
     * Updates all cases of a specific process identified by its process identifier.
     *
     * @param update A closure containing the code to execute for each matching case.
     * @param processIdentifier The identifier of the PetriNet process.
     * @param pageSize Optional page size for processing cases. Default is 100.
     */
    void updateCasesCursor(Closure update, String processIdentifier, int pageSize = 100,
                           MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting updateCasesCursor for processIdentifier: ${processIdentifier}, pageSize: ${pageSize}")
        Query query = new Query(Criteria.where("processIdentifier").is(processIdentifier))
        iterate(update, null, query, 0, pageSize, errorPolicy)
    }

    /**
     * Updates all cases associated with a specific PetriNet identified by its ObjectId.
     * The update closure is executed for each matching case.
     *
     * @param update A closure containing the code to execute for each matching case.
     * @param petriNetObjectId The ObjectId of the PetriNet whose cases should be updated.
     * @param pageSize Optional page size for processing cases. Default is 100.
     * @param errorPolicy Optional error handling policy. Defaults to the default error policy.
     */
    void updateCasesCursor(Closure update, ObjectId petriNetObjectId, int pageSize = 100,
                           MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting updateCasesCursor for petriNetObjectId: ${petriNetObjectId}, pageSize: ${pageSize}")
        Query query = new Query(Criteria.where("petriNetObjectId").is(petriNetObjectId))
        iterate(update, null, query, 0, pageSize, errorPolicy)
    }

    /**
     * Updates all cases in the system. The update closure is executed for each case.
     *
     * @param update A closure containing the code to execute for each case.
     * @param pageSize Optional page size for processing cases. Default is 100.
     */
    void updateAllCasesCursor(Closure update, int pageSize = 100, MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting updateAllCasesCursor with pageSize: ${pageSize}")
        iterate(update, null, new Query(), 0, pageSize, errorPolicy)
    }

    /**
     * Indexes provided case in elasticsearch
     * handles useCase.petriNet internally
     * @param useCase Instance of Case that will be indexed into elasticsearch index
     */
    void elasticIndex(Case useCase, MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting elasticIndex for case: ${useCase.stringId}")
        try {
            PetriNetMigrationHelper.setPetriNet(useCase, petriNetService.get(useCase.petriNetObjectId))
            if (!useCase.petriNet) {
                String message = "Failed to set petriNet for case $useCase.stringId"
                log.error(message)
                handleMigrationError(errorPolicy, "elasticIndex", type, useCase.stringId, message)
                return
            }
            log.trace("Successfully set petriNet for case: ${useCase.stringId}")
            elasticCaseService.indexNow(elasticCaseMappingService.transform(useCase))
        } catch (Exception ex) {
            if (useCase.lastModified == null) {
                log.warn("Creating new lastModified date for $useCase.stringId")
                useCase.lastModified = LocalDateTime.now()
                try {
                    elasticCaseService.indexNow(elasticCaseMappingService.transform(useCase))
                } catch (Exception retryEx) {
                    String message = "Failed to index $useCase.stringId after setting lastModified"
                    log.error(message, retryEx)
                    handleMigrationError(errorPolicy, "elasticIndex", type, useCase.stringId, message, retryEx)
                }
            } else {
                String message = "Failed to index $useCase.stringId"
                log.error(message, ex)
                handleMigrationError(errorPolicy, "elasticIndex", type, useCase.stringId, message, ex)
            }
        }
    }

    /**
     * Delete given data fields from useCase
     * @param useCase Instance of Case
     * @param toDelete List of field IDs that will be deleted from useCase
     */
    static void deleteDataFields(Case useCase, Set<String> toDelete) {
        log.debug("Starting deleteDataFields for case: ${useCase.stringId}, fields to delete: ${toDelete}")
        toDelete.each { dataFieldID ->
            log.trace("Removing data field: ${dataFieldID} from case: ${useCase.stringId}")
            useCase.dataSet.remove(dataFieldID)
        }
    }

    /**
     * Changes value of given data fields from number to text
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    static void changeDataFieldsValueFromNumberToText(Case useCase, Set<String> toChange) {
        log.debug("Starting changeDataFieldsValueFromNumberToText for case: ${useCase.stringId}, fields to change: ${toChange}")
        toChange.each { dataFieldID ->
            DataField dataField = useCase.dataSet[dataFieldID]
            if (dataField?.value != null && dataField.value != "") {
                double value = dataField.value as double
                dataField.value = value as String
                log.trace("Converted field ${dataFieldID} from number ${value} to text in case: ${useCase.stringId}")
            }
        }
    }

    /**
     * Changes value of given data fields from text to number
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    void changeDataFieldsValueFromTextToNumber(Case useCase, Set<String> toChange, MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting changeDataFieldsValueFromTextToNumber for case: ${useCase.stringId}, fields to change: ${toChange}")
        toChange.each { dataFieldID ->
            DataField dataField = useCase.dataSet[dataFieldID]
            if (dataField?.value != null && dataField.value != "") {
                try {
                    def originalValue = dataField.value
                    dataField.value = dataField.value as double
                    log.trace("Converted field ${dataFieldID} from text ${originalValue} to number in case: ${useCase.stringId}")
                } catch (Exception e) {
                    def originalValue = dataField.value
                    dataField.value = null
                    String message = "[${useCase.stringId}] could not convert value ${originalValue} in field ${dataFieldID}"
                    log.error(message, e)
                    handleMigrationError(errorPolicy, "changeDataFieldsValueFromTextToNumber", type, useCase.stringId, message, e)
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
        log.debug("Starting addTextDataFields for case: ${useCase.stringId}, fields to add: ${toAdd.keySet()}")
        toAdd.each { dataFieldID, value ->
            log.trace("Adding text data field ${dataFieldID} with value '${value}' to case: ${useCase.stringId}")
            useCase.dataSet[dataFieldID] = new DataField(value)
        }
    }

    /**
     * Changes value of given data fields from enumeration to multichoice
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    static void changeDataFieldsValueFromEnumerationToMultichoice(Case useCase, Set<String> toChange) {
        log.debug("Starting changeDataFieldsValueFromEnumerationToMultichoice for case: ${useCase.stringId}, fields to change: ${toChange}")
        toChange.each { dataFieldID ->
            DataField dataField = useCase.dataSet[dataFieldID]
            if (!dataField) {
                return
            }
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
                log.trace("Converted field ${dataFieldID} from enumeration to multichoice in case: ${useCase.stringId}")
            }
        }
    }

    /**
     * Adds new choices into enumeration or multichoice field
     * @param useCase Instance of Case
     * @param toAdd Map<field id, list of choices to add into data data field>
     */
    static void addChoices(Case useCase, Map<String, List<String>> toAdd) {
        log.debug("Starting addChoices for case: ${useCase.stringId}, fields: ${toAdd.keySet()}")
        toAdd.each { dataFieldID, newChoices ->
            DataField dataField = useCase.dataSet[dataFieldID]
            if (!dataField) {
                return
            }
            if (dataField.choices == null) {
                dataField.setChoices(new HashSet<I18nString>())
            }

            newChoices.each {
                log.trace("Adding choice '${it}' to field ${dataFieldID} in case: ${useCase.stringId}")
                dataField.choices.add(new I18nString(it))
            }
        }
    }

    /**
     * Removes choices from enumeration or multichoice field
     * @param useCase Instance of Case
     * @param toRemove Map<field id, list of choices to remove from data field>
     */
    static void removeChoices(Case useCase, Map<String, List<String>> toRemove) {
        log.debug("Starting removeChoices for case: ${useCase.stringId}, fields: ${toRemove.keySet()}")
        toRemove.each { dataFieldID, choicesToRemove ->
            log.trace("Removing choices ${choicesToRemove} from field ${dataFieldID} in case: ${useCase.stringId}")
            DataField dataField = useCase.dataSet[dataFieldID]
            if (!dataField) {
                return
            }
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
        log.debug("Starting changeFileFieldToFileList for case: ${useCase.stringId}, field: ${fieldId}")
        FileListFieldValue fileListFieldValue = new FileListFieldValue()
        DataField dataField = useCase.dataSet[fieldId]
        if (!dataField) {
            return
        }
        def existingValue = dataField.value
        if (existingValue != null) {
            fileListFieldValue.namesPaths.add(existingValue as FileFieldValue)
        }
        dataField.value = fileListFieldValue
        log.trace("Converted field ${fieldId} from FileFieldValue to FileListFieldValue in case: ${useCase.stringId}")
    }

    /**
     * Update dataField and dataRef components of given case
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    static void updateCaseComponents(Case useCase, PetriNet net) {
        log.debug("Starting updateCaseComponents for case: ${useCase.stringId}, net: ${net.stringId}")
        Map<String, com.netgrif.application.engine.objects.petrinet.domain.Component> components = PetriNetMigrationHelper.createComponentsMap(net)
        Map<String, Map<String, com.netgrif.application.engine.objects.petrinet.domain.Component>> dataRefComponents = PetriNetMigrationHelper.createDataRefComponentsMap(net)

        useCase.dataSet.each { dataField ->
            if (components[dataField.key]) {
                log.trace("Updating component for field ${dataField.key} in case: ${useCase.stringId}")
                useCase.dataSet[dataField.key].component = components[dataField.key]
            }
            if (dataRefComponents[dataField.key]) {
                log.trace("Updating dataRef components for field ${dataField.key} in case: ${useCase.stringId}")
                useCase.dataSet[dataField.key].dataRefComponents = dataRefComponents[dataField.key]
            }
        }
    }

    /**
     * Updates case permissions from PetriNet
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void updateCasePermissionsFromNet(Case useCase, PetriNet net, boolean updateTasks = false
                                      , MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting updateCasePermissionsFromNet for case: ${useCase.stringId}, net: ${net.stringId}, updateTasks: ${updateTasks}")
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
        log.trace("Updated permissions and enabled roles for case: ${useCase.stringId}")
        if (updateTasks) {
            useCase.tasks.each { taskPair ->
                taskMigrationHelper.updateTaskPermissions(useCase, taskPair, net, errorPolicy)
            }
        }
    }

    /**
     * Removes a case from both MongoDB and Elasticsearch.
     * Deletes the case document from MongoDB and removes its corresponding index entry from Elasticsearch.
     * If the MongoDB deletion is not acknowledged, an error is logged and handled according to the error policy.
     *
     * @param useCase The case instance to be removed from the system.
     * @param errorPolicy The error handling policy to apply if the removal fails. Defaults to the default error policy.
     */
    void removeCase(Case useCase, MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting removeCase for case: ${useCase.stringId}")
        DeleteResult deleteResult = mongoTemplate.remove(useCase)
        log.trace("MongoDB delete result for case ${useCase.stringId}: acknowledged=${deleteResult.wasAcknowledged()}, deletedCount=${deleteResult.deletedCount}")
        if (!deleteResult.wasAcknowledged()) {
            String message = "Failed to delete case ${useCase.stringId} from MongoDB"
            log.error(message)
            handleMigrationError(errorPolicy, "removeCase", type, useCase.stringId, message)
            return
        }
        elasticCaseService.remove(useCase.getStringId())
        log.trace("Successfully removed case ${useCase.stringId} from Elasticsearch")
    }

    /**
     * Changes PetriNet reference in useCase
     * @param useCase Instance of Case
     * @param newNet Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void migratePetriNet(Case useCase, PetriNet newNet, MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting migratePetriNet for case: ${useCase.stringId}, new net: ${newNet.stringId}")
        ProcessResourceId newCaseId = new ProcessResourceId(newNet.getStringId(), useCase.get_id().getObjectId())
        useCase.set_id(newCaseId)
        useCase.setPetriNetObjectId(newNet.objectId)
        log.trace("Updated petriNet reference for case: ${useCase.stringId} to net: ${newNet.stringId}")
    }
}

