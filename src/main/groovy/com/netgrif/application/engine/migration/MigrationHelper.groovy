package com.netgrif.application.engine.migration

import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.elastic.service.interfaces.*
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.migration.helpers.AbstractMigrationHelper
import com.netgrif.application.engine.migration.helpers.CaseMigrationHelper
import com.netgrif.application.engine.migration.helpers.PetriNetMigrationHelper
import com.netgrif.application.engine.migration.helpers.TaskMigrationHelper
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.Transition
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.*
import com.netgrif.application.engine.petrinet.domain.events.Event
import com.netgrif.application.engine.petrinet.domain.events.EventType
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.service.PetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.workflow.domain.*
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.querydsl.core.types.Predicate
import groovy.util.logging.Slf4j
import org.apache.tomcat.util.http.fileupload.IOUtils
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

import javax.inject.Provider
import java.text.Collator
import java.time.LocalDateTime
import java.util.stream.Collectors

@Slf4j
@Component
class MigrationHelper {

    @Autowired
    private CaseMigrationHelper caseMigrationHelper

    @Autowired
    private TaskMigrationHelper taskMigrationHelper

    @Autowired
    private PetriNetMigrationHelper petriNetMigrationHelper

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private TaskRepository taskRepository

    @Autowired
    private PetriNetService service

    @Autowired
    private Provider<Importer> importerProvider

    @Autowired
    private ProcessRoleRepository roleRepository

    @Autowired
    private PetriNetRepository netRepository

    @Autowired
    private ITaskService taskService

    @Autowired
    private IElasticCaseService elasticCaseService

    @Autowired
    private IElasticCaseMappingService caseMappingService

    @Autowired
    private IElasticTaskService elasticTaskService

    @Autowired
    private IElasticTaskMappingService elasticTaskMappingService

    @Autowired
    private IUserService userService

    @Autowired
    private IElasticIndexService elasticIndexService

    @Autowired
    private MongoTemplate mongoTemplate

    @Autowired
    private IPetriNetService petriNetService

    private Importer getImporter() {
        return importerProvider.get()
    }

    Closure<PetriNet> updateRoleEvents = { PetriNet existing, PetriNet reimported ->
        petriNetMigrationHelper.updateRoleEvents(existing, reimported)
    }

    /**
     * Updates all cases filtered by filter Predicate. Update closure is called on each filtered case.
     * @param update Instance of Closure, which should contain code that will be executed for every Case matched by filter
     * @param filter Instance of Predicate, to filter which cases should be updated
     */
    void updateCases(Closure update, Predicate filter) {
        caseMigrationHelper.updateCases(update, filter)
    }

    /**
     * Iterates all cases filtered by filter Predicate. Update closure is called on each filtered case. PageProcessed closure is called after each page iteration.
     * @param update Instance of Closure, which should contain code that will be executed for every Case matched by filter (changes made to Case will not be saved automatically, for that use updateCases method)
     * @param sleepFor Optional attribute to set sleep time (in milliseconds) to sleep for after each iterated page. Default 0ms
     * @param filter Instance of Predicate, to filter which cases should be iterated
     */
    void iterateCases(Closure update, Closure pageProcessed = AbstractMigrationHelper.DEFAULT_PROCESS_OPERATIONS, long sleepFor = 0, Predicate filter) {
        caseMigrationHelper.iterateCases(update, pageProcessed, sleepFor, filter)
    }

    /**
     * Updates all cases of a given process.
     * @param update Instance of Closure, which should contain code that will be executed for every Case matched by filter
     * @param processIdentifier identifier of PetriNet, to filter which cases should be updated
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateCasesCursor(Closure update, String processIdentifier, double pageSize = 100.0) {
        caseMigrationHelper.updateCasesCursor(update, processIdentifier, pageSize)
    }

    /**
     * Update all cases.
     * @param update Instance of Closure, which should contain code that will be executed for every Case
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateAllCasesCursor(Closure update, double pageSize = 100.0) {
        caseMigrationHelper.updateAllCasesCursor(update, pageSize)
    }

    /**
     * Updates all tasks filtered by filter Predicate. Update closure is called on each filtered task.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param filter Instance of Predicate, to filter which tasks should be updated
     */
    void updateTasks(Closure update, Predicate filter) {
        taskMigrationHelper.updateTasks(update, filter)
    }

    /**
     * Iterates all tasks filtered by filter Predicate. Update closure is called on each filtered task. PageProcessed closure is called after each page iteration.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter (changes made to Task will not be saved automatically, for that use updateCases method)
     * @param sleepFor Optional attribute to set sleep time (in milliseconds) to sleep for after each iterated page. Default 0ms
     * @param filter Instance of Predicate, to filter which tasks should be iterated
     */
    void iterateTasks(Closure update, Closure pageProcessed = AbstractMigrationHelper.DEFAULT_PROCESS_OPERATIONS, long sleepFor = 0, Predicate filter) {
        taskMigrationHelper.iterateTasks(update, pageProcessed, sleepFor, filter)
    }

    /**
     * Updates all tasks of a given process.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param processIdentifier identifier of PetriNet, to filter which tasks should be updated
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateTasksCursor(Closure update, String processIdentifier, double pageSize = 100.0) {
        taskMigrationHelper.updateTasksCursor(update, processIdentifier, pageSize)
    }

    /**
     * Updates specific tasks of a given process.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param processIdentifier identifier of PetriNet, to filter which tasks should be updated
     * @param transitionIds List of transition IDs to limit filter to specific transitions of given processIdentifier
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateSpecificTasksCursor(Closure update, String processIdentifier, List<String> transitionIds, double pageSize = 100.0) {
        taskMigrationHelper.updateSpecificTasksCursor(update, processIdentifier, transitionIds, pageSize)
    }

    /**
     * Update all tasks.
     * @param update Instance of Closure, which should contain code that will be executed for every Task
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateAllTasksCursor(Closure update, double pageSize = 100.0) {
        taskMigrationHelper.updateAllTasksCursor(update, pageSize)
    }

    /**
     * Updates existing Petri Net model with new values. New process roles are ignored! New roles in existing user type fields will be ignored!
     * @param identifier Identifier of Petri Net model that is being updated
     * @param resource Resource object with new version of Petri Net model
     */
    void updateNetIgnoreRoles(String identifier, Resource resource, List<Closure<PetriNet>> customUpdates = null) {
        petriNetMigrationHelper.updateNetIgnoreRoles(identifier, resource, customUpdates)
    }

    /**
     * Updates existing Petri Net model with new values. New process roles are ignored! New roles in existing user type fields will be ignored!
     * @param identifier Identifier of Petri Net model that is being updated
     * @param fileName File name of new version of Petri Net model
     */
    void updateNetIgnoreRoles(String identifier, String fileName, List<Closure<PetriNet>> customUpdates = null) {
        petriNetMigrationHelper.updateNetIgnoreRoles(identifier, fileName, customUpdates)
    }

    /**
     * Updates existing Petri Net model with new values. New process roles are ignored! New roles in existing user type fields will be ignored!
     * @param currentNet Current Petri Net object that will be updated
     * @param reimported New version of Petri Net object, its values will be applied to currentNet
     */
    void updateNetIgnoreRoles(PetriNet currentNet, PetriNet reimported, List<Closure<PetriNet>> customUpdates) {
        petriNetMigrationHelper.updateNetIgnoreRoles(currentNet, reimported, customUpdates)
    }

    /**
     * Replaces role permissions on transition with provided map e.g. ["roleId": ["perform": true]]
     * @param net Instance of Petri Net in which role on transition will be updated
     * @param transitionId Transition ID of updated transition
     * @param role ProcessRole that will be updated on transition
     * @param permissions New role permissions on transition
     */
    void updateTransitionRoles(PetriNet net, String transitionId, ProcessRole role, Map<String, Boolean> permissions) {
        petriNetMigrationHelper.updateTransitionRoles(net, transitionId, role, permissions)
    }

    /**
     * Replaces role permissions on transition with provided map e.g. ["roleId": ["perform": true]]
     * @param net Instance of Petri Net in which role on transition will be updated
     * @param transitionId Transition ID of updated transition
     * @param roleImportId ID of a role that will be updated on transition
     * @param permissions New role permissions on transition
     */
    void updateTransitionRoles(PetriNet net, String transitionId, String roleImportId, Map<String, Boolean> permissions) {
        petriNetMigrationHelper.updateTransitionRoles(net, transitionId, roleImportId, permissions)
    }

    /**
     * Replaces role permissions on transition with provided map e.g. ["roleId": ["perform": true]]
     * @param transitionId Transition ID of updated transition
     * @param roleImportId ID of a role that will be updated on transition
     * @param permissions New role permissions on transition
     */
    Closure<PetriNet> updateTransitionRolesClosure(String transitionId, String roleImportId, Map<String, Boolean> permissions) {
        petriNetMigrationHelper.updateTransitionRolesClosure(transitionId, roleImportId, permissions)
    }

    /**
     * Updates data set of existing Petri Net model with new values.
     * @param identifier Identifier of Petri Net model that is being updated
     * @param fileName File name of new version of Petri Net model
     */
    void updateDataSet(String identifier, String fileName, Closure<PetriNet> customUpdate = null) {
        petriNetMigrationHelper.updateDataSet(identifier, fileName, customUpdate)
    }

    /**
     * Create new role in existing Petri Net model.
     * @param identifier Identifier of Petri Net model in which the Process Role will be created
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    def createRoleInNet(String identifier, String id, String title, Map<EventType, Event> events = [:]) {
        return petriNetMigrationHelper.createRoleInNet(identifier, id, title, events)
    }

    /**
     * Create new role in existing Petri Net model.
     * @param identifier Identifier of Petri Net model in which the Process Role will be created
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    def createRoleInNet(String identifier, String id, I18nString title, Map<EventType, Event> events = [:]) {
        return petriNetMigrationHelper.createRoleInNet(identifier, id, title, events)
    }

    /**
     * Creates new global role
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    def createGlobalRole(String id, String title, Map<EventType, Event> events = [:]) {
        return petriNetMigrationHelper.createGlobalRole(id, title, event)
    }

    /**
     * Creates new global role
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    def createGlobalRole(String id, I18nString title, Map<EventType, Event> events = [:]) {
        return petriNetMigrationHelper.createGlobalRole(id, title, events)
    }

    /**
     * Reloads tasks of provided case via TaskService,
     * handles useCase.petriNet internally
     * @param useCase Instance of Case for which tasks will be reloaded
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void reloadTasks(Case useCase, PetriNet net) {
        taskMigrationHelper.reloadTasks(useCase, net)
    }

    /**
     * Indexes provided case in elasticsearch
     * handles useCase.petriNet internally
     * @param useCase Instance of Case that will be indexed into elasticsearch index
     */
    void elasticIndex(Case useCase) {
        caseMigrationHelper.elasticIndex(useCase)
    }

    /**
     * Indexes provided task in elasticsearch
     * @param task Instance of Task that will be indexed into elasticsearch index
     */
    void elasticTaskIndex(Task task) {
        taskMigrationHelper.elasticTaskIndex(task)
    }

    /**
     * Adds role with permissions to existing tasks of net
     * @param role ProcessRole that will be added to transitions
     * @param net Instance of Petri Net of updated transitions
     * @param transitionIds List of transition IDs the role will be added to
     * @param permissions Map of permissions for the role
     */
    void addRoleToExistingTasks(ProcessRole role, PetriNet net, List<String> transitionIds, Map<String, Boolean> permissions) {
        taskMigrationHelper.addRoleToExistingTasks(role, net, transitionIds, permissions)
    }

    /**
     * Sets petriNet object in case instance
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void setPetriNet(Case useCase, PetriNet net) {
        PetriNetMigrationHelper.setPetriNet(useCase, net)
    }

    /**
     * Delete given data fields from useCase
     * @param useCase Instance of Case
     * @param toDelete List of field IDs that will be deleted from useCase
     */
    void deleteDataFields(Case useCase, List<String> toDelete) {
        caseMigrationHelper.deleteDataFields(useCase, toDelete)
    }

    /**
     * Changes value of given data fields from number to text
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    void changeDataFieldsValueFromNumberToText(Case useCase, List<String> toChange) {
        caseMigrationHelper.changeDataFieldsValueFromNumberToText(useCase, toChange)
    }

    /**
     * Changes value of given data fields from text to number
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    void changeDataFieldsValueFromTextToNumber(Case useCase, List<String> toChange) {
        caseMigrationHelper.changeDataFieldsValueFromTextToNumber(useCase, toChange)
    }

    /**
     * Adds new data fields with their init value into useCase
     * @param useCase Instance of Case
     * @param toAdd Map<field id, init value of field>
     */
    void addTextDataFields(Case useCase, Map<String, String> toAdd) {
        caseMigrationHelper.addTextDataFields(useCase, toAdd)
    }

    /**
     * Changes value of given data fields from enumeration to multichoice
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    void changeDataFieldsValueFromEnumerationToMultichoice(Case useCase, List<String> toChange) {
        caseMigrationHelper.changeDataFieldsValueFromEnumerationToMultichoice(useCase, toChange)
    }

    /**
     * Adds new choices into enumeration or multichoice field
     * @param useCase Instance of Case
     * @param toAdd Map<field id, list of choices to add into data data field>
     */
    void addChoices(Case useCase, Map<String, List<String>> toAdd) {
        caseMigrationHelper.addChoices(useCase, toAdd)
    }

    /**
     * Removes choices from enumeration or multichoice field
     * @param useCase Instance of Case
     * @param toAdd Map<field id, list of choices to add into data field>
     */
    void removeChoices(Case useCase, Map<String, List<String>> toRemove) {
        caseMigrationHelper.removeChoices(useCase, toRemove)
    }

    /**
     * Changes value from FileFieldValue to FileListFieldValue
     * @param useCase Instance of Case
     * @param fieldId Field ID for value change
     */
    private void changeFileFieldToFileList(Case useCase, String fieldId) {
        FileListFieldValue fileListFieldValue = new FileListFieldValue()
        fileListFieldValue.namesPaths.add(useCase.dataSet[fieldId].value as FileFieldValue)
        useCase.dataSet[fieldId].value = fileListFieldValue
    }

    /**
     * Helper method used in updateNetIgnoreRoles method, it sorts PetriNet dataSet alphabetically
     * @param petriNet Instance of Petri Net
     */
    void resolveDataOrder(PetriNet petriNet) {
        Collator skCollator = Collator.getInstance(new Locale("sk", "SK"))
        List<Field> fields = new LinkedList<>(petriNet.getDataSet().values())
        fields = fields.stream().sorted({ f1, f2 ->
            int comparedTypes = f2.type.name <=> f1.type.name
            if (comparedTypes != 0) return comparedTypes
            return skCollator.compare((f1.name?.defaultValue ?: f1.stringId), (f2.name?.defaultValue ?: f2.stringId))
        }).collect(Collectors.toList())
        petriNet.dataSet = fields.collectEntries { [(it.getStringId()): (it)] } as Map<String, Field>
    }

    /**
     * Update dataField and dataRef components of given case
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void updateCaseComponents(Case useCase, PetriNet net) {
        Map<String, com.netgrif.application.engine.petrinet.domain.Component> components = createComponentsMap(net)
        Map<String, Map<String, com.netgrif.application.engine.petrinet.domain.Component>> dataRefComponents = createDataRefComponentsMap(net)

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
     * Method that collects all dataRef components of given PetriNet. Should be used in updateCases method, when a new dataRef component is added into PetriNet.
     * @param net Instance of PetriNet
     */
    Map<String, Map<String, com.netgrif.application.engine.petrinet.domain.Component>> createDataRefComponentsMap(PetriNet net) {
        Map<String, Map<String, com.netgrif.application.engine.petrinet.domain.Component>> componentsMap = [:]
        net.transitions.each {transition ->
            String transId = transition.key
            transition.value.dataSet.each {dataField ->
                String fieldId = dataField.key
                if (dataField.value.component) {
                    if (!componentsMap[fieldId]) {
                        componentsMap.put(fieldId, [(transId) : dataField.value.component])
                    } else {
                        Map<String, com.netgrif.application.engine.petrinet.domain.Component> existingMap = componentsMap[fieldId]
                        existingMap.put(transId, dataField.value.component)
                        componentsMap.put(fieldId, existingMap)
                    }
                }
            }
        }
        return componentsMap
    }

    /**
     * Method that collects all dataField components of given PetriNet. Should be used in updateCases method, when a new dataField component is added into PetriNet.
     * @param net Instance of PetriNet
     */
    Map<String, com.netgrif.application.engine.petrinet.domain.Component> createComponentsMap(PetriNet net) {
        Map<String, com.netgrif.application.engine.petrinet.domain.Component> componentsMap = [:]
        net.dataSet.each {dataField ->
            if (dataField.value.component) {
                componentsMap.put(dataField.key, dataField.value.component)
            }
        }
        return componentsMap
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
                updateTaskPermissions(useCase, taskPair, net)
            }
        }
    }

    /**
     * Updates permissions on existing tasks filtered by relevantTransitionIds
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     * @param relevantTransitionIds List of transition IDs for permissions update
     */
    void updateTasksPermissions(Case useCase, PetriNet net, List<String> relevantTransitionIds) {
        useCase.tasks.findAll { it.transition in relevantTransitionIds }.each { taskPair ->
            updateTaskPermissions(useCase, taskPair, net)
        }
    }

    /**
     * Updates permissions on existing task
     * @param useCase Instance of Case
     * @param taskPair TaskPair object of updated Task
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void updateTaskPermissions(Case useCase, TaskPair taskPair, PetriNet net) {
        try {
            Transition newTransition = net.getTransition(taskPair.transition)
            Task oldTask = taskService.findOne(taskPair.task)
            oldTask.setProcessId(net.stringId)
            oldTask.getRoles().clear()
            oldTask.setRoles(newTransition.roles)
            oldTask.setNegativeViewRoles(newTransition.negativeViewRoles)
            oldTask.resolveViewRoles()
            taskService.save(oldTask)
        } catch (Exception e) {
            log.error("Failed to update task permissions $useCase.stringId $taskPair.transition", e)
        }
    }

    /**
     * Changes PetriNet reference in useCase
     * @param useCase Instance of Case
     * @param newNet Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void migratePetriNet(Case useCase, PetriNet newNet) {
        useCase.setPetriNetObjectId(newNet.objectId)
    }
}