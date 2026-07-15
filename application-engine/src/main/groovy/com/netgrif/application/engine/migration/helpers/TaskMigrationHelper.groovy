package com.netgrif.application.engine.migration.helpers

import com.netgrif.application.engine.adapter.spring.workflow.domain.QTask
import com.netgrif.application.engine.configuration.properties.MigrationProperties
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService
import com.netgrif.application.engine.migration.model.MigrationErrorPolicy
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.Transition
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.objects.workflow.domain.Task
import com.netgrif.application.engine.objects.workflow.domain.TaskPair
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.querydsl.core.types.Predicate
import groovy.util.logging.Slf4j
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

/**
 * A helper class for managing task migrations. 
 * This class extends {@link AbstractMigrationHelper} and provides methods for updating, iterating, 
 * and manipulating {@link Task} entities in bulk during migration processes. 
 * It integrates with MongoDB and uses the {@link MongoTemplate} for data operations and 
 * {@link IPetriNetService} for interacting with PetriNet services.
 */
@Slf4j
@Component
class TaskMigrationHelper extends AbstractMigrationHelper<Task> {

    /**
     * Service for handling Petri Net operations.
     *
     * This service is used to access and interact with Petri Net tasks,
     * such as retrieving the latest version of a Petri Net by its identifier
     * during task migrations.
     */
    protected final IPetriNetService petriNetService

    /**
     * Service for handling task operations.
     *
     * This service provides methods for managing task entities,
     * including finding, saving, and reloading tasks during migration processes.
     */
    protected final ITaskService taskService

    /**
     * Service for handling Elasticsearch task indexing operations.
     *
     * This service is used to index task documents into Elasticsearch,
     * enabling full-text search and analytics capabilities for tasks.
     */
    protected final IElasticTaskService elasticTaskService

    /**
     * Service for mapping task entities to Elasticsearch documents.
     *
     * This service transforms task domain objects into their Elasticsearch
     * representation before indexing, ensuring proper field mapping and data structure.
     */
    protected final IElasticTaskMappingService elasticTaskMappingService

    /**
     * Constructs a new TaskMigrationHelper with the specified MongoTemplate.
     *
     * @param mongoTemplate the {@link MongoTemplate} to use for interacting with MongoDB
     */
    TaskMigrationHelper(MongoTemplate mongoTemplate,
                        MigrationProperties migrationProperties,
                        IPetriNetService petriNetService,
                        ITaskService taskService,
                        IElasticTaskService elasticTaskService,
                        IElasticTaskMappingService elasticTaskMappingService) {
        super(Task.class, mongoTemplate, migrationProperties)
        this.petriNetService = petriNetService
        this.taskService = taskService
        this.elasticTaskService = elasticTaskService
        this.elasticTaskMappingService = elasticTaskMappingService
    }

    /**
     * Returns the page size for the task migration process.
     *
     * The page size is configured in the {@link MigrationProperties.TaskMigrationProperties} and determines
     * the number of tasks processed in a single batch during migration operations.
     *
     * @return an integer indicating the configured page size
     */
    @Override
    int getPageSize() {
        return migrationProperties.tasks.pageSize
    }

    /**
     * Prepares a set of bulk operations for tasks during the migration process.
     *
     * This method is called for each individual {@link Task} document that needs to be updated. 
     * It executes the provided {@code update} closure to modify the task and 
     * prepares a bulk replacement operation to save the changes to the database.
     *
     * @param document the {@link Task} document to be updated
     * @param update a {@link Closure} that defines the update logic to be applied to the {@link Task}
     * @param bulkOperations the {@link BulkOperations} object used to queue the MongoDB operations for batch execution
     */
    @Override
    void prepareOperations(Task document, Closure update, BulkOperations bulkOperations) {
        log.debug("Updating task with ID ${document.stringId}")
        update(document)
        bulkOperations.replaceOne(Query.query(Criteria.where("_id").is(document.getObjectId())), document)
    }

    /**
     * Resolves and returns the unique identifier of the given task document.
     *
     * This method extracts the string representation of the task's identifier,
     * which is used for logging and tracking purposes during migration operations.
     *
     * @param document the {@link Task} document whose identifier should be resolved
     * @return a {@link String} representing the unique identifier of the task
     */
    @Override
    String resolveId(Task document) {
        return document.getStringId()
    }

    /**
     * Updates all tasks filtered by filter Predicate. Update closure is called on each filtered task.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param filter Instance of Predicate, to filter which tasks should be updated
     */
    void updateTasks(Closure update, Predicate filter, MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting updateTasks with filter: ${filter.toString()}")
        log.info("Updating tasks with filter ${filter.toString()} and update ${update.toString()}")
        log.trace("Converting filter to query and calling iterate")
        iterate(update, null, toQuery(filter), 0, getPageSize(), errorPolicy)
    }

    /**
     * Iterates all tasks filtered by filter Predicate. Update closure is called on each filtered task. PageProcessed closure is called after each page iteration.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter (changes made to Task will not be saved automatically, for that use updateCases method)
     * @param sleepFor Optional attribute to set sleep time (in milliseconds) to sleep for after each iterated page. Default 0ms
     * @param filter Instance of Predicate, to filter which tasks should be iterated
     */
    void iterateTasks(Closure update, Closure pageProcessed = null, long sleepFor = 0, Predicate filter,
                      MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting iterateTasks with filter: ${filter.toString()}, sleepFor: ${sleepFor}ms")
        log.trace("Converting filter to query and calling iterate with pageProcessed closure")
        iterate(update, pageProcessed, toQuery(filter), sleepFor, getPageSize(), errorPolicy)
    }

    /**
     * Updates all tasks of a given process.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param processIdentifier identifier of PetriNet, to filter which tasks should be updated
     * @param pageSize Optional attribute to set page size. Default page size 100
     */
    void updateTasksCursor(Closure update, String processIdentifier, int pageSize = 100,
                           MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting updateTasksCursor for processIdentifier: ${processIdentifier}, pageSize: ${pageSize}")
        String processId = petriNetService.getDefaultVersionByIdentifier(processIdentifier).stringId
        Query query = new Query(Criteria.where("processId").is(processId))
        log.trace("Created query for processId: ${processId}, calling iterate")
        iterate(update, null, query, 0, pageSize as int, errorPolicy)
    }

    /**
     * Updates specific tasks of a given process.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param processIdentifier identifier of PetriNet, to filter which tasks should be updated
     * @param transitionIds List of transition IDs to limit filter to specific transitions of given processIdentifier
     * @param pageSize Optional attribute to set page size. Default page size 100
     */
    void updateSpecificTasksCursor(Closure update, String processIdentifier, List<String> transitionIds, int pageSize = 100,
                                   MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting updateSpecificTasksCursor for processIdentifier: ${processIdentifier}, transitionIds: ${transitionIds}, pageSize: ${pageSize}")
        String processId = petriNetService.getDefaultVersionByIdentifier(processIdentifier).stringId
        Query query = new Query(Criteria.where("processId").is(processId))
        query.addCriteria(Criteria.where("transitionId").in(transitionIds))
        log.trace("Created query with criteria for processId: ${processId} and transitionIds: ${transitionIds}, calling iterate")
        iterate(update, null, query, 0, pageSize as int, errorPolicy)
    }

    /**
     * Update all tasks.
     * @param update Instance of Closure, which should contain code that will be executed for every Task
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateAllTasksCursor(Closure update, int pageSize = 100, MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting updateAllTasksCursor with pageSize: ${pageSize}")
        log.trace("Calling iterate with empty query to process all tasks")
        iterate(update, null, new Query(), 0, pageSize as int, errorPolicy)
    }

    /**
     * Reloads tasks of provided case via TaskService,
     * handles useCase.petriNet internally
     * @param useCase Instance of Case for which tasks will be reloaded
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void reloadTasks(Case useCase, PetriNet net) {
        log.debug("Starting reloadTasks for case: ${useCase.stringId}, net identifier: ${net.identifier}")
        PetriNetMigrationHelper.setPetriNet(useCase, net)
        log.trace("Set PetriNet for case, calling taskService.reloadTasks")
        taskService.reloadTasks(useCase, false)
    }

    /**
     * Indexes provided task in elasticsearch
     * @param task Instance of Task that will be indexed into elasticsearch index
     */
    void elasticTaskIndex(Task task, MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting elasticTaskIndex for task: ${task.stringId}")
        try {
            log.trace("Transforming and indexing task: ${task.stringId} into elasticsearch")
            elasticTaskService.indexNow(elasticTaskMappingService.transform(task))
        } catch (Exception e) {
            String message = "Failed to index $task.stringId"
            log.error(message, e)
            handleMigrationError(errorPolicy, "elasticTaskIndex", type, task.stringId, message, e)
        }
    }

    /**
     * Adds role with permissions to existing tasks of net
     * @param role ProcessRole that will be added to transitions
     * @param net Instance of Petri Net of updated transitions
     * @param transitionIds List of transition IDs the role will be added to
     * @param permissions Map of permissions for the role
     */
    void addRoleToExistingTasks(ProcessRole role, PetriNet net, List<String> transitionIds, Map<String, Boolean> permissions) {
        log.debug("Starting addRoleToExistingTasks for role: ${role.getName()}, net: ${net.identifier}, transitionIds: ${transitionIds}")
        log.trace("Calling updateTasks to add role with permissions: ${permissions}")
        updateTasks({ Task task ->
            log.trace("Add role '${role.getName()}' with roleId=${role.getImportId()} to transitionId=${task.getTransitionId()} in task ${task.stringId}")
            task.addRole(role.getStringId(), permissions)
        }, QTask.task.transitionId.in(transitionIds) & QTask.task.processId.eq(net.getStringId()))
    }

    /**
     * Updates permissions on existing tasks filtered by relevantTransitionIds
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     * @param relevantTransitionIds List of transition IDs for permissions update
     */
    void updateTasksPermissions(Case useCase, PetriNet net, List<String> relevantTransitionIds, MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting updateTasksPermissions for case: ${useCase.stringId}, net: ${net.identifier}, relevantTransitionIds: ${relevantTransitionIds}")
        useCase.tasks.findAll { it.transition in relevantTransitionIds }.each { taskPair ->
            log.trace("Processing task permissions for transition: ${taskPair.transition} in case: ${useCase.stringId}")
            updateTaskPermissions(useCase, taskPair, net, errorPolicy)
        }
    }

    /**
     * Updates permissions on existing task
     * @param useCase Instance of Case
     * @param taskPair TaskPair object of updated Task
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void updateTaskPermissions(Case useCase, TaskPair taskPair, PetriNet net, MigrationErrorPolicy errorPolicy = defaultErrorPolicy()) {
        log.debug("Starting updateTaskPermissions for case: ${useCase.stringId}, task transition: ${taskPair.transition}")
        try {
            Transition newTransition = net.getTransition(taskPair.transition)
            Task oldTask = taskService.findOne(taskPair.task)
            log.trace("Updating task roles and permissions for task: ${oldTask.stringId}")
            oldTask.setProcessId(net.stringId)
            oldTask.setProcessIdentifier(net.identifier)
            oldTask.setRoles(newTransition.roles)
            oldTask.setNegativeViewRoles(newTransition.negativeViewRoles)
            oldTask.resolveViewRoles()
            taskService.save(oldTask)
        } catch (Exception e) {
            String message = "Failed to update task permissions $useCase.stringId $taskPair.transition"
            log.error(message, e)
            handleMigrationError(errorPolicy, "updateTaskPermissions", type, taskPair?.task?.toString(), message, e)
        }
    }
}
