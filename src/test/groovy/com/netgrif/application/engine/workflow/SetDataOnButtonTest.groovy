package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.ButtonField
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.petrinet.domain.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.params.SetDataParams
import com.netgrif.application.engine.workflow.domain.params.TaskParams
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import groovy.transform.CompileStatic
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
@CompileStatic
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

    @Autowired
    private SuperCreator superCreator

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
        net = petriNetService.importPetriNet(new ImportPetriNetParams(
                new FileInputStream(RESOURCE_PATH), VersionType.MAJOR, userService.loggedOrSystem.transformToLoggedUser())).getNet()
        assert net != null
    }

    @Test
    void setDataFromTestCase() {
        Case parentCase = helper.createCase(PARENT_CASE, net)
        Case childCase = helper.createCase(CHILD_CASE, net)
        (parentCase.dataSet.get(CHILD_CASE_FIELD_ID) as TextField).rawValue = childCase.getStringId()
        assert parentCase.dataSet.get(CHILD_CASE_FIELD_ID).rawValue == childCase.getStringId()
        workflowService.save(parentCase)

        Task parentTask = taskService.searchOne(QTask.task.caseTitle.eq(PARENT_CASE) & QTask.task.transitionId.eq(TEST_TRANSITION))
        assert parentTask != null

        taskService.assignTask(new TaskParams(parentTask))
        taskService.finishTask(new TaskParams(parentTask))

        childCase = workflowService.findOne(childCase.getStringId())
        assert childCase.dataSet.get(TEXT_0_FIELD_ID).rawValue.toString() == OUTPUT_TEXT_0
        assert childCase.dataSet.get(TEXT_1_FIELD_ID).rawValue.toString() == OUTPUT_TEXT_1
        assert childCase.dataSet.get(TEXT_2_FIELD_ID).rawValue.toString() == OUTPUT_TEXT_2
    }

    @Test
    void setData() {
        Case testCase = helper.createCase(PARENT_CASE, net)

        Task testCaseTask = taskService.searchOne(QTask.task.caseTitle.eq(PARENT_CASE) & QTask.task.transitionId.eq(TEST_TRANSITION))
        assert testCaseTask != null
        dataService.setData(new SetDataParams(testCaseTask.stringId, new DataSet([
                "button_0": new ButtonField(rawValue: 42),
                "button_1": new ButtonField(rawValue: 42),
                "button_2": new ButtonField(rawValue: 42)
        ] as Map<String, Field<?>>), superCreator.getSuperUser()))

        testCase = workflowService.findOne(testCase.getStringId())

        assert testCase.dataSet.get(BUTTON_0_FIELD_ID).rawValue == 42
        assert testCase.dataSet.get(BUTTON_1_FIELD_ID).rawValue == 42
        assert testCase.dataSet.get(BUTTON_2_FIELD_ID).rawValue == 42
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).rawValue.toString() == OUTPUT_TEXT_0
        assert testCase.dataSet.get(TEXT_1_FIELD_ID).rawValue.toString() == OUTPUT_TEXT_1
        assert testCase.dataSet.get(TEXT_2_FIELD_ID).rawValue.toString() == OUTPUT_TEXT_2
    }
}
