package com.netgrif.workflow.action

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@ActiveProfiles(["test"])
@SpringBootTest
@RunWith(SpringRunner.class)
class ExportActionsTest {

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

    PetriNet testNet
    Case mainCase

    @Before
    void before(){
        testHelper.truncateDbs()
        Optional<PetriNet> optionalTestNet =  importHelper.createNet("NAE-1290_Export_actions.xml", VersionType.MAJOR)
        assert optionalTestNet.isPresent()
        testNet = optionalTestNet.get()
        mainCase = importHelper.createCase("export test main",testNet)
        9.times {
            importHelper.createCase("export test",testNet)
        }
    }

    @Test
    void testCaseMongoExport(){
        String exportTask = mainCase.tasks.find{it.transition == "t1"}.task
        taskService.assignTask(exportTask)
        File csvFile = new File("src/test/resources/csv/case_mongo_export.csv")
        assert csvFile.readLines().size() == 11
        String[] headerSplit = csvFile.readLines()[0].split(",")
        assert (headerSplit.contains("immediate_multichoice")
                && headerSplit.contains("immediate_number")
                && !headerSplit.contains("text"))
        taskService.cancelTask(userService.getLoggedOrSystem().transformToLoggedUser(),exportTask)
    }

    @Test
    void testCaseElasticExport(){
        String exportTask = mainCase.tasks.find{it.transition == "t2"}.task
        taskService.assignTask(exportTask)
        File csvFile = new File("src/test/resources/csv/case_elastic_export.csv")
        assert csvFile.readLines().size() == 11
        String[] headerSplit = csvFile.readLines()[0].split(",")
        assert (headerSplit.contains("text")
                && !headerSplit.contains("immediate_multichoice")
                && !headerSplit.contains("immediate_number"))
        taskService.cancelTask(userService.getLoggedOrSystem().transformToLoggedUser(),exportTask)
    }

    @Test
    void testTaskMongoExport(){
        String exportTask = mainCase.tasks.find{it.transition == "t3"}.task
        taskService.assignTask(exportTask)
        File csvFile = new File("src/test/resources/csv/task_mongo_export.csv")
        assert csvFile.readLines().size() == 11
        String[] headerSplit = csvFile.readLines()[0].split(",")
        assert (headerSplit.contains("immediate_multichoice")
                && headerSplit.contains("immediate_number")
                && headerSplit.contains("text")
                && !headerSplit.contains("no_export"))
        taskService.cancelTask(userService.getLoggedOrSystem().transformToLoggedUser(),exportTask)
    }

    @Test
    void testTaskElasticExport(){
//        todo problém s elastictask service
        Thread.sleep(5000)
        String exportTask = mainCase.tasks.find{it.transition == "t4"}.task
        taskService.assignTask(userService.findByEmail("super@netgrif.com", false).transformToLoggedUser(),exportTask)
        File csvFile = new File("src/test/resources/csv/task_elastic_export.csv")
        assert csvFile.readLines().size() == 11
        String[] headerSplit = csvFile.readLines()[0].split(",")
        assert (headerSplit.contains("immediate_multichoice")
                && headerSplit.contains("immediate_number")
                && !headerSplit.contains("text")
                && !headerSplit.contains("no_export"))
        taskService.cancelTask(userService.getLoggedOrSystem().transformToLoggedUser(),exportTask)
    }

}
