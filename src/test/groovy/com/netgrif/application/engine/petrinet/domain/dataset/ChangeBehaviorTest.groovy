//file:noinspection GroovyPointlessBoolean
package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.params.SetDataParams
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

import static com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior.EDITABLE
import static com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior.VISIBLE

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
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

    @Autowired
    private SuperCreator superCreator

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

        dataService.setData(new SetDataParams(mainTask.stringId, new DataSet([
                "boolean_0": new BooleanField(rawValue: true)
        ] as Map<String, Field<?>>), superCreator.getSuperUser()))

        testCase = workflowService.findOne(testCase.getStringId())
        assert testCase.dataSet.get(BOOLEAN_0_FIELD_ID).rawValue == true

        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(MAIN_TRANSITION).behavior == EDITABLE
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(MAIN_TRANSITION).required == true
        assert testCase.dataSet.get(TEXT_1_FIELD_ID).behaviors.get(MAIN_TRANSITION).required == false
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

        dataService.setData(new SetDataParams(mainTask.stringId, new DataSet([
                "boolean_1": new BooleanField(rawValue: true)
        ] as Map<String, Field<?>>), superCreator.getSuperUser()))

        testCase = workflowService.findOne(testCase.getStringId())
        assert testCase.dataSet.get(BOOLEAN_1_FIELD_ID).rawValue == true

        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(MAIN_TRANSITION).behavior == EDITABLE
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(TEST_TRANSITION_1).behavior == EDITABLE
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(TEST_TRANSITION_2).behavior == EDITABLE
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(TEST_TRANSITION_3).behavior == EDITABLE
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(MAIN_TRANSITION).required
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(TEST_TRANSITION_1).required
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(TEST_TRANSITION_2).required
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(TEST_TRANSITION_3).required
    }

    @Test
    void changeBehaviorOfSingleFieldOnMultipleTransitions() {
        Case testCase = helper.createCase(TEST_CASE_NAME, net)

        Task mainTask = taskService.searchOne(QTask.task.transitionId.eq(MAIN_TRANSITION) & QTask.task.caseId.eq(testCase.stringId))
        assert mainTask
        Task otherTask = taskService.searchOne(QTask.task.transitionId.eq(TEST_TRANSITION_1) & QTask.task.caseId.eq(testCase.stringId))
        assert otherTask

        dataService.setData(new SetDataParams(mainTask.stringId, new DataSet([
                "boolean_2": new BooleanField(rawValue: true)
        ] as Map<String, Field<?>>), superCreator.getSuperUser()))

        testCase = workflowService.findOne(testCase.getStringId())
        assert testCase.dataSet.get(BOOLEAN_2_FIELD_ID).rawValue == true

        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(MAIN_TRANSITION).behavior == EDITABLE
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(TEST_TRANSITION_1).behavior == EDITABLE
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(TEST_TRANSITION_2).behavior == VISIBLE
    }

    @Test
    void changeBehaviorOfMultipleFieldsOnMultipleTransitions() {
        Case testCase = helper.createCase(TEST_CASE_NAME, net)

        Task mainTask = taskService.searchOne(QTask.task.transitionId.eq(MAIN_TRANSITION) & QTask.task.caseId.eq(testCase.stringId))
        assert mainTask
        Task otherTask = taskService.searchOne(QTask.task.transitionId.eq(TEST_TRANSITION_1) & QTask.task.caseId.eq(testCase.stringId))
        assert otherTask

        dataService.setData(new SetDataParams(mainTask.stringId, new DataSet([
                "boolean_3": new BooleanField(rawValue: true)
        ] as Map<String, Field<?>>), superCreator.getSuperUser()))

        testCase = workflowService.findOne(testCase.getStringId())
        assert testCase.dataSet.get(BOOLEAN_3_FIELD_ID).rawValue == true

        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(MAIN_TRANSITION).behavior == EDITABLE
        assert testCase.dataSet.get(TEXT_1_FIELD_ID).behaviors.get(MAIN_TRANSITION).behavior == EDITABLE
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(TEST_TRANSITION_1).behavior == EDITABLE
        assert testCase.dataSet.get(TEXT_1_FIELD_ID).behaviors.get(TEST_TRANSITION_1).behavior == EDITABLE
        assert testCase.dataSet.get(TEXT_1_FIELD_ID).behaviors.get(TEST_TRANSITION_2).behavior == VISIBLE
    }

    @Test
    void initialBehaviorTest() {
        Case testCase = helper.createCase(TEST_CASE_NAME, net)

        Task mainTask = taskService.searchOne(QTask.task.transitionId.eq(MAIN_TRANSITION) & QTask.task.caseId.eq(testCase.stringId))
        assert mainTask

        dataService.setData(new SetDataParams(mainTask.stringId, new DataSet([
                "boolean_0": new BooleanField(rawValue: true)
        ] as Map<String, Field<?>>), superCreator.getSuperUser()))

        testCase = workflowService.findOne(testCase.getStringId())
        assert testCase.dataSet.get(BOOLEAN_0_FIELD_ID).rawValue == true
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(MAIN_TRANSITION).behavior == EDITABLE
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(MAIN_TRANSITION).required == true

        dataService.setData(new SetDataParams(mainTask.stringId, new DataSet([
                "boolean_0": new BooleanField(rawValue: false)
        ] as Map<String, Field<?>>), superCreator.getSuperUser()))

        testCase = workflowService.findOne(testCase.getStringId())
        assert testCase.dataSet.get(BOOLEAN_0_FIELD_ID).rawValue == false
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(MAIN_TRANSITION).behavior == VISIBLE
        assert testCase.dataSet.get(TEXT_0_FIELD_ID).behaviors.get(MAIN_TRANSITION).required == false
    }
}
