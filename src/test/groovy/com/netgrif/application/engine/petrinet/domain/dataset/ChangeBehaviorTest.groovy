package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.dmg.pmml.time_series.MA
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
class ChangeBehaviorTest {

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

    String TEXT_0_FIELD_ID = "text_0"
    String TEXT_1_FIELD_ID = "text_1"
    String BOOLEAN_0_FIELD_ID = "boolean_0"
    String BOOLEAN_1_FIELD_ID = "boolean_1"
    String BOOLEAN_2_FIELD_ID = "boolean_2"
    String BOOLEAN_3_FIELD_ID = "boolean_3"
    String TEST_CASE_NAME = "Change behavior improvements"
    String MAIN_TRANSITION = "t1"
    String TEST_TRANSITION_1 = "t3"
    String TEST_TRANSITION_2 = "t4"
    String TEST_TRANSITION_3 = "t2"
    String RESOURCE_PATH = "src/test/resources/change_behavior_update.xml"

    PetriNet net = null

    @BeforeEach
    void initNet() {
        testHelper.truncateDbs()
        net = petriNetService.importPetriNet(new FileInputStream(RESOURCE_PATH), VersionType.MAJOR, userService.loggedOrSystem.transformToLoggedUser()).getNet()
        assert net != null
    }

    @Test
    void changeBehaviorOfSingleFieldOnSingleTransition() {
        Case testCase = helper.createCase(TEST_CASE_NAME, net)

        Task mainTask = taskService.searchOne(QTask.task.transitionId.eq(MAIN_TRANSITION) & QTask.task.caseId.eq(testCase.stringId))
        assert mainTask

        dataService.setData(mainTask.stringId, ImportHelper.populateDataset([
                "boolean_0": [
                        "value": true,
                        "type" : "boolean"
                ]
        ]))

        testCase = workflowService.findOne(testCase.getStringId())
        assert testCase.dataSet[BOOLEAN_0_FIELD_ID].value == true

        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(MAIN_TRANSITION).find {it.toString() == FieldBehavior.EDITABLE.toString() }
        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(MAIN_TRANSITION).find {it.toString() == FieldBehavior.REQUIRED.toString() }
        assert testCase.dataSet[TEXT_1_FIELD_ID].behavior.get(MAIN_TRANSITION).find {it.toString() == FieldBehavior.REQUIRED.toString() } == null
    }

    @Test
    void changeBehaviorOnEachTransition() {
        Case testCase = helper.createCase(TEST_CASE_NAME, net)

        Task mainTask = taskService.searchOne(QTask.task.transitionId.eq(MAIN_TRANSITION) & QTask.task.caseId.eq(testCase.stringId))
        assert mainTask
        Task otherTask1 = taskService.searchOne(QTask.task.transitionId.eq(TEST_TRANSITION_1) & QTask.task.caseId.eq(testCase.stringId))
        assert otherTask1
        Task otherTask2 = taskService.searchOne(QTask.task.transitionId.eq(TEST_TRANSITION_2) & QTask.task.caseId.eq(testCase.stringId))
        assert otherTask2

        dataService.setData(mainTask.stringId, ImportHelper.populateDataset([
                "boolean_1": [
                        "value": true,
                        "type" : "boolean"
                ]
        ]))

        testCase = workflowService.findOne(testCase.getStringId())
        assert testCase.dataSet[BOOLEAN_1_FIELD_ID].value == true

        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(MAIN_TRANSITION).find { it.toString() == FieldBehavior.EDITABLE.toString() }
        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(TEST_TRANSITION_1).find { it.toString() == FieldBehavior.EDITABLE.toString() }
        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(TEST_TRANSITION_2).find { it.toString() == FieldBehavior.EDITABLE.toString() }
        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(TEST_TRANSITION_3).find { it.toString() == FieldBehavior.EDITABLE.toString() }
        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(MAIN_TRANSITION).find { it.toString() == FieldBehavior.REQUIRED.toString() }
        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(TEST_TRANSITION_1).find { it.toString() == FieldBehavior.REQUIRED.toString() }
        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(TEST_TRANSITION_2).find { it.toString() == FieldBehavior.REQUIRED.toString() }
        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(TEST_TRANSITION_3).find { it.toString() == FieldBehavior.REQUIRED.toString() }
    }

    @Test
    void changeBehaviorOfSingleFieldOnMultipleTransitions() {
        Case testCase = helper.createCase(TEST_CASE_NAME, net)

        Task mainTask = taskService.searchOne(QTask.task.transitionId.eq(MAIN_TRANSITION) & QTask.task.caseId.eq(testCase.stringId))
        assert mainTask
        Task otherTask = taskService.searchOne(QTask.task.transitionId.eq(TEST_TRANSITION_1) & QTask.task.caseId.eq(testCase.stringId))
        assert otherTask

        dataService.setData(mainTask.stringId, ImportHelper.populateDataset([
                "boolean_2": [
                        "value": true,
                        "type" : "boolean"
                ]
        ]))

        testCase = workflowService.findOne(testCase.getStringId())
        assert testCase.dataSet[BOOLEAN_2_FIELD_ID].value == true

        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(MAIN_TRANSITION).find {it.toString() == FieldBehavior.EDITABLE.toString() }
        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(TEST_TRANSITION_1).find {it.toString() == FieldBehavior.EDITABLE.toString() }
        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(TEST_TRANSITION_2).find {it.toString() == FieldBehavior.EDITABLE.toString() } == null
    }

    @Test
    void changeBehaviorOfMultipleFieldsOnMultipleTransitions() {
        Case testCase = helper.createCase(TEST_CASE_NAME, net)

        Task mainTask = taskService.searchOne(QTask.task.transitionId.eq(MAIN_TRANSITION) & QTask.task.caseId.eq(testCase.stringId))
        assert mainTask
        Task otherTask = taskService.searchOne(QTask.task.transitionId.eq(TEST_TRANSITION_1) & QTask.task.caseId.eq(testCase.stringId))
        assert otherTask

        dataService.setData(mainTask.stringId, ImportHelper.populateDataset([
                "boolean_3": [
                        "value": true,
                        "type" : "boolean"
                ]
        ]))

        testCase = workflowService.findOne(testCase.getStringId())
        assert testCase.dataSet[BOOLEAN_3_FIELD_ID].value == true

        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(MAIN_TRANSITION).find { it.toString() == FieldBehavior.EDITABLE.toString() }
        assert testCase.dataSet[TEXT_1_FIELD_ID].behavior.get(MAIN_TRANSITION).find { it.toString() == FieldBehavior.EDITABLE.toString() }
        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(TEST_TRANSITION_1).find { it.toString() == FieldBehavior.EDITABLE.toString() }
        assert testCase.dataSet[TEXT_1_FIELD_ID].behavior.get(TEST_TRANSITION_1).find { it.toString() == FieldBehavior.EDITABLE.toString() }
        assert testCase.dataSet[TEXT_1_FIELD_ID].behavior.get(TEST_TRANSITION_2).find { it.toString() == FieldBehavior.EDITABLE.toString() } == null
    }

    @Test
    void initialBehaviorTest() {
        Case testCase = helper.createCase(TEST_CASE_NAME, net)

        Task mainTask = taskService.searchOne(QTask.task.transitionId.eq(MAIN_TRANSITION) & QTask.task.caseId.eq(testCase.stringId))
        assert mainTask

        dataService.setData(mainTask.stringId, ImportHelper.populateDataset([
                "boolean_0": [
                        "value": true,
                        "type" : "boolean"
                ]
        ]))

        testCase = workflowService.findOne(testCase.getStringId())
        assert testCase.dataSet[BOOLEAN_0_FIELD_ID].value == true

        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(MAIN_TRANSITION).find { it.toString() == FieldBehavior.EDITABLE.toString() }
        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(MAIN_TRANSITION).find { it.toString() == FieldBehavior.REQUIRED.toString() }

        dataService.setData(mainTask.stringId, ImportHelper.populateDataset([
                "boolean_0": [
                        "value": false,
                        "type" : "boolean"
                ]
        ]))

        testCase = workflowService.findOne(testCase.getStringId())
        assert testCase.dataSet[BOOLEAN_0_FIELD_ID].value == false

        assert testCase.dataSet[TEXT_0_FIELD_ID].behavior.get(MAIN_TRANSITION) == testCase.petriNet.transitions[MAIN_TRANSITION].dataSet[TEXT_0_FIELD_ID].behavior
    }
}
