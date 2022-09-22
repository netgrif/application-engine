package com.netgrif.application.engine.export.service

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest
import com.netgrif.application.engine.export.service.interfaces.IExportService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension;


@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class ExportServiceTest {

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IUserService userService

    @Autowired
    private TestHelper testHelper

    @Autowired
    private TaskRepository taskRepository

    @Autowired
    private ActionDelegate actionDelegate

    @Autowired
    private IExportService exportService

    PetriNet testNet
    Case mainCase

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        Optional<PetriNet> optionalTestNet = importHelper.createNet("NAE-1290_Export_actions.xml", VersionType.MAJOR)
        assert optionalTestNet.isPresent()
        testNet = optionalTestNet.get()
        mainCase = importHelper.createCase("export test main", testNet)
        9.times {
            importHelper.createCase("export test", testNet)
        }
    }

    @Test
    @Order(2)
    void testCaseMongoExport() {
        String exportTask = mainCase.tasks.find { it.transition == "t1" }.task
        taskService.assignTask(userService.findByEmail("super@netgrif.com", false).transformToLoggedUser(), exportTask)
        File csvFile = new File("src/test/resources/csv/case_mongo_export.csv")
        assert csvFile.readLines().size() == 11
        String[] headerSplit = csvFile.readLines()[0].split(",")
        assert (headerSplit.contains("immediate_multichoice")
                && headerSplit.contains("immediate_number")
                && !headerSplit.contains("text"))
        taskService.cancelTask(userService.getLoggedOrSystem().transformToLoggedUser(), exportTask)
    }

    @Test
    @Order(3)
    void testCaseElasticExport() {
        Thread.sleep(5000)  //Elastic wait
        String exportTask = mainCase.tasks.find { it.transition == "t2" }.task
        taskService.assignTask(userService.findByEmail("super@netgrif.com", false).transformToLoggedUser(), exportTask)
        File csvFile = new File("src/test/resources/csv/case_elastic_export.csv")
        assert csvFile.readLines().size() == 11
        String[] headerSplit = csvFile.readLines()[0].split(",")
        assert (headerSplit.contains("text")
                && !headerSplit.contains("immediate_multichoice")
                && !headerSplit.contains("immediate_number"))
        taskService.cancelTask(userService.getLoggedOrSystem().transformToLoggedUser(), exportTask)
    }

    @Test
    @Order(4)
    void testTaskMongoExport() {
        String exportTask = mainCase.tasks.find { it.transition == "t3" }.task
        taskService.assignTask(userService.findByEmail("super@netgrif.com", false).transformToLoggedUser(), exportTask)
        File csvFile = new File("src/test/resources/csv/task_mongo_export.csv")
        assert csvFile.readLines().size() == 11
        String[] headerSplit = csvFile.readLines()[0].split(",")
        assert (headerSplit.contains("immediate_multichoice")
                && headerSplit.contains("immediate_number")
                && headerSplit.contains("text")
                && !headerSplit.contains("no_export"))
        taskService.cancelTask(userService.getLoggedOrSystem().transformToLoggedUser(), exportTask)
    }

    @Test
    @Order(1)
    @Disabled("Github action")
    void testTaskElasticExport() {
        Thread.sleep(10000)  //Elastic wait
        String exportTask = mainCase.tasks.find { it.transition == "t4" }.task
        taskService.assignTask(userService.findByEmail("super@netgrif.com", false).transformToLoggedUser(), exportTask)
        Thread.sleep(20000)  //Elastic wait

        def processId = petriNetService.getNewestVersionByIdentifier("export_test").stringId
        def taskRequest = new ElasticTaskSearchRequest()
        taskRequest.process = [new com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.PetriNet(processId)] as List
        taskRequest.transitionId = ["t4"] as List
        actionDelegate.exportTasksToFile([taskRequest],"src/test/resources/csv/task_elastic_export.csv",null, userService.findByEmail("super@netgrif.com", false).transformToLoggedUser())
        File csvFile = new File("src/test/resources/csv/task_elastic_export.csv")
        int pocet = ((taskRepository.count(QTask.task.processId.eq(processId).and(QTask.task.transitionId.eq("t4"))) as int) + 1)
        assert csvFile.readLines().size() == pocet
        String[] headerSplit = csvFile.readLines()[0].split(",")
        assert (headerSplit.contains("immediate_multichoice")
                && headerSplit.contains("immediate_number")
                && !headerSplit.contains("text")
                && !headerSplit.contains("no_export"))
        taskService.cancelTask(userService.findByEmail("super@netgrif.com", false).transformToLoggedUser(), exportTask)
    }

    @Test
    void buildDefaultCsvTaskHeaderTest(){
        def processId = petriNetService.getNewestVersionByIdentifier("export_test").stringId
        String exportTask = mainCase.tasks.find { it.transition == "t4" }.task
        taskService.assignTask(userService.findByEmail("super@netgrif.com", false).transformToLoggedUser(), exportTask)
        List<Task> task = taskRepository.findAll(QTask.task.processId.eq(processId).and(QTask.task.transitionId.eq("t4")))
        Set<String> header = exportService.buildDefaultCsvTaskHeader(task)
        assert header != null
        assert header.size() == 2
    }

}
