package com.netgrif.application.engine.validation

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
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

    private PetriNet importTextNet() {
        PetriNet testNet = importHelper.createNet("validation/valid_text.xml", VersionType.MAJOR).get()
        assert testNet != null
        return testNet
    }

    private Case createValidation(String name, String validationDefinitionGroovy, Boolean active = true) {
        PetriNet net = petriNetService.getNewestVersionByIdentifier(VALIDATION_PETRI_NET_IDENTIFIER)

        Case validationCase = importHelper.createCase("Validation ${name}", net)
        assert validationCase != null

        Task validationTask = importHelper.assignTaskToSuper("Init", validationCase.stringId).getTask()
        assert validationTask != null

        SetDataEventOutcome outcome = importHelper.setTaskData("Init", validationCase.stringId, new DataSet([
                "name": new TextField(rawValue: name),
                "validation_definition_groovy": new TextField(rawValue: validationDefinitionGroovy)
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

        createValidation("aaaa", "a -> thisField.rawValue.size() == a as Integer", true)

        PetriNet testNet = importTextNet()
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

        createValidation("aaaa", "a -> thisField.rawValue.size() == a as Integer", true)

        PetriNet testNet = importTextNet()
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

        createValidation("number01", "a -> thisField.rawValue.size() == a as Integer", true)

        PetriNet testNet = importTextNet()
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
    void dynamicValidation_importActive() {
        createValidation("test1", "-> thisField.rawValue = 1", true)
        createValidation("test2", "-> thisField.rawValue = 2", true)
        createValidation("test3", "-> thisField.rawValue = 3", false)

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
}
