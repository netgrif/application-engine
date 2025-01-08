package com.netgrif.application.engine.validation

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.workflow.domain.VersionType
import com.netgrif.application.engine.workflow.domain.dataset.Field
import com.netgrif.application.engine.workflow.domain.dataset.MultichoiceMapField
import com.netgrif.application.engine.workflow.domain.dataset.TextField
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.ValidationRunner
import com.netgrif.application.engine.validations.interfaces.IValidationService
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class ValidationTestDynamic {

    public static final String VALIDATION_PETRI_NET_IDENTIFIER = "validation"
    public static final String VALIDATION_NAME_FIELD_ID = "name"
    public static final String VALIDATION_VALIDATION_TYPE_FIELD_ID = "validation_type"
    public static final String VALIDATION_DEFINITION_GROOVY_FIELD_ID = "validation_definition_groovy"
    public static final String VALIDATION_NUM_ARGUMENTS_GROOVY_FIELD_ID = "num_arguments_groovy"
    public static final String VALIDATION_DEFINITION_JAVASCRIPT_FIELD_ID = "validation_definition_javascript"
    public static final String VALIDATION_NUM_ARGUMENTS_JAVASCRIPT_FIELD_ID = "num_arguments_javascript"
    public static final String VALIDATION_INIT_TRANS_ID = "init"
    public static final String VALIDATION_DETAIL_TRANS_ID = "detail"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IValidationService validationService

    @Autowired
    protected ValidationRunner validationRunner

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
        validationService.clearValidations()
    }

    private Process importTextNet() {
        Process testNet = importHelper.createNet("validation/valid_text.xml", VersionType.MAJOR).get()
        assert testNet != null
        return testNet
    }

    private Case createValidation(String name, String validationDefinitionGroovy, Boolean active = true) {
        Process net = petriNetService.getNewestVersionByIdentifier(VALIDATION_PETRI_NET_IDENTIFIER)

        Case validationCase = importHelper.createCase("Validation ${name}", net)
        assert validationCase != null

        Task validationTask = importHelper.assignTaskToSuper("Init", validationCase.stringId).getTask()
        assert validationTask != null

        SetDataEventOutcome outcome = importHelper.setTaskData("Init", validationCase.stringId, new DataSet([
                (VALIDATION_NAME_FIELD_ID): new TextField(rawValue: name),
                (VALIDATION_VALIDATION_TYPE_FIELD_ID): new MultichoiceMapField(rawValue: ["server"]),
                (VALIDATION_DEFINITION_GROOVY_FIELD_ID): new TextField(rawValue: validationDefinitionGroovy)
        ] as Map<String, Field<?>>))
        assert outcome != null

        validationTask = importHelper.finishTaskAsSuper("Init", validationCase.stringId).getTask()
        assert validationTask != null

        if (active) {
            validationTask = importHelper.assignTaskToSuper("Activate", validationCase.stringId).getTask()
            assert validationTask != null

            validationTask = importHelper.finishTaskAsSuper("Activate", validationCase.stringId).getTask()
            assert validationTask != null
        }

        validationCase = workflowService.findOne(validationCase.stringId)
        assert validationCase.tasks.get("deactivate") != null

        return validationCase
    }

    @Test
    void textDynamic_validation() {
        createValidation("aaaa", "a -> field.rawValue.size() == a as Integer", true)

        Process testNet = importTextNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), new DataSet(["text06": new TextField(rawValue: "12345")]))
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void textDynamic_validation_fail() {
        createValidation("aaaa", "a -> field.rawValue.size() == a as Integer", true)

        Process testNet = importTextNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), new DataSet(["text06": new TextField(rawValue: "1234567")]))
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })

        assert "error-text06" == thrown.getMessage()
    }

    @Test
    void textDynamic_validation_conflictWithFieldName() {
        createValidation("number01", "a -> field.rawValue.size() == a as Integer", true)

        Process testNet = importTextNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        Assertions.assertThrows(MissingMethodException.class, () -> {
            importHelper.setTaskData(task.getStringId(), new DataSet(["text07": new TextField(rawValue: "1234567")]))
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
    }

    @Test
    void dynamicValidation_process_importActive() {
        createValidation("test1", "-> field.rawValue = 1", true)
        createValidation("test2", "-> field.rawValue = 2", true)
        createValidation("test3", "-> field.rawValue = 3", false)

        assert validationService.getValidation("test1") instanceof Closure<Boolean> && validationService.getValidation("test1") != null
        assert validationService.getValidation("test2") instanceof Closure<Boolean> && validationService.getValidation("test2") != null
        assert validationService.getValidation("test3") == null

        validationService.clearValidations()

        assert validationService.getValidation("test1") == null
        assert validationService.getValidation("test2") == null
        assert validationService.getValidation("test3") == null

        validationRunner.run()

        assert validationService.getValidation("test1") instanceof Closure<Boolean> && validationService.getValidation("test1") != null
        assert validationService.getValidation("test2") instanceof Closure<Boolean> && validationService.getValidation("test2") != null
        assert validationService.getValidation("test3") == null
    }

    @Test
    void dynamicValidation_process_behaviors() {
        Process net = petriNetService.getNewestVersionByIdentifier(VALIDATION_PETRI_NET_IDENTIFIER)

        Case validationCase = importHelper.createCase("Validation test", net)
        assert validationCase != null

        Task validationTask = importHelper.assignTaskToSuper("Init", validationCase.stringId).getTask()
        assert validationTask != null

        // TODO: release/8.0.0
//        assert validationCase.dataSet.get(VALIDATION_DEFINITION_GROOVY_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).hidden
//        assert validationCase.dataSet.get(VALIDATION_NUM_ARGUMENTS_GROOVY_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).hidden
//        assert validationCase.dataSet.get(VALIDATION_DEFINITION_GROOVY_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).hidden
//        assert validationCase.dataSet.get(VALIDATION_NUM_ARGUMENTS_GROOVY_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).hidden
//        assert validationCase.dataSet.get(VALIDATION_DEFINITION_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).hidden
//        assert validationCase.dataSet.get(VALIDATION_NUM_ARGUMENTS_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).hidden
//        assert validationCase.dataSet.get(VALIDATION_DEFINITION_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).hidden
//        assert validationCase.dataSet.get(VALIDATION_NUM_ARGUMENTS_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).hidden

        SetDataEventOutcome outcome = importHelper.setTaskData("Init", validationCase.stringId, new DataSet([
                (VALIDATION_VALIDATION_TYPE_FIELD_ID): new MultichoiceMapField(rawValue: ["client"]),
        ] as Map<String, Field<?>>))
        assert outcome != null
        assert outcome.case.dataSet.get(VALIDATION_DEFINITION_GROOVY_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).hidden
        assert outcome.case.dataSet.get(VALIDATION_NUM_ARGUMENTS_GROOVY_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).hidden
        assert outcome.case.dataSet.get(VALIDATION_DEFINITION_GROOVY_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).hidden
        assert outcome.case.dataSet.get(VALIDATION_NUM_ARGUMENTS_GROOVY_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).hidden
        assert outcome.case.dataSet.get(VALIDATION_DEFINITION_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).editable
        assert outcome.case.dataSet.get(VALIDATION_NUM_ARGUMENTS_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).editable
        assert outcome.case.dataSet.get(VALIDATION_DEFINITION_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).editable
        assert outcome.case.dataSet.get(VALIDATION_NUM_ARGUMENTS_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).editable

        outcome = importHelper.setTaskData("Init", validationCase.stringId, new DataSet([
                (VALIDATION_VALIDATION_TYPE_FIELD_ID): new MultichoiceMapField(rawValue: ["server"]),
        ] as Map<String, Field<?>>))
        assert outcome != null
        assert outcome.case.dataSet.get(VALIDATION_DEFINITION_GROOVY_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).editable
        assert outcome.case.dataSet.get(VALIDATION_NUM_ARGUMENTS_GROOVY_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).editable
        assert outcome.case.dataSet.get(VALIDATION_DEFINITION_GROOVY_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).editable
        assert outcome.case.dataSet.get(VALIDATION_NUM_ARGUMENTS_GROOVY_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).editable
        assert outcome.case.dataSet.get(VALIDATION_DEFINITION_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).hidden
        assert outcome.case.dataSet.get(VALIDATION_NUM_ARGUMENTS_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).hidden
        assert outcome.case.dataSet.get(VALIDATION_DEFINITION_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).hidden
        assert outcome.case.dataSet.get(VALIDATION_NUM_ARGUMENTS_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).hidden

        outcome = importHelper.setTaskData("Init", validationCase.stringId, new DataSet([
                (VALIDATION_VALIDATION_TYPE_FIELD_ID): new MultichoiceMapField(rawValue: ["server", "client"]),
        ] as Map<String, Field<?>>))
        assert outcome != null
        assert outcome.case.dataSet.get(VALIDATION_DEFINITION_GROOVY_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).editable
        assert outcome.case.dataSet.get(VALIDATION_NUM_ARGUMENTS_GROOVY_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).editable
        assert outcome.case.dataSet.get(VALIDATION_DEFINITION_GROOVY_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).editable
        assert outcome.case.dataSet.get(VALIDATION_NUM_ARGUMENTS_GROOVY_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).editable
        assert outcome.case.dataSet.get(VALIDATION_DEFINITION_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).editable
        assert outcome.case.dataSet.get(VALIDATION_NUM_ARGUMENTS_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_INIT_TRANS_ID).editable
        assert outcome.case.dataSet.get(VALIDATION_DEFINITION_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).editable
        assert outcome.case.dataSet.get(VALIDATION_NUM_ARGUMENTS_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).editable

        outcome = importHelper.setTaskData("Init", validationCase.stringId, new DataSet([
                (VALIDATION_NAME_FIELD_ID): new TextField(rawValue: "test"),
                (VALIDATION_VALIDATION_TYPE_FIELD_ID): new MultichoiceMapField(rawValue: ["server"]),
                (VALIDATION_DEFINITION_GROOVY_FIELD_ID): new TextField(rawValue: "-> field.rawValue == 1")
        ] as Map<String, Field<?>>))
        assert outcome != null

        validationTask = importHelper.finishTaskAsSuper("Init", validationCase.stringId).getTask()
        assert validationTask != null

        validationTask = importHelper.assignTaskToSuper("Activate", validationCase.stringId).getTask()
        assert validationTask != null

        validationTask = importHelper.finishTaskAsSuper("Activate", validationCase.stringId).getTask()
        assert validationTask != null

        validationCase = workflowService.findOne(validationCase.stringId)

        assert validationCase.dataSet.get(VALIDATION_DEFINITION_GROOVY_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).visible
        assert validationCase.dataSet.get(VALIDATION_NUM_ARGUMENTS_GROOVY_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).visible
        assert validationCase.dataSet.get(VALIDATION_DEFINITION_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).hidden
        assert validationCase.dataSet.get(VALIDATION_NUM_ARGUMENTS_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).hidden

        validationTask = importHelper.assignTaskToSuper("Deactivate", validationCase.stringId).getTask()
        assert validationTask != null

        validationTask = importHelper.finishTaskAsSuper("Deactivate", validationCase.stringId).getTask()
        assert validationTask != null

        validationCase = workflowService.findOne(validationCase.stringId)

        assert validationCase.dataSet.get(VALIDATION_DEFINITION_GROOVY_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).editable
        assert validationCase.dataSet.get(VALIDATION_NUM_ARGUMENTS_GROOVY_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).editable
        assert validationCase.dataSet.get(VALIDATION_DEFINITION_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).hidden
        assert validationCase.dataSet.get(VALIDATION_NUM_ARGUMENTS_JAVASCRIPT_FIELD_ID).behaviors.get(VALIDATION_DETAIL_TRANS_ID).hidden
    }

    @Test
    void dynamicValidation_process_create() {
        Process net = petriNetService.getNewestVersionByIdentifier(VALIDATION_PETRI_NET_IDENTIFIER)

        Case validationCase = importHelper.createCase("Validation test", net)
        assert validationCase != null

        Task validationTask = importHelper.assignTaskToSuper("Init", validationCase.stringId).getTask()
        assert validationTask != null

        SetDataEventOutcome outcome = importHelper.setTaskData("Init", validationCase.stringId, new DataSet([
                (VALIDATION_NAME_FIELD_ID): new TextField(rawValue: "test"),
                (VALIDATION_VALIDATION_TYPE_FIELD_ID): new MultichoiceMapField(rawValue: ["server"]),
                (VALIDATION_DEFINITION_GROOVY_FIELD_ID): new TextField(rawValue: null)
        ] as Map<String, Field<?>>))
        assert outcome != null

        validationTask = importHelper.finishTaskAsSuper("Init", validationCase.stringId).getTask()
        assert validationTask != null

        validationTask = importHelper.assignTaskToSuper("Activate", validationCase.stringId).getTask()
        assert validationTask != null

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            validationTask = importHelper.finishTaskAsSuper("Activate", validationCase.stringId).getTask()
            assert validationTask != null
        })

        outcome = importHelper.setTaskData("Detail", validationCase.stringId, new DataSet([
                (VALIDATION_DEFINITION_GROOVY_FIELD_ID): new TextField(rawValue: "-> field.rawValue == 1")
        ] as Map<String, Field<?>>))
        assert outcome != null

        validationTask = importHelper.finishTaskAsSuper("Activate", validationCase.stringId).getTask()
        assert validationTask != null
    }
}
