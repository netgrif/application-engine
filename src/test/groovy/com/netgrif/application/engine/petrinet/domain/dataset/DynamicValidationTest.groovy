package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.DynamicValidation
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
@CompileStatic
class DynamicValidationTest extends EngineTest {

    @BeforeEach
    void before() {
        truncateDbs();
    }

    @Test
    @Disabled
    void testValidations() {
        ImportPetriNetEventOutcome optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/dynamic_validations.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        Case useCase = importHelper.createCase("test", optNet.getNet())
        Map<String, Field> data = getData(useCase)
        NumberField field = data["number"] as NumberField
        DynamicValidation validation = field.validations[0] as DynamicValidation
        assert validation instanceof DynamicValidation
        assert validation.compiledRule == ("inrange ${useCase.dataSet["min"].value as Integer},${useCase.dataSet["max"].value as Integer}" as String)
        assert validation.validationMessage.defaultValue == "Number field validation message"

        assert (data["text"]).validations[0] instanceof DynamicValidation
        assert (data["text"]).validations[0].compiledRule == ("maxLength ${useCase.dataSet["max"].value as Integer}" as String)

        assert (data["date"]).validations[0] instanceof DynamicValidation
        assert (data["date"]).validations[0].compiledRule == ("between past,today-P${useCase.dataSet["max"].value as Integer}D" as String)

        SetDataEventOutcome changes = setData(useCase, ["number_valid_switch": ["type": "boolean", "value": true],
                                                        "text_valid_switch"  : ["type": "boolean", "value": true]])
        assert (changes.changedFields["number"].attributes["validations"] as List)[0]["validationRule"] == "odd"
        assert (changes.changedFields["text"].attributes["validations"] as List)[0]["validationRule"] == "email"

        useCase = workflowService.findOne(useCase.stringId)
        assert useCase.dataSet["number"].validations[0].validationRule == "odd"
        assert useCase.dataSet["text"].validations[0].validationRule == "email"

        data = getData(useCase)
        assert !(validation instanceof DynamicValidation)
        assert validation.validationRule == "odd"

        assert !((data["text"]).validations[0] instanceof DynamicValidation)
        assert (data["text"]).validations[0].validationRule == "email"

        changes = setData(useCase, ["number_valid_switch": ["type": "boolean", "value": false],
                                    "text_valid_switch"  : ["type": "boolean", "value": false]])
        assert (changes.changedFields["number"].attributes["validations"] as List)[0]["validationRule"] == ("inrange ${useCase.dataSet["min"].value as Integer},${useCase.dataSet["max"].value as Integer}" as String)
        assert (changes.changedFields["text"].attributes["validations"] as List)[0]["validationRule"] == ("maxLength ${useCase.dataSet["max"].value as Integer}" as String)

        setData(useCase, ["min": ["type": "number", "value": "10"],
                          "max": ["type": "number", "value": "20"]])

        useCase = workflowService.findOne(useCase.stringId)
        data = getData(useCase)
        assert validation.compiledRule == ("inrange 10,20" as String)
        assert data["text"].validations[0].compiledRule == ("maxLength 20" as String)

        assert useCase.dataSet["number"].validations[0].validationRule == '''inrange ${min.value as Integer},${max.value as Integer}'''
        assert useCase.dataSet["text"].validations[0].validationRule == '''maxLength ${max.value as Integer}'''

        assert (useCase.dataSet["number"].validations[0] as DynamicValidation).expression.definition == '''"inrange ${min.value as Integer},${max.value as Integer}"'''
        assert (useCase.dataSet["text"].validations[0] as DynamicValidation).expression.definition == '''"maxLength ${max.value as Integer}"'''

    }

    Map<String, Field> getData(Case useCase) {
        Task task = task(useCase)
        return dataService.getData(task, useCase, superCreator.superUser)
                .getData()
                .collectEntries { [(it.fieldId): (it)] }
    }

    SetDataEventOutcome setData(Case useCase, Map<String, Map<String, Object>> values) {
        Task task = task(useCase)
        return dataService.setData(task, ImportHelper.populateDataset(values))
    }

    Task task(Case useCase) {
        return taskService.findOne(useCase.tasks.find { it.transitionId == "transition" }.taskId)
    }
}
