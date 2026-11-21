package com.netgrif.application.engine

import com.netgrif.application.engine.auth.domain.repositories.UserRepository
import com.netgrif.application.engine.elastic.domain.ElasticCase
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository
import com.netgrif.application.engine.elastic.domain.ElasticPetriNet
import com.netgrif.application.engine.elastic.domain.ElasticTask
import com.netgrif.application.engine.elastic.domain.ElasticTaskRepository
import com.netgrif.application.engine.elastic.service.ElasticTaskQueueManager
import com.netgrif.application.engine.elastic.service.executors.Executor
import com.netgrif.application.engine.menu.service.interfaces.IMenuItemService
import com.netgrif.application.engine.petrinet.domain.repository.UriNodeRepository
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.*
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService
import groovy.util.logging.Slf4j
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Slf4j
@Component
class TestHelper {

    @Autowired
    private SuperCreator superCreator
    @Autowired
    private MongoTemplate template
    @Autowired
    private UserRepository userRepository
    @Autowired
    private ProcessRoleRepository roleRepository
    @Autowired
    private ProcessRoleService roleService
    @Autowired
    private SystemUserRunner systemUserRunner
    @Autowired
    private DefaultRoleRunner defaultRoleRunner
    @Autowired
    private AnonymousRoleRunner anonymousRoleRunner
    @Autowired
    private ElasticTaskRepository elasticTaskRepository
    @Autowired
    private ElasticCaseRepository elasticCaseRepository
    @Autowired
    private UriNodeRepository uriNodeRepository
    @Autowired
    private GroupRunner groupRunner
    @Autowired
    private IFieldActionsCacheService actionsCacheService
    @Autowired
    private MenuRunner filterRunner
    @Autowired
    private FinisherRunner finisherRunner
    @Autowired
    private ImpersonationRunner impersonationRunner
    @Autowired
    private UriRunner uriRunner
    @Autowired
    private IPetriNetService petriNetService
    @Autowired
    private IMenuItemService menuItemService
    @Autowired
    private MongoDbRunner mongoDbRunner
    @Autowired
    private ProcessRunner processRunner

    @Autowired
    private FunctionsCacheRunner functionsCacheRunner
    @Autowired
    private ElasticsearchRestTemplate elasticTemplate
    @Autowired
    private Executor elasticCaseExecutor
    @Autowired
    private ElasticTaskQueueManager elasticTaskQueueManager
    @Autowired
    @Qualifier("taskExecutor")
    private TaskExecutor asyncTaskExecutor // declared as TaskExecutor in TaskExecutionConfiguration
    @Autowired
    @Qualifier("elasticTaskExecutor")
    private ThreadPoolTaskExecutor elasticTaskExecutor

    // Snapshot of the state after startup runners, per Spring context (this bean); restored instead of re-running runners.
    private Map<String, List<Document>> mongoSnapshot
    private List<ElasticCase> elasticCaseSnapshot
    private List<ElasticTask> elasticTaskSnapshot

    void truncateDbs() {
        if (mongoSnapshot == null) {
            fullReset()
            awaitElasticIndexing()
            takeSnapshot()
        } else {
            restoreSnapshot()
        }
    }

    private void takeSnapshot() {
        def db = template.db
        mongoSnapshot = db.listCollectionNames().into([]).collectEntries { String name ->
            [(name): db.getCollection(name).find().into([])]
        }
        // keep only documents of entities that exist in Mongo; late async indexing from previous tests can leave extra ones
        Set<String> caseIds = (mongoSnapshot[template.getCollectionName(Case)] ?: []).collect { it.getObjectId("_id").toString() } as Set
        Set<String> taskIds = (mongoSnapshot[template.getCollectionName(Task)] ?: []).collect { it.getObjectId("_id").toString() } as Set
        elasticCaseSnapshot = elasticCaseRepository.findAll().findAll { it.stringId in caseIds }
        elasticTaskSnapshot = elasticTaskRepository.findAll().findAll { it.stringId in taskIds }
        log.info("Test DB snapshot taken: ${mongoSnapshot.values().sum { it.size() } ?: 0} Mongo documents in ${mongoSnapshot.size()} collections, " +
                "${elasticCaseSnapshot.size()} Elastic cases, ${elasticTaskSnapshot.size()} Elastic tasks")
    }

    private void restoreSnapshot() {
        awaitElasticQueues()
        def db = template.db
        List<String> existing = db.listCollectionNames().into([])
        existing.findAll { !mongoSnapshot.containsKey(it) }.each { db.getCollection(it).deleteMany(new Document()) }
        mongoSnapshot.each { name, docs ->
            def collection = db.getCollection(name)
            collection.deleteMany(new Document()) // deleteMany instead of drop keeps indexes
            if (docs) collection.insertMany(docs)
        }
        // some tests drop the whole database, which removes indexes as well
        if (!existing.containsAll(mongoSnapshot.keySet())) {
            mongoDbRunner.resolveIndexes()
        }

        elasticTaskRepository.deleteAll()
        elasticCaseRepository.deleteAll()
        // ElasticCase uses external @Version; ES keeps versions of deleted ids, so re-saving under the old _id conflicts.
        // The app looks cases up by stringId, so a fresh _id is fine (ES requires an explicit id with external versioning).
        elasticCaseSnapshot.each { it.id = UUID.randomUUID().toString() }
        elasticCaseRepository.saveAll(elasticCaseSnapshot)
        elasticTaskRepository.saveAll(elasticTaskSnapshot)
        elasticTemplate.indexOps(ElasticCase).refresh()
        elasticTemplate.indexOps(ElasticTask).refresh()

        // in-memory state normally rebuilt by runners; action/function caches are keyed by ids, which the snapshot keeps
        roleService.clearCache()
        petriNetService.evictAllCaches()
        actionsCacheService.clearNamespaceFunctionCache()
        functionsCacheRunner.run()
        systemUserRunner.run()
        superCreator.run()

        verifySnapshotRestored()
    }

    // Fails the test if the restored state differs from the snapshot, e.g. when async work of a previous test
    // (Elastic indexing queues) writes after the restore.
    private void verifySnapshotRestored() {
        List<String> diffs = []
        def db = template.db
        // Quartz job store lives in the same DB and writes its scheduler state on its own
        db.listCollectionNames().into([]).findAll { !it.startsWith("quartz_") }.each { String name ->
            List<Document> expected = (mongoSnapshot[name] ?: []).sort(false) { it.get("_id").toString() }
            List<Document> actual = db.getCollection(name).find().into([]).sort { it.get("_id").toString() }
            if (actual != expected) {
                Set expectedIds = expected*.get("_id")*.toString() as Set
                Set actualIds = actual*.get("_id")*.toString() as Set
                def changed = actual.findAll { it.get("_id").toString() in expectedIds && !(it in expected) }*.get("_id")
                diffs << "Mongo '$name': expected ${expected.size()} docs, found ${actual.size()}; " +
                        "missing ${expectedIds - actualIds}, extra ${actualIds - expectedIds}, changed $changed"
            }
        }

        elasticTemplate.indexOps(ElasticCase).refresh()
        elasticTemplate.indexOps(ElasticTask).refresh()
        Map<String, Long> expectedCases = elasticCaseSnapshot.collectEntries { [(it.stringId): it.version] }
        Map<String, Long> actualCases = elasticCaseRepository.findAll().collectEntries { [(it.stringId): it.version] }
        if (actualCases != expectedCases || elasticCaseRepository.count() != expectedCases.size()) {
            diffs << "Elastic cases: expected $expectedCases, found $actualCases (count ${elasticCaseRepository.count()})"
        }
        Set<String> expectedTasks = elasticTaskSnapshot*.stringId as Set
        List<String> actualTasks = elasticTaskRepository.findAll()*.stringId
        if ((actualTasks as Set) != expectedTasks || actualTasks.size() != expectedTasks.size()) {
            diffs << "Elastic tasks: missing ${expectedTasks - actualTasks}, extra ${(actualTasks as Set) - expectedTasks}, " +
                    "count ${actualTasks.size()} (expected ${expectedTasks.size()})"
        }

        if (diffs) {
            throw new IllegalStateException("Test DB snapshot was not restored exactly:\n" + diffs.join("\n"))
        }
    }

    // One TestHelper per cached Spring context; all contexts share the same Mongo/Elastic, so the previous test
    // may have run in another context whose queues must drain too.
    private static final Set<TestHelper> INSTANCES = ConcurrentHashMap.newKeySet()

    @PostConstruct
    void register() { INSTANCES.add(this) }

    @PreDestroy
    void unregister() { INSTANCES.remove(this) }

    /**
     * Use instead of Thread.sleep when a test waits for async work: Elastic indexing of cases, tasks and nets,
     * and @Async methods (history, task events, async actions). Returns once all of it is done and searchable.
     */
    void waitForAsync() {
        awaitElasticQueues()
        refreshElasticIndices()
    }

    private void refreshElasticIndices() {
        [ElasticCase, ElasticTask, ElasticPetriNet].each { elasticTemplate.indexOps(it).refresh() }
    }

    // Waits until async Elastic indexing started by the previous test is done, otherwise it lands after the restore.
    // Cases and nets: per-id single-thread executors (FIFO), so a finished no-op means all earlier jobs are done.
    // Tasks and other @Async work: taskExecutor -> ElasticTaskQueueManager -> elasticTaskExecutor; polled until idle.
    // Two rounds, because an @Async method may still submit to a case executor after the first drain.
    private static void awaitElasticQueues() {
        long deadline = System.currentTimeMillis() + 15_000
        boolean idle = false
        2.times {
            INSTANCES.each { it.drainCaseExecutors(deadline) }
            int idleInRow = 0
            while (System.currentTimeMillis() < deadline && idleInRow < 2) {
                idleInRow = INSTANCES.every { it.elasticTasksIdle() } ? idleInRow + 1 : 0
                if (idleInRow < 2) Thread.sleep(20)
            }
            idle = idleInRow >= 2
        }
        if (!idle) {
            log.warn("Elastic indexing queues did not drain within timeout")
        }
    }

    private void drainCaseExecutors(long deadline) {
        Map<String, ExecutorService> caseExecutors = elasticCaseExecutor.@executors
        List<ExecutorService> executors
        synchronized (caseExecutors) { executors = new ArrayList<>(caseExecutors.values()) }
        executors.each { ExecutorService executor ->
            try {
                executor.submit({} as Runnable).get(Math.max(deadline - System.currentTimeMillis(), 1), TimeUnit.MILLISECONDS)
            } catch (RejectedExecutionException ignored) {
                // executor already shut down (evicted from the map)
            } catch (TimeoutException ignored) {
            }
        }
    }

    private boolean elasticTasksIdle() {
        Map taskQueues = elasticTaskQueueManager.@taskQueues
        Set activeTasks = elasticTaskQueueManager.@activeTasks
        return taskQueues.isEmpty() && activeTasks.isEmpty() &&
                [asyncTaskExecutor, elasticTaskExecutor].findAll { it instanceof ThreadPoolTaskExecutor }
                        .every { ThreadPoolTaskExecutor it -> it.activeCount == 0 && it.threadPoolExecutor.queue.isEmpty() }
    }

    private void awaitElasticIndexing() {
        long caseCount = template.getCollection(template.getCollectionName(Case)).countDocuments()
        long taskCount = template.getCollection(template.getCollectionName(Task)).countDocuments()
        long deadline = System.currentTimeMillis() + 15_000
        while (System.currentTimeMillis() < deadline) {
            elasticTemplate.indexOps(ElasticCase).refresh()
            elasticTemplate.indexOps(ElasticTask).refresh()
            if (elasticCaseRepository.count() >= caseCount && elasticTaskRepository.count() >= taskCount) return
            Thread.sleep(200)
        }
        log.warn("Elastic indexing did not catch up before taking test DB snapshot (Mongo cases: $caseCount, tasks: $taskCount; " +
                "Elastic cases: ${elasticCaseRepository.count()}, tasks: ${elasticTaskRepository.count()})")
    }

    private void fullReset() {
        template.db.drop()
        elasticTaskRepository.deleteAll()
        elasticCaseRepository.deleteAll()
        uriNodeRepository.deleteAll()
        userRepository.deleteAll()
        roleRepository.deleteAll()
        roleService.clearCache()
        actionsCacheService.clearActionCache()
        actionsCacheService.clearFunctionCache()
        actionsCacheService.clearNamespaceFunctionCache()
        petriNetService.evictAllCaches()

        mongoDbRunner.run()
        defaultRoleRunner.run()
        anonymousRoleRunner.run()
        systemUserRunner.run()
        uriRunner.run()
        processRunner.run()
        groupRunner.run()
        filterRunner.run()
        impersonationRunner.run()
        superCreator.run()
        finisherRunner.run()
        menuItemService.ensureDatabaseIndexes()
    }
}