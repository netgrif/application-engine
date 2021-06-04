package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.DynamicValidation
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetOutcome
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@ActiveProfiles(["test"])
@RunWith(SpringRunner.class)
class DynamicValidationTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IDataService dataService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IWorkflowService workflowService

    @Before
    void before() {
        testHelper.truncateDbs();
    }

    @Test
    void testValidations() {
        ImportPetriNetOutcome optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/dynamic_validations.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        Case useCase = importHelper.createCase("test", optNet.getNet())
        Map<String, Field> data = getData(useCase)
        assert (data["number"]).validations[0] instanceof DynamicValidation
        assert (data["number"]).validations[0].compiledRule == ("inrange ${useCase.dataSet["min"].value as Integer},${useCase.dataSet["max"].value as Integer}" as String)
        assert (data["number"]).validations[0].validationMessage.defaultValue == "Number field validation message"

        assert (data["text"]).validations[0] instanceof DynamicValidation
        assert (data["text"]).validations[0].compiledRule == ("maxLength ${useCase.dataSet["max"].value as Integer}" as String)

        assert (data["date"]).validations[0] instanceof DynamicValidation
        assert (data["date"]).validations[0].compiledRule == ("between past,today-P${useCase.dataSet["max"].value as Integer}D" as String)

        ChangedFieldsTree changes = setData(useCase, ["number_valid_switch": ["type": "boolean", "value": true],
                                                      "text_valid_switch"  : ["type": "boolean", "value": true]])
        assert (changes.changedFields["number"].attributes["validations"] as List)[0]["validationRule"] == "odd"
        assert (changes.changedFields["text"].attributes["validations"] as List)[0]["validationRule"] == "email"

        useCase = workflowService.findOne(useCase.stringId)
        assert useCase.dataSet["number"].validations[0].validationRule == "odd"
        assert useCase.dataSet["text"].validations[0].validationRule == "email"

        data = getData(useCase)
        assert !((data["number"]).validations[0] instanceof DynamicValidation)
        assert (data["number"]).validations[0].validationRule == "odd"

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
        assert data["number"].validations[0].compiledRule == ("inrange 10,20" as String)
        assert data["text"].validations[0].compiledRule == ("maxLength 20" as String)

        assert useCase.dataSet["number"].validations[0].validationRule == '''inrange ${min.value as Integer},${max.value as Integer}'''
        assert useCase.dataSet["text"].validations[0].validationRule == '''maxLength ${max.value as Integer}'''

        assert (useCase.dataSet["number"].validations[0] as DynamicValidation).expression.definition == '''"inrange ${min.value as Integer},${max.value as Integer}"'''
        assert (useCase.dataSet["text"].validations[0] as DynamicValidation).expression.definition == '''"maxLength ${max.value as Integer}"'''

    }

    Map<String, Field> getData(Case useCase) {
        Task task = task(useCase)
        return dataService.getData(task, useCase).getData().collectEntries { [(it.importId): (it)] }
    }

    ChangedFieldsTree setData(Case useCase, Map<String, Map<String, Object>> values) {
        Task task = task(useCase)
        return dataService.setData(task, ImportHelper.populateDataset(values)).getData()
    }

    Task task(Case useCase) {
        return taskService.findOne(useCase.tasks.find { it.transition == "transition" }.task)
    }
}
