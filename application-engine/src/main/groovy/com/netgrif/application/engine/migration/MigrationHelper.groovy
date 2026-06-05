package com.netgrif.application.engine.migration

import com.netgrif.application.engine.configuration.properties.MigrationProperties
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.migration.helpers.CaseMigrationHelper
import com.netgrif.application.engine.migration.helpers.PetriNetMigrationHelper
import com.netgrif.application.engine.migration.helpers.TaskMigrationHelper
import com.netgrif.application.engine.migration.model.MigrationError
import com.netgrif.application.engine.migration.model.MigrationErrorPolicy
import com.netgrif.application.engine.objects.petrinet.domain.I18nString
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.events.Event
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.objects.workflow.domain.Task
import com.netgrif.application.engine.objects.workflow.domain.TaskPair
import com.querydsl.core.types.Predicate
import groovy.util.logging.Slf4j
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
/**
 * Helper class for migrating cases, tasks and Petri net models.
 * Provides convenience methods for updating existing data and models during system migrations.
 * This class delegates migration operations to specialized helper classes for cases, tasks, and Petri nets.
 */
@Slf4j
class MigrationHelper {

    /**
     * Helper for case-related migration operations including updating, iterating, and indexing cases.
     */
    @Autowired
    private CaseMigrationHelper caseMigrationHelper

    /**
     * Helper for task-related migration operations including updating, iterating, and managing task permissions.
     */
    @Autowired
    private TaskMigrationHelper taskMigrationHelper

    /**
     * Helper for Petri net model migration operations including updating models, roles, and data sets.
     */
    @Autowired
    private PetriNetMigrationHelper petriNetMigrationHelper

    /**
     * Configuration properties for migration operations.
     * Contains settings such as default error policy and other migration-related configuration.
     */
    @Autowired
    private MigrationProperties migrationProperties

    /**
     * Thread-local storage for the current migration error policy.
     * Allows different threads to maintain their own error handling policies during migration operations.
     * If not set, defaults to the policy specified in migrationProperties.
     */
    private final ThreadLocal<MigrationErrorPolicy> currentErrorPolicy = new ThreadLocal<>()

    /**
     * Closure for updating role events between existing and reimported Petri net models.
     * This closure synchronizes role-related events from the reimported model to the existing model,
     * ensuring that role event configurations are properly migrated during process updates.
     * @param existing The current Petri Net model that will be updated with new role events
     * @param reimported The newly imported Petri Net model containing updated role event definitions
     * @return Updated Petri Net model with synchronized role events
     */
    Closure<PetriNet> updateRoleEvents = { PetriNet existing, PetriNet reimported ->
        petriNetMigrationHelper.updateRoleEvents(existing, reimported)
    }

    /**
     * Returns the Importer service instance used for importing and processing Petri net models.
     * This method delegates to the PetriNetMigrationHelper to retrieve the importer.
     * @return Importer service instance
     */
    private Importer getImporter() {
        return petriNetMigrationHelper.getImporter()
    }

    /**
     * Retrieves the current error policy for migration operations.
     * If no policy is set in the thread-local storage, returns the default policy from migration properties.
     *
     * @return the current {@link MigrationErrorPolicy} or the default policy if none is set
     */
    MigrationErrorPolicy getCurrentErrorPolicy() {
        return currentErrorPolicy.get() ?: MigrationErrorPolicy.defaultErrorPolicy(migrationProperties.errorPolicy)
    }

    /**
     * Executes the provided closure with a specific error policy, then restores the previous policy.
     * This method allows temporary override of the error handling policy for a specific migration operation.
     * The previous policy is automatically restored after the closure execution, even if an exception occurs.
     *
     * @param policy the {@link MigrationErrorPolicy} to use during the closure execution
     * @param code the closure containing migration code to execute with the specified error policy
     */
    void withErrorPolicy(MigrationErrorPolicy policy, Closure code) {
        MigrationErrorPolicy previous = currentErrorPolicy.get()
        currentErrorPolicy.set(policy)
        try {
            code.call()
        } finally {
            if (previous) {
                currentErrorPolicy.set(previous)
            } else {
                currentErrorPolicy.remove()
            }
        }
    }

    /**
     * Updates all cases filtered by filter Predicate. Update closure is called on each filtered case.
     * @param update Instance of Closure, which should contain code that will be executed for every Case matched by filter
     * @param filter Instance of Predicate, to filter which cases should be updated
     */
    void updateCases(Closure update, Predicate filter) {
        log.debug("updateCases called with filter: {}", filter)
        caseMigrationHelper.updateCases(update, filter, getCurrentErrorPolicy())
    }

    /**
     * Iterates all cases filtered by filter Predicate. Update closure is called on each filtered case. PageProcessed closure is called after each page iteration.
     * @param update Instance of Closure, which should contain code that will be executed for every Case matched by filter (changes made to Case will not be saved automatically, for that use updateCases method)
     * @param sleepFor Optional attribute to set sleep time (in milliseconds) to sleep for after each iterated page. Default 0ms
     * @param filter Instance of Predicate, to filter which cases should be iterated
     */
    void iterateCases(Closure update, Closure pageProcessed = null, 
                      long sleepFor = 0, Predicate filter) {
        log.debug("iterateCases called with filter: {}, sleepFor: {}", filter, sleepFor)
        caseMigrationHelper.iterateCases(update, pageProcessed, sleepFor, filter, getCurrentErrorPolicy())
    }

    /**
     * Updates all cases of a given process.
     * @param update Instance of Closure, which should contain code that will be executed for every Case matched by filter
     * @param processIdentifier identifier of PetriNet, to filter which cases should be updated
     * @param pageSize Optional attribute to set page size. Default page size 100
     */
    void updateCasesCursor(Closure update, String processIdentifier, int pageSize = 100) {
        log.debug("updateCasesCursor called with processIdentifier: {}, pageSize: {}", processIdentifier, pageSize)
        caseMigrationHelper.updateCasesCursor(update, processIdentifier, pageSize, getCurrentErrorPolicy())
    }


    /**
     * Updates all cases of a given process identified by ObjectId.
     * @param update Instance of Closure, which should contain code that will be executed for every Case matched by filter
     * @param petriNetObjectId ObjectId of PetriNet, to filter which cases should be updated
     * @param pageSize Optional attribute to set page size. Default page size 100
     */
    void updateCasesCursor(Closure update, ObjectId petriNetObjectId, int pageSize = 100) {
        log.debug("updateCasesCursor called with petriNetObjectId: {}, pageSize: {}", petriNetObjectId, pageSize)
        caseMigrationHelper.updateCasesCursor(update, petriNetObjectId, pageSize, getCurrentErrorPolicy())
    }

    /**
     * Update all cases.
     * @param update Instance of Closure, which should contain code that will be executed for every Case
     * @param pageSize Optional attribute to set page size. Default page size 100
     */
    void updateAllCasesCursor(Closure update, int pageSize = 100) {
        log.debug("updateAllCasesCursor called with pageSize: {}", pageSize)
        caseMigrationHelper.updateAllCasesCursor(update, pageSize, getCurrentErrorPolicy())
    }

    /**
     * Updates all tasks filtered by filter Predicate. Update closure is called on each filtered task.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param filter Instance of Predicate, to filter which tasks should be updated
     */
    void updateTasks(Closure update, Predicate filter) {
        log.debug("updateTasks called with filter: {}", filter)
        taskMigrationHelper.updateTasks(update, filter, getCurrentErrorPolicy())
    }

    /**
     * Iterates all tasks filtered by filter Predicate. Update closure is called on each filtered task. PageProcessed closure is called after each page iteration.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter (changes made to Task will not be saved automatically, for that use updateCases method)
     * @param sleepFor Optional attribute to set sleep time (in milliseconds) to sleep for after each iterated page. Default 0ms
     * @param filter Instance of Predicate, to filter which tasks should be iterated
     */
    void iterateTasks(Closure update, Closure pageProcessed = null, long sleepFor = 0, Predicate filter) {
        log.debug("iterateTasks called with filter: {}, sleepFor: {}", filter, sleepFor)
        taskMigrationHelper.iterateTasks(update, pageProcessed, sleepFor, filter, getCurrentErrorPolicy())
    }

    /**
     * Updates all tasks of a given process.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param processIdentifier identifier of PetriNet, to filter which tasks should be updated
     * @param pageSize Optional attribute to set page size. Default page size 100
     */
    void updateTasksCursor(Closure update, String processIdentifier, int pageSize = 100) {
        log.debug("updateTasksCursor called with processIdentifier: {}, pageSize: {}", processIdentifier, pageSize)
        taskMigrationHelper.updateTasksCursor(update, processIdentifier, pageSize, getCurrentErrorPolicy())
    }

    /**
     * Updates specific tasks of a given process.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param processIdentifier identifier of PetriNet, to filter which tasks should be updated
     * @param transitionIds List of transition IDs to limit filter to specific transitions of given processIdentifier
     * @param pageSize Optional attribute to set page size. Default page size 100
     */
    void updateSpecificTasksCursor(Closure update, String processIdentifier, List<String> transitionIds, int pageSize = 100) {
        log.debug("updateSpecificTasksCursor called with processIdentifier: {}, transitionIds: {}, pageSize: {}", processIdentifier, transitionIds, pageSize)
        taskMigrationHelper.updateSpecificTasksCursor(update, processIdentifier, transitionIds, pageSize, getCurrentErrorPolicy())
    }

    /**
     * Update all tasks.
     * @param update Instance of Closure, which should contain code that will be executed for every Task
     * @param pageSize Optional attribute to set page size. Default page size 100
     */
    void updateAllTasksCursor(Closure update, int pageSize = 100) {
        log.debug("updateAllTasksCursor called with pageSize: {}", pageSize)
        taskMigrationHelper.updateAllTasksCursor(update, pageSize, getCurrentErrorPolicy())
    }

    /**
     * Updates existing Petri Net model with new values. New process roles are ignored! New roles in existing user type fields will be ignored!
     * @param identifier Identifier of Petri Net model that is being updated
     * @param resource Resource object with new version of Petri Net model
     */
    void updateNetIgnoreRoles(String identifier, Resource resource, List<Closure<PetriNet>> customUpdates = null) {
        log.debug("updateNetIgnoreRoles called with identifier: {}, resource: {}", identifier, resource)
        petriNetMigrationHelper.updateNetIgnoreRoles(identifier, resource, customUpdates)
    }

    /**
     * Updates existing Petri Net model with new values. New process roles are ignored! New roles in existing user type fields will be ignored!
     * @param identifier Identifier of Petri Net model that is being updated
     * @param fileName File name of new version of Petri Net model
     */
    void updateNetIgnoreRoles(String identifier, String fileName, List<Closure<PetriNet>> customUpdates = null) {
        log.debug("updateNetIgnoreRoles called with identifier: {}, fileName: {}", identifier, fileName)
        petriNetMigrationHelper.updateNetIgnoreRoles(identifier, fileName, customUpdates)
    }

    /**
     * Updates existing Petri Net model with new values. New process roles are ignored! New roles in existing user type fields will be ignored!
     * @param currentNet Current Petri Net object that will be updated
     * @param reimported New version of Petri Net object, its values will be applied to currentNet
     */
    void updateNetIgnoreRoles(PetriNet currentNet, PetriNet reimported, List<Closure<PetriNet>> customUpdates) {
        log.debug("updateNetIgnoreRoles called with currentNet: {}, reimported: {}", currentNet?.identifier, reimported?.identifier)
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
        log.debug("updateTransitionRoles called with net: {}, transitionId: {}, role: {}, permissions: {}", net?.identifier, transitionId, role?.stringId, permissions)
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
        log.debug("updateTransitionRoles called with net: {}, transitionId: {}, roleImportId: {}, permissions: {}", net?.identifier, transitionId, roleImportId, permissions)
        petriNetMigrationHelper.updateTransitionRoles(net, transitionId, roleImportId, permissions)
    }

    /**
     * Replaces role permissions on transition with provided map e.g. ["roleId": ["perform": true]]
     * @param transitionId Transition ID of updated transition
     * @param roleImportId ID of a role that will be updated on transition
     * @param permissions New role permissions on transition
     */
    Closure<PetriNet> updateTransitionRolesClosure(String transitionId, String roleImportId, Map<String, Boolean> permissions) {
        log.debug("updateTransitionRolesClosure called with transitionId: {}, roleImportId: {}, permissions: {}", transitionId, roleImportId, permissions)
        petriNetMigrationHelper.updateTransitionRolesClosure(transitionId, roleImportId, permissions)
    }

    /**
     * Updates data set of existing Petri Net model with new values.
     * @param identifier Identifier of Petri Net model that is being updated
     * @param fileName File name of new version of Petri Net model
     */
    void updateDataSet(String identifier, String fileName, Closure<PetriNet> customUpdate = null) {
        log.debug("updateDataSet called with identifier: {}, fileName: {}", identifier, fileName)
        petriNetMigrationHelper.updateDataSet(identifier, fileName, customUpdate)
    }

    /**
     * Create new role in existing Petri Net model.
     * @param identifier Identifier of Petri Net model in which the Process Role will be created
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    def createRoleInNet(String identifier, String id, String title, Map<EventType, Event> events = [:]) {
        log.debug("createRoleInNet called with identifier: {}, id: {}, title: {}", identifier, id, title)
        return petriNetMigrationHelper.createRoleInNet(identifier, id, title, events)
    }

    /**
     * Create new role in existing Petri Net model.
     * @param identifier Identifier of Petri Net model in which the Process Role will be created
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    def createRoleInNet(String identifier, String id, I18nString title, Map<EventType, Event> events = [:]) {
        log.debug("createRoleInNet called with identifier: {}, id: {}, title: {}", identifier, id, title)
        return petriNetMigrationHelper.createRoleInNet(identifier, id, title, events)
    }

    /**
     * Creates new global role
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    def createGlobalRole(String id, String title, Map<EventType, Event> events = [:]) {
        log.debug("createGlobalRole called with id: {}, title: {}", id, title)
        return petriNetMigrationHelper.createGlobalRole(id, title, events)
    }

    /**
     * Creates new global role
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    def createGlobalRole(String id, I18nString title, Map<EventType, Event> events = [:]) {
        log.debug("createGlobalRole called with id: {}, title: {}", id, title)
        return petriNetMigrationHelper.createGlobalRole(id, title, events)
    }

    /**
     * Reloads tasks of provided case via TaskService,
     * handles useCase.petriNet internally
     * @param useCase Instance of Case for which tasks will be reloaded
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void reloadTasks(Case useCase, PetriNet net) {
        log.debug("reloadTasks called with useCase: {}, net: {}", useCase?.stringId, net?.identifier)
        taskMigrationHelper.reloadTasks(useCase, net)
    }

    /**
     * Indexes provided case in elasticsearch
     * handles useCase.petriNet internally
     * @param useCase Instance of Case that will be indexed into elasticsearch index
     */
    void elasticIndex(Case useCase) {
        log.debug("elasticIndex called with useCase: {}", useCase?.stringId)
        caseMigrationHelper.elasticIndex(useCase, getCurrentErrorPolicy())
    }

    /**
     * Indexes provided task in elasticsearch
     * @param task Instance of Task that will be indexed into elasticsearch index
     */
    void elasticTaskIndex(Task task) {
        log.debug("elasticTaskIndex called with task: {}", task?.stringId)
        taskMigrationHelper.elasticTaskIndex(task, getCurrentErrorPolicy())
    }

    /**
     * Adds role with permissions to existing tasks of net
     * @param role ProcessRole that will be added to transitions
     * @param net Instance of Petri Net of updated transitions
     * @param transitionIds List of transition IDs the role will be added to
     * @param permissions Map of permissions for the role
     */
    void addRoleToExistingTasks(ProcessRole role, PetriNet net, List<String> transitionIds, Map<String, Boolean> permissions) {
        log.debug("addRoleToExistingTasks called with role: {}, net: {}, transitionIds: {}, permissions: {}", role?.stringId, net?.identifier, transitionIds, permissions)
        taskMigrationHelper.addRoleToExistingTasks(role, net, transitionIds, permissions)
    }

    /**
     * Sets petriNet object in case instance
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    static void setPetriNet(Case useCase, PetriNet net) {
        PetriNetMigrationHelper.setPetriNet(useCase, net)
    }

    /**
     * Delete given data fields from useCase
     * @param useCase Instance of Case
     * @param toDelete List of field IDs that will be deleted from useCase
     */
    static void deleteDataFields(Case useCase, Set<String> toDelete) {
        CaseMigrationHelper.deleteDataFields(useCase, toDelete)
    }

    /**
     * Changes value of given data fields from number to text
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    static void changeDataFieldsValueFromNumberToText(Case useCase, Set<String> toChange) {
        CaseMigrationHelper.changeDataFieldsValueFromNumberToText(useCase, toChange)
    }

    /**
     * Changes value of given data fields from text to number
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    void changeDataFieldsValueFromTextToNumber(Case useCase, Set<String> toChange) {
        caseMigrationHelper.changeDataFieldsValueFromTextToNumber(useCase, toChange, getCurrentErrorPolicy())
    }

    /**
     * Adds new data fields with their init value into useCase
     * @param useCase Instance of Case
     * @param toAdd Map<field id, init value of field>
     */
    static void addTextDataFields(Case useCase, Map<String, String> toAdd) {
        CaseMigrationHelper.addTextDataFields(useCase, toAdd)
    }

    /**
     * Changes value of given data fields from enumeration to multichoice
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    static void changeDataFieldsValueFromEnumerationToMultichoice(Case useCase, Set<String> toChange) {
        CaseMigrationHelper.changeDataFieldsValueFromEnumerationToMultichoice(useCase, toChange)
    }

    /**
     * Adds new choices into enumeration or multichoice field
     * @param useCase Instance of Case
     * @param toAdd Map<field id, list of choices to add into data data field>
     */
    static void addChoices(Case useCase, Map<String, List<String>> toAdd) {
        CaseMigrationHelper.addChoices(useCase, toAdd)
    }

    /**
     * Removes choices from enumeration or multichoice field
     * @param useCase Instance of Case
     * @param toAdd Map<field id, list of choices to add into data field>
     */
    static void removeChoices(Case useCase, Map<String, List<String>> toRemove) {
        CaseMigrationHelper.removeChoices(useCase, toRemove)
    }

    /**
     * Changes value from FileFieldValue to FileListFieldValue
     * @param useCase Instance of Case
     * @param fieldId Field ID for value change
     */
    static void changeFileFieldToFileList(Case useCase, String fieldId) {
        CaseMigrationHelper.changeFileFieldToFileList(useCase, fieldId)
    }

    /**
     * Helper method used in updateNetIgnoreRoles method, it sorts PetriNet dataSet alphabetically
     * @param petriNet Instance of Petri Net
     */
    static void resolveDataOrder(PetriNet petriNet) {
        PetriNetMigrationHelper.resolveDataOrder(petriNet)
    }

    /**
     * Update dataField and dataRef components of given case
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    static void updateCaseComponents(Case useCase, PetriNet net) {
        CaseMigrationHelper.updateCaseComponents(useCase, net)
    }

    /**
     * Method that collects all dataRef components of given PetriNet. Should be used in updateCases method, when a new dataRef component is added into PetriNet.
     * @param net Instance of PetriNet
     */
    static Map<String, Map<String, com.netgrif.application.engine.objects.petrinet.domain.Component>> createDataRefComponentsMap(PetriNet net) {
        PetriNetMigrationHelper.createDataRefComponentsMap(net)
    }

    /**
     * Method that collects all dataField components of given PetriNet. Should be used in updateCases method, when a new dataField component is added into PetriNet.
     * @param net Instance of PetriNet
     */
    static Map<String, com.netgrif.application.engine.objects.petrinet.domain.Component> createComponentsMap(PetriNet net) {
        PetriNetMigrationHelper.createComponentsMap(net)
    }

    /**
     * Updates case permissions from PetriNet
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void updateCasePermissionsFromNet(Case useCase, PetriNet net, boolean updateTasks = false) {
        log.debug("updateCasePermissionsFromNet called with useCase: {}, net: {}, updateTasks: {}", useCase?.stringId, net?.identifier, updateTasks)
        caseMigrationHelper.updateCasePermissionsFromNet(useCase, net, updateTasks, getCurrentErrorPolicy())
    }

    /**
     * Updates permissions on existing tasks filtered by relevantTransitionIds
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     * @param relevantTransitionIds List of transition IDs for permissions update
     */
    void updateTasksPermissions(Case useCase, PetriNet net, List<String> relevantTransitionIds) {
        log.debug("updateTasksPermissions called with useCase: {}, net: {}, relevantTransitionIds: {}", useCase?.stringId, net?.identifier, relevantTransitionIds)
        taskMigrationHelper.updateTasksPermissions(useCase, net, relevantTransitionIds, getCurrentErrorPolicy())
    }

    /**
     * Updates permissions on existing task
     * @param useCase Instance of Case
     * @param taskPair TaskPair object of updated Task
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void updateTaskPermissions(Case useCase, TaskPair taskPair, PetriNet net) {
        log.debug("updateTaskPermissions called with useCase: {}, taskPair: {}, net: {}", useCase?.stringId, taskPair?.task?.toString(), net?.identifier)
        taskMigrationHelper.updateTaskPermissions(useCase, taskPair, net, getCurrentErrorPolicy())
    }

    /**
     * Changes PetriNet reference in useCase
     * @param useCase Instance of Case
     * @param newNet Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void migratePetriNet(Case useCase, PetriNet newNet) {
        caseMigrationHelper.migratePetriNet(useCase, newNet)
    }

    /**
     * Removes a case from the system.
     * This operation delegates to the CaseMigrationHelper to perform the actual deletion,
     * respecting the current error policy for handling any issues that may occur during removal.
     *
     * @param useCase Instance of Case to be removed from the system
     */
    void removeCase(Case useCase) {
        caseMigrationHelper.removeCase(useCase, getCurrentErrorPolicy())
    }

    /**
     * Returns cached migration errors without clearing them.
     *
     * @return immutable snapshot of cached migration errors
     */
    List<MigrationError> getErrors() {
        List<MigrationError> errors = []
        errors.addAll(caseMigrationHelper.getErrors())
        errors.addAll(taskMigrationHelper.getErrors())
        errors.addAll(petriNetMigrationHelper.getErrors())
        return Collections.unmodifiableList(errors)
    }

    /**
     * Returns cached migration errors and clears the cache.
     *
     * @return cached migration errors collected since the last clear/pop
     */
    List<MigrationError> popErrors() {
        List<MigrationError> errors = []
        errors.addAll(caseMigrationHelper.popErrors())
        errors.addAll(taskMigrationHelper.popErrors())
        errors.addAll(petriNetMigrationHelper.popErrors())
        return errors
    }

    /**
     * Clears cached migration errors.
     */
    void clearErrors() {
        caseMigrationHelper.clearErrors()
        taskMigrationHelper.clearErrors()
        petriNetMigrationHelper.clearErrors()
    }

    /**
     * Indicates whether any migration errors were cached.
     *
     * @return true if at least one error is cached
     */
    boolean hasErrors() {
        return caseMigrationHelper.hasErrors() || taskMigrationHelper.hasErrors() || petriNetMigrationHelper.hasErrors()
    }

    /**
     * Runs migration code with a clean error cache and returns errors collected during execution.
     *
     * @param migrationCode migration logic to execute
     * @return errors collected during migrationCode execution
     */
    List<MigrationError> collectErrors(Closure migrationCode) {
        clearErrors()
        try {
            migrationCode.call()
        } finally {
            return popErrors()
        }
    }
}