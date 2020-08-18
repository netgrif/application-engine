package com.netgrif.workflow.elastic

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.WorkflowManagementSystemApplication
import com.netgrif.workflow.elastic.domain.ElasticTask
import com.netgrif.workflow.elastic.domain.ElasticTaskRepository
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = WorkflowManagementSystemApplication.class
)
@AutoConfigureMockMvc
class ElasticTaskTest {

    @Autowired
    private ImportHelper helper
    @Autowired
    private IPetriNetService petriNetService
    @Autowired
    private SuperCreator superCreator
    @Autowired
    private ElasticTaskRepository elasticTaskRepository
    @Autowired
    private IElasticTaskService elasticTaskService
    @Autowired
    private TaskRepository taskRepository
    @Autowired
    private ITaskService taskService
    @Autowired
    private TestHelper testHelper

    @Value("classpath:task_reindex_test.xml")
    private Resource netResource

    @Before
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void taskReindexTest() {
        def netOptional = petriNetService.importPetriNet(netResource.inputStream, "major", superCreator.getLoggedSuper())
        assert netOptional.isPresent()
        def net = netOptional.get()

        def testCase = helper.createCase("Test case", net)
        assert testCase.tasks.size() == 3

        List<Task> tasks = taskRepository.findAllByCaseId(testCase.getStringId())
        for (Task task : tasks) {
            elasticTaskService.indexNow(new ElasticTask(task))
        }
        def all = elasticTaskRepository.findAll()
        assert all.size() == 3
        assert all.find { it.transitionId == "2" }
        assert all.find { it.transitionId == "2" }
        assert all.find { it.transitionId == "5" }

        taskService.assignTask(tasks.find { it.transitionId == "2" }.stringId)
        taskService.finishTask(tasks.find { it.transitionId == "2" }.stringId)

        tasks = taskRepository.findAllByCaseId(testCase.getStringId())
        for (Task task : tasks) {
            elasticTaskService.indexNow(new ElasticTask(task));
        }

        all = elasticTaskRepository.findAll()
        assert all.size() == 2
        assert all.find { it.transitionId == "9" }
        assert all.find { it.transitionId == "5" }
    }
}
