package com.netgrif.application.engine.workflow

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
class SetDataOnButtonTest {

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

    String CHILD_CASE_FIELD_ID = "child_case_id"
    String TEXT_0_FIELD_ID = "text_0"
    String TEXT_1_FIELD_ID = "text_1"
    String TEXT_2_FIELD_ID = "text_2"
    String BUTTON_0_FIELD_ID = "button_0"
    String BUTTON_1_FIELD_ID = "button_1"
    String BUTTON_2_FIELD_ID = "button_2"
    String OUTPUT_TEXT_0 = "Clicked 0!"
    String OUTPUT_TEXT_1 = "Clicked 1!"
    String OUTPUT_TEXT_2 = "Clicked 2!"
    String PARENT_CASE = "Set Data On Button Parent"
    String CHILD_CASE = "Set Data On Button Child"
    String TEST_TRANSITION = "t1"
    String RESOURCE_PATH = "src/test/resources/button_set_data_test.xml"

    PetriNet net = null

    @BeforeEach
    void initNet() {
        testHelper.truncateDbs()
        net = petriNetService.importPetriNet(new FileInputStream(RESOURCE_PATH), VersionType.MAJOR, userService.loggedOrSystem.transformToLoggedUser()).getNet()
        assert net != null
    }

    @Test
    void setDataFromTestCase() {
        Case parentCase = helper.createCase(PARENT_CASE, net)
        Case childCase = helper.createCase(CHILD_CASE, net)

        parentCase.dataSet[CHILD_CASE_FIELD_ID].value = childCase.getStringId()
        assert parentCase.dataSet[CHILD_CASE_FIELD_ID].value == childCase.getStringId()
        workflowService.save(parentCase)

        Task parentTask = taskService.searchOne(QTask.task.caseTitle.eq(PARENT_CASE) & QTask.task.transitionId.eq(TEST_TRANSITION))
        assert parentTask != null

        taskService.assignTask(parentTask.getStringId())
        taskService.finishTask(parentTask.getStringId())

        childCase = workflowService.findOne(childCase.getStringId())

        assert childCase.dataSet[TEXT_0_FIELD_ID].value.toString() == OUTPUT_TEXT_0
        assert childCase.dataSet[TEXT_1_FIELD_ID].value.toString() == OUTPUT_TEXT_1
        assert childCase.dataSet[TEXT_2_FIELD_ID].value.toString() == OUTPUT_TEXT_2
    }

    @Test
    void setData() {
        Case testCase = helper.createCase(PARENT_CASE, net)

        Task testCaseTask = taskService.searchOne(QTask.task.caseTitle.eq(PARENT_CASE) & QTask.task.transitionId.eq(TEST_TRANSITION))
        assert testCaseTask != null

        dataService.setData(testCaseTask.stringId, ImportHelper.populateDataset([
                "button_0": [
                        "value": "42",
                        "type" : "button"
                ],
                "button_1": [
                        "value": 42,
                        "type" : "button"
                ],
                "button_2": [
                        "type" : "button"
                ]
        ]))

        testCase = workflowService.findOne(testCase.getStringId())

        assert testCase.dataSet[BUTTON_0_FIELD_ID].value == 42
        assert testCase.dataSet[BUTTON_1_FIELD_ID].value == 42
        assert testCase.dataSet[BUTTON_2_FIELD_ID].value == 1
        assert testCase.dataSet[TEXT_0_FIELD_ID].value.toString() == OUTPUT_TEXT_0
        assert testCase.dataSet[TEXT_1_FIELD_ID].value.toString() == OUTPUT_TEXT_1
        assert testCase.dataSet[TEXT_2_FIELD_ID].value.toString() == OUTPUT_TEXT_2
    }
}
