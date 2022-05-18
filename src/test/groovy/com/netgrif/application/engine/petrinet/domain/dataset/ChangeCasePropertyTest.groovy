package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ChangeCasePropertyTest {
    @Autowired
    private ITaskService taskService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IDataService dataService

    @Autowired
    private ImportHelper helper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IUserService userService

    @Autowired
    private TestHelper testHelper

    String PRE_ASSIGN_TITLE = "Pre assign title"
    String SET_DATA_TITLE = "Set action title"
    String TEST_CASE_TITLE = "Original title"
    String TEST_TRANSITION = "t1"
    String RESOURCE_PATH = "src/test/resources/case_name_change_test.xml"

    PetriNet net = null

    @BeforeEach
    void initNet() {
        testHelper.truncateDbs()
        net = petriNetService.importPetriNet(new FileInputStream(RESOURCE_PATH), VersionType.MAJOR, userService.loggedOrSystem.transformToLoggedUser()).getNet()
        assert net != null
    }

    @Test
    void setTitleFromEvent() {
        Case testCase = helper.createCase(TEST_CASE_TITLE, net)
        assert testCase
        assert testCase.getTitle() == TEST_CASE_TITLE

        Task testCaseTask = taskService.searchOne(QTask.task.caseTitle.eq(TEST_CASE_TITLE) & QTask.task.transitionId.eq(TEST_TRANSITION))
        assert testCaseTask

        taskService.assignTask(testCaseTask.getStringId())
        taskService.finishTask(testCaseTask.getStringId())

        testCase = workflowService.findOne(testCase.getStringId())
        testCaseTask = taskService.findOne(testCaseTask.getStringId())

        assert testCase.getTitle() == PRE_ASSIGN_TITLE
        assert testCaseTask.getCaseTitle() == PRE_ASSIGN_TITLE
    }

    @Test
    void setTitleFromFieldAction() {
        Case testCase = helper.createCase(TEST_CASE_TITLE, net)
        assert testCase
        assert testCase.getTitle() == TEST_CASE_TITLE

        Task testCaseTask = taskService.searchOne(QTask.task.caseTitle.eq(TEST_CASE_TITLE) & QTask.task.transitionId.eq(TEST_TRANSITION))
        assert testCaseTask

        taskService.assignTask(testCaseTask.getStringId())
        dataService.setData(testCaseTask.stringId, ImportHelper.populateDataset([
                "bln": [
                        "value": "true",
                        "type" : "boolean"
                ]
        ]))
        taskService.finishTask(testCaseTask.getStringId())

        testCase = workflowService.findOne(testCase.getStringId())
        testCaseTask = taskService.findOne(testCaseTask.getStringId())

        assert testCase.getTitle() == SET_DATA_TITLE
        assert testCaseTask.getCaseTitle() == SET_DATA_TITLE
    }

}
