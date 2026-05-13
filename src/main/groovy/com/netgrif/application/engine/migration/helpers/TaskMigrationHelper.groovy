package com.netgrif.application.engine.migration.helpers

import com.netgrif.application.engine.migration.config.properties.MigrationConfigurationProperties
import com.netgrif.application.engine.migration.config.properties.MigrationConfigurationProperties.TaskMigrationProperties
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.workflow.domain.Task
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
class TaskMigrationHelper extends AbstractMigrationHelper<Task>{

    /**
     * The task migration properties configuration.
     *
     * This property provides the configuration values for task migration,
     * such as the size of the page used to process tasks in the migration.
     * It is loaded from the {@link MigrationConfigurationProperties} during initialization.
     */
    private TaskMigrationProperties taskMigrationProperties

    /**
     * Service for handling Petri Net operations.
     *
     * This service is used to access and interact with Petri Net tasks,
     * such as retrieving the latest version of a Petri Net by its identifier
     * during task migrations.
     */
    private IPetriNetService petriNetService

    /**
     * Constructs a new TaskMigrationHelper with the specified MongoTemplate.
     *
     * @param mongoTemplate the {@link MongoTemplate} to use for interacting with MongoDB
     */
    TaskMigrationHelper(MongoTemplate mongoTemplate,
                        MigrationConfigurationProperties migrationConfigurationProperties,
                        IPetriNetService petriNetService) {
        super(Task.class, mongoTemplate)
        this.taskMigrationProperties = migrationConfigurationProperties.tasks
        this.petriNetService = petriNetService
    }

    /**
     * Returns the page size for the task migration process.
     *
     * The page size is configured in the {@link TaskMigrationProperties} and determines
     * the number of tasks processed in a single batch during migration operations.
     *
     * @return an integer indicating the configured page size
     */
    @Override
    int getPageSize() {
        return taskMigrationProperties.pageSize
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
        log.debug("Updating case with ID ${document.stringId}")
        log.trace("Updating case ${document.toString()}")
        update(document)
        bulkOperations.replaceOne(Query.query(Criteria.where("_id").is(document.getObjectId())), document)
    }

    /**
     * Updates all tasks filtered by filter Predicate. Update closure is called on each filtered task.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param filter Instance of Predicate, to filter which tasks should be updated
     */
    void updateTasks(Closure update, Predicate filter) {
        log.info("Updating tasks with filter ${filter.toString()} and update ${update.toString()}")
        iterate(update, DEFAULT_PROCESS_OPERATIONS, toQuery(filter))
    }

    /**
     * Iterates all tasks filtered by filter Predicate. Update closure is called on each filtered task. PageProcessed closure is called after each page iteration.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter (changes made to Task will not be saved automatically, for that use updateCases method)
     * @param sleepFor Optional attribute to set sleep time (in milliseconds) to sleep for after each iterated page. Default 0ms
     * @param filter Instance of Predicate, to filter which tasks should be iterated
     */
    void iterateTasks(Closure update, Closure pageProcessed = DEFAULT_PROCESS_OPERATIONS, long sleepFor = 0, Predicate filter) {
        iterate(update, pageProcessed, toQuery(filter), sleepFor)
    }

    /**
     * Updates all tasks of a given process.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param processIdentifier identifier of PetriNet, to filter which tasks should be updated
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateTasksCursor(Closure update, String processIdentifier, double pageSize = 100.0) {
        String processId = petriNetService.getNewestVersionByIdentifier(processIdentifier).stringId
        Query query = new Query(Criteria.where("processId").is(processId))
        iterate(update, DEFAULT_PROCESS_OPERATIONS, query, 0, pageSize as int)
    }

    /**
     * Updates specific tasks of a given process.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param processIdentifier identifier of PetriNet, to filter which tasks should be updated
     * @param transitionIds List of transition IDs to limit filter to specific transitions of given processIdentifier
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateSpecificTasksCursor(Closure update, String processIdentifier, List<String> transitionIds, double pageSize = 100.0) {
        String processId = petriNetService.getNewestVersionByIdentifier(processIdentifier).stringId
        Query query = new Query(Criteria.where("processId").is(processId))
        query.addCriteria(Criteria.where("transitionId").in(transitionIds))
        iterate(update, DEFAULT_PROCESS_OPERATIONS, query, 0, pageSize as int)
    }

    /**
     * Update all tasks.
     * @param update Instance of Closure, which should contain code that will be executed for every Task
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateAllTasksCursor(Closure update, double pageSize = 100.0) {
        iterate(update, DEFAULT_PROCESS_OPERATIONS, new Query(), 0, pageSize as int)
    }
}
