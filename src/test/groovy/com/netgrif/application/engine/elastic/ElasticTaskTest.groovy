package com.netgrif.application.engine.elastic

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.ApplicationEngine
import com.netgrif.core.elastic.domain.ElasticTask
import com.netgrif.application.engine.elastic.domain.ElasticTaskRepository
import com.netgrif.application.engine.elastic.service.ReindexingTask
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService
import com.netgrif.core.petrinet.domain.VersionType
import com.netgrif.adapter.petrinet.service.PetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.core.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.lang.management.ManagementFactory
import java.lang.management.ThreadInfo
import java.lang.management.ThreadMXBean
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ApplicationEngine.class
)
@AutoConfigureMockMvc
class ElasticTaskTest {

    @Autowired
    private ElasticTaskRepository elasticTaskRepository

    @Autowired
    private TaskRepository taskRepository

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper helper

    @Autowired
    private ReindexingTask reindexingTask

    @Autowired
    private PetriNetService petriNetService

    @Autowired
    private SuperCreatorRunner superCreator

    @Autowired
    protected IElasticTaskService elasticTaskService

    @Value("classpath:task_reindex_test.xml")
    private Resource netResource

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void taskReindexTest() {
        def optional = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert optional.getNet() != null

        def net = optional.getNet()
        10.times {
            helper.createCase("Case $it", net)
        }

        reindexingTask.forceReindexPage(QCase.case$.lastModified.before(LocalDateTime.now()), 0, 1)
    }

    @Test
    void testIndexTask() {
        ElasticTask task = new com.netgrif.adapter.elastic.domain.ElasticTask()
        task.setTaskId("TestTask")
        task.setTitle("Test")
        task.setProcessId("Process")

        Future<ElasticTask> resultFuture = elasticTaskService.scheduleTaskIndexing(task)
        ElasticTask result = resultFuture.get()

        assert result != null
        assert result.getTitle().equals("Test")

        ElasticTask resultFromDB = elasticTaskRepository.findByTaskId("TestTask")
        assert resultFromDB.getTitle() == "Test"

        task.setTitle("Test2")

        Future<ElasticTask> resultFuture2 = elasticTaskService.scheduleTaskIndexing(task)
        ElasticTask result2 = resultFuture2.get()

        assert result2 != null
        assert result2.getTitle().equals("Test2")

        task.setTitle("Test3")

        Future<ElasticTask> resultFuture3 = elasticTaskService.scheduleTaskIndexing(task)
        ElasticTask result3 = resultFuture3.get()

        assert result3 != null
        assert result3.getTitle().equals("Test3")
    }

    @Test
    void testRemoveTaskByProcess() throws Exception {
        ElasticTask task = new com.netgrif.adapter.elastic.domain.ElasticTask()
        task.setTaskId("TestTask")
        task.setTitle("Test")
        task.setProcessId("Process")

        Future<ElasticTask> resultFuture = elasticTaskService.scheduleTaskIndexing(task) as CompletableFuture<ElasticTask>
        ElasticTask result = resultFuture.get()
        assert  result

        ElasticTask indexedTask = elasticTaskRepository.findByTaskId("TestTask")
        assert indexedTask != null
        CountDownLatch latch = new CountDownLatch(1)
        elasticTaskService.removeByPetriNetId("Process")

        latch.await(10, TimeUnit.SECONDS)
        ElasticTask deletedTask = elasticTaskRepository.findByTaskId("TestTask")
        assert deletedTask == null
    }

    @Test
    void reindexTaskAllTest() throws InterruptedException, ExecutionException {
        int pocetOpakovani = 100
        ElasticTask task = new com.netgrif.adapter.elastic.domain.ElasticTask()
        task.setTaskId("TestTask")
        task.setTitle("START")
        task.setProcessId("TestProcess")
        elasticTaskService.index(task)

        ExecutorService executorService = Executors.newFixedThreadPool(3)
        CountDownLatch latch = new CountDownLatch(pocetOpakovani)
        (1..pocetOpakovani).each { it ->
            final int index = it
            executorService.submit(() -> {
                try {
                    ElasticTask taskParallel = new com.netgrif.adapter.elastic.domain.ElasticTask()
                    taskParallel.setTaskId("TestTask")
                    taskParallel.setTitle("START" + index)
                    taskParallel.setProcessId("TestProcess")
                    Future<ElasticTask> resultFuture = elasticTaskService.scheduleTaskIndexing(taskParallel)
                    ElasticTask result = resultFuture.get()
                    assert result != null
                    assert result.getTitle().equals("START" + index)
                } catch (Exception e) {
                    e.printStackTrace()
                } finally {
                    latch.countDown()
                }
            })
        }
        latch.await(5, TimeUnit.SECONDS)
        ElasticTask indexedTask = elasticTaskRepository.findByTaskId("TestTask")
        assert indexedTask != null
        elasticTaskService.removeByPetriNetId("TestProcess")

        ElasticTask deletedTask = elasticTaskRepository.findByTaskId("TestTask")
        assert deletedTask == null
    }



    @Test
    void reindexTaskTest() throws InterruptedException, ExecutionException {
        int pocetOpakovani = 100
        ElasticTask task = new com.netgrif.adapter.elastic.domain.ElasticTask()
        task.setTaskId("TestTask")
        task.setTitle("START")
        task.setStringId("TestTask")
        elasticTaskService.index(task)

        ExecutorService executorService = Executors.newFixedThreadPool(3)
        CountDownLatch latch = new CountDownLatch(pocetOpakovani)

        (1..pocetOpakovani).each { it ->
            final int index = it
            executorService.submit(() -> {
                try {
                    ElasticTask taskParallel = new com.netgrif.adapter.elastic.domain.ElasticTask()
                    taskParallel.setTaskId("TestTask")
                    taskParallel.setTitle("START" + index)
                    taskParallel.setStringId("TestTask")
                    Future<ElasticTask> resultFuture = elasticTaskService.scheduleTaskIndexing(taskParallel)
                    ElasticTask result = resultFuture.get()
                    assert result != null
                    assert result.getTitle().equals("START" + index)
                } catch (Exception e) {
                    e.printStackTrace()
                } finally {
                    latch.countDown()
                }
            })
        }

        latch.await(5, TimeUnit.SECONDS)

        String title = "FINISH"
        task.setTitle(title)
        Future<ElasticTask> resultFuture = elasticTaskService.scheduleTaskIndexing(task) as CompletableFuture<ElasticTask>
        ElasticTask result = resultFuture.get()
        assert result != null
        assert result.getTitle().equals(title)
    }


    @Test
    void reindexTaskParallelTest() throws InterruptedException, ExecutionException {
        int pocetOpakovani = 1000
        ElasticTask task = new com.netgrif.adapter.elastic.domain.ElasticTask()
        task.setTaskId("TestTask")
        task.setTitle("START")
        elasticTaskService.index(task)

        ExecutorService executorService = Executors.newFixedThreadPool(3)
        CountDownLatch latch = new CountDownLatch(pocetOpakovani)

        (1..pocetOpakovani).each { it ->
            final int index = it
            executorService.submit(() -> {
                try {
                    ElasticTask taskParallel = new com.netgrif.adapter.elastic.domain.ElasticTask()
                    taskParallel.setTaskId("TestTask"+ index)
                    taskParallel.setTitle("START")
                    taskParallel.setStringId("TestTask"+index)
                    Future<ElasticTask> resultFuture = elasticTaskService.scheduleTaskIndexing(taskParallel)
                    ElasticTask result = resultFuture.get()
                    assert result != null
                    assert result.getTitle().equals("TestTask"+ index)
                } catch (Exception e) {
                    e.printStackTrace()
                } finally {
                    latch.countDown()
                }
            })
        }
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean()
        long[] threadIds = threadMXBean.getAllThreadIds()
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds)

        long count = Arrays.stream(threadInfos)
                .filter(threadInfo -> threadInfo != null && threadInfo.getThreadName().startsWith("ElasticTaskExecutor-"))
                .count()

        println("Počet vlákien s prefixom 'ElasticTaskExecutor-': " + count)

        latch.await(50, TimeUnit.SECONDS)
        String title = "FINISH"
        task.setTitle(title)
        Future<ElasticTask> resultFuture = elasticTaskService.scheduleTaskIndexing(task) as CompletableFuture<ElasticTask>
        ElasticTask result = resultFuture.get()
        assert result != null
        assert result.getTitle().equals(title)
    }


}
