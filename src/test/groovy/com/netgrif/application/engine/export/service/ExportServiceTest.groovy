package com.netgrif.application.engine.export.service

import com.netgrif.application.engine.TestHelper

import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest
import com.netgrif.application.engine.export.service.interfaces.IExportService
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.PetriNet
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension


@SpringBootTest
@ActiveProfiles(["test"])
@CompileStatic
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
    private TestHelper testHelper

    @Autowired
    private TaskRepository taskRepository

    @Autowired
    private ActionDelegate actionDelegate

    @Autowired
    private IExportService exportService

    @Autowired
    private SuperCreator superCreator

    Process testNet
    Case mainCase

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        Optional<Process> optionalTestNet = importHelper.createNet("NAE-1290_Export_actions.xml", VersionType.MAJOR)
        assert optionalTestNet.isPresent()
        testNet = optionalTestNet.get()
        TestHelper.login(superCreator.superIdentity)
        mainCase = importHelper.createCase("export test main", testNet)
        9.times {
            importHelper.createCase("export test", testNet)
        }
    }

    @Test
    @Order(2)
    void testCaseMongoExport() {
        String exportTask = mainCase.getTaskStringId("t1")
        taskService.assignTask(superCreator.getLoggedSuper().activeActorId, exportTask)
        File csvFile = new File("src/test/resources/csv/case_mongo_export.csv")
        assert csvFile.readLines().size() == 11
        String[] headerSplit = csvFile.readLines()[0].split(",")
        assert (headerSplit.contains("immediate_multichoice")
                && headerSplit.contains("immediate_number")
                && !headerSplit.contains("text"))
        taskService.cancelTask(superCreator.getLoggedSuper().activeActorId, exportTask)
    }

    @Test
    @Order(3)
    void testCaseElasticExport() {
        Thread.sleep(5000)  //Elastic wait
        String exportTask = mainCase.getTaskStringId("t2")
        taskService.assignTask(superCreator.getLoggedSuper().activeActorId, exportTask)
        File csvFile = new File("src/test/resources/csv/case_elastic_export.csv")
        assert csvFile.readLines().size() == 11
        String[] headerSplit = csvFile.readLines()[0].split(",")
        assert (headerSplit.contains("text")
                && !headerSplit.contains("immediate_multichoice")
                && !headerSplit.contains("immediate_number"))
        taskService.cancelTask(superCreator.getLoggedSuper().activeActorId, exportTask)
    }

    @Test
    @Order(4)
    void testTaskMongoExport() {
        String exportTask = mainCase.getTaskStringId("t3")
        taskService.assignTask(superCreator.getLoggedSuper().activeActorId, exportTask)
        File csvFile = new File("src/test/resources/csv/task_mongo_export.csv")
        assert csvFile.readLines().size() == 11
        String[] headerSplit = csvFile.readLines()[0].split(",")
        assert (headerSplit.contains("immediate_multichoice")
                && headerSplit.contains("immediate_number")
                && headerSplit.contains("text")
                && !headerSplit.contains("no_export"))
        taskService.cancelTask(superCreator.getLoggedSuper().activeActorId, exportTask)
    }

    @Test
    @Order(1)
    @Disabled("Github action")
    void testTaskElasticExport() {
        Thread.sleep(10000)  //Elastic wait
        String exportTask = mainCase.getTaskStringId("t4")
        taskService.assignTask(superCreator.getLoggedSuper().activeActorId, exportTask)
        Thread.sleep(20000)  //Elastic wait

        def processId = petriNetService.getNewestVersionByIdentifier("export_test").stringId
        def taskRequest = new ElasticTaskSearchRequest()
        taskRequest.process = [new PetriNet(processId)] as List
        taskRequest.transitionId = ["t4"] as List
        actionDelegate.exportTasksToFile([taskRequest],"src/test/resources/csv/task_elastic_export.csv",null,
                superCreator.getLoggedSuper())
        File csvFile = new File("src/test/resources/csv/task_elastic_export.csv")
        int count = ((taskRepository.count(QTask.task.processId.eq(processId) & QTask.task.transitionId.eq("t4")) as int) + 1)
        assert csvFile.readLines().size() == count
        String[] headerSplit = csvFile.readLines()[0].split(",")
        assert (headerSplit.contains("immediate_multichoice")
                && headerSplit.contains("immediate_number")
                && !headerSplit.contains("text")
                && !headerSplit.contains("no_export"))
        taskService.cancelTask(superCreator.getLoggedSuper().activeActorId, exportTask)
    }

    @Test
    void buildDefaultCsvTaskHeaderTest(){
        def processId = petriNetService.getNewestVersionByIdentifier("export_test").stringId
        String exportTask = mainCase.getTaskStringId("t4")
        taskService.assignTask(superCreator.getLoggedSuper().activeActorId, exportTask)
        List<Task> task = taskRepository.findAll(QTask.task.processId.eq(processId) & QTask.task.transitionId.eq("t4")) as List<Task>
        Set<String> header = exportService.buildDefaultCsvTaskHeader(task)
        assert header != null
        // TODO: release/8.0.0 empty header
/*
assert header.size() == 2
       |      |      |
       []     0      false
 */
        assert header.size() == 2
    }
}
