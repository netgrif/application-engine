//file:noinspection GrMethodMayBeStatic
package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.DataGroup
import com.netgrif.application.engine.petrinet.domain.DataRef
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.ButtonField
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.dataoutcomes.SetDataEventOutcome
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.params.SetDataParams
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static org.junit.jupiter.api.Assertions.assertThrows

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class DataServiceTest {

    private static final String TASK_TITLE = "Transition"
    private static final String FILE_FIELD_TITLE = "File"
    private static final String TEXT_FIELD_TITLE = "Result"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IDataService dataService

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IWorkflowService workflowService

    private PetriNet agreementNet
    private PetriNet setDataNet
    private PetriNet net

    @BeforeEach
    void beforeAll() {
        testHelper.truncateDbs()
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(new ImportPetriNetParams(
                new FileInputStream("src/test/resources/data_service_referenced.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()))
        assert net.getNet() != null

        net = petriNetService.importPetriNet(new ImportPetriNetParams(
                new FileInputStream("src/test/resources/data_service_taskref.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()))
        assert net.getNet() != null
        this.net = net.getNet()

        ImportPetriNetEventOutcome agreementNet = petriNetService.importPetriNet(new ImportPetriNetParams(
                new FileInputStream("src/test/resources/agreement.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()))
        assert agreementNet.getNet() != null
        this.agreementNet = agreementNet.getNet()

        ImportPetriNetEventOutcome netoutcome = petriNetService.importPetriNet(new ImportPetriNetParams(
                new FileInputStream("src/test/resources/test_setData.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()))
        assert netoutcome.getNet() != null
        this.setDataNet = netoutcome.getNet()
    }

    @Test
    @Disabled
    void testTaskrefedFileFieldAction() {
        def aCase = importHelper.createCase("Case", this.net)
        assert aCase != null

        def taskId = importHelper.getTaskId(TASK_TITLE, aCase.stringId)
        assert taskId != null

        importHelper.assignTaskToSuper(TASK_TITLE, aCase.stringId)
        List<DataGroup> datagroups = dataService.getDataGroups(taskId, Locale.ENGLISH, superCreator.getLoggedSuper()).getData()

        assert datagroups.stream().filter({ it -> it.dataRefs.size() > 0 }).count() == 3
        DataRef fileField = findField(datagroups, FILE_FIELD_TITLE)
        MockMultipartFile file = new MockMultipartFile("data", "filename.txt", "text/plain", "hello world".getBytes())
        def changes = dataService.saveFile(taskId, fileField.fieldId, file)
        assert changes.changedFields.fields.size() == 1
        DataRef textField = findField(datagroups, TEXT_FIELD_TITLE)
        assert changes.changedFields.fields.containsKey(textField.fieldId)
        assert changes.changedFields.fields.get(textField.fieldId).rawValue == "OK"
    }

    DataRef findField(List<DataGroup> datagroups, String fieldTitle) {
        def fieldDataGroup = datagroups.find { it -> it.dataRefs.values().find({ DataRef field -> (field.field.name.defaultValue == fieldTitle) }) != null }
        assert fieldDataGroup != null
        DataRef field = fieldDataGroup.dataRefs.values().find({ DataRef field -> (field.field.name.defaultValue == fieldTitle) })
        assert field != null
        return field
    }

    @Test
    void testTaskRefOrderOnGridLayout() {
        def aCase = importHelper.createCase("Case", this.agreementNet)
        assert aCase != null

        def taskId = importHelper.getTaskId("summary A", aCase.stringId)
        assert taskId != null

        importHelper.assignTaskToSuper("summary A", aCase.stringId)

        List<DataGroup> dataGroups = dataService.getDataGroups(taskId, Locale.ENGLISH, superCreator.getLoggedSuper()).getData()
        assert dataGroups.get(1).getParentTaskRefId() == "taskRef_result"
        assert dataGroups.get(2).getParentTaskRefId() == "taskRef_1"
        assert dataGroups.get(3).getParentTaskRefId() == "taskRef_0"
    }

    @Test
    void testTransactionalSetDataOutcomes() {
        def aCase = importHelper.createCase("Case", this.setDataNet)
        ButtonField buttonWithValueOne = new ButtonField()
        buttonWithValueOne.setRawValue(1)

        assert aCase != null

        SetDataParams setDataParams = SetDataParams.with()
                .useCase(aCase)
                .user(superCreator.superUser)
                .isTransactional(true)
                .dataSet(new DataSet(Map.of(
                        "button_1", (Field<Integer>) buttonWithValueOne,
                        "button_0", (Field<Integer>) buttonWithValueOne
                )))
                .build()
        SetDataEventOutcome outcome = dataService.setData(setDataParams)
        assert outcome
        assert outcome.getOutcomes().size() == 2
    }

    @Test
    void testTransactionalSetDataFailure() {
        def aCase = importHelper.createCase("Case", this.setDataNet)
        ButtonField buttonWithValueOne = new ButtonField()
        buttonWithValueOne.setRawValue(1)

        assert aCase != null

        SetDataParams setDataParams = SetDataParams.with()
                .useCase(aCase)
                .user(superCreator.superUser)
                .isTransactional(true)
                .dataSet(new DataSet(Map.of(
                        "button_1", (Field<Integer>) buttonWithValueOne,
                        "non_existing_button", (Field<Integer>) buttonWithValueOne
                )))
                .build()
        assertThrows(IllegalArgumentException.class, { dataService.setData(setDataParams) })

        aCase = workflowService.findOne(aCase.stringId)
        assert aCase.getDataSet().get("button_1").getRawValue() != 1
    }

    @Test
    void testTransactionalSetDataSuccess() {
        def aCase = importHelper.createCase("Case", this.setDataNet)
        ButtonField buttonWithValueOne = new ButtonField()
        buttonWithValueOne.setRawValue(1)

        assert aCase != null

        SetDataParams setDataParams = SetDataParams.with()
                .useCase(aCase)
                .user(superCreator.superUser)
                .isTransactional(false)
                .dataSet(new DataSet(Map.of(
                        "button_1", (Field<Integer>) buttonWithValueOne,
                        "non_existing_button", (Field<Integer>) buttonWithValueOne
                )))
                .build()
        assertThrows(IllegalArgumentException.class, { dataService.setData(setDataParams) })

        aCase = workflowService.findOne(aCase.stringId)
        assert aCase.getDataSet().get("button_1").getRawValue() == 1
    }

//    @Test
//    void testSetDataAllowednets() {
//        def aCase = importHelper.createCase("test allowed nets", setDataNet)
//
//        def taskId = importHelper.getTaskId("", aCase.stringId)
//        def caze = dataService.setData(taskId, ImportHelper.populateDataset([
//                "caseRef_1": [
//                        "type"       : "caseRef",
//                        "allowedNets": [net.getStringId()]
//                ]
//        ] as Map)).getCase()
//
//        assert caze.getDataField("caseRef_1").getAllowedNets().size() == 1;
//        assert caze.getDataField("caseRef_1").getAllowedNets().get(0) == net.getStringId()
//    }
//
//    @Test
//    void testSetDataOptions() {
//        def aCase = importHelper.createCase("test set data options", setDataNet)
//
//        def taskId = importHelper.getTaskId("", aCase.stringId)
//        def caze = dataService.setData(taskId, ImportHelper.populateDataset([
//                "enumeration_map_0": [
//                        "type"   : "enumeration_map",
//                        "options": ["test1": "string1", "test2": new I18nString("string2")]
//                ],
//                "enumeration_0"    : [
//                        "type"   : "enumeration",
//                        "options": ["test1": "string1", "test2": new I18nString("string2")]
//                ]
//        ] as Map)).getCase()
//
//        assert caze.getDataField("enumeration_map_0").getOptions().size() == 2;
//        assert caze.getDataField("enumeration_map_0").getOptions().get("test1").defaultValue == "string1"
//        assert caze.getDataField("enumeration_map_0").getOptions().get("test2").defaultValue == "string2"
//
//        assert caze.getDataField("enumeration_0").getChoices().size() == 2;
//        assert caze.getDataField("enumeration_0").getChoices().toArray()[0].defaultValue == "string1"
//        assert caze.getDataField("enumeration_0").getChoices().toArray()[1].defaultValue == "string2"
//    }
//
//    @Test
//    void testSetDataChoices() {
//        def aCase = importHelper.createCase("test set data choices", setDataNet)
//
//        def taskId = importHelper.getTaskId("", aCase.stringId)
//        def caze = dataService.setData(taskId, ImportHelper.populateDataset([
//                "enumeration_0": [
//                        "type"   : "enumeration",
//                        "choices": ["string1", new I18nString("string2")]
//                ]
//        ] as Map)).getCase()
//
//        assert caze.getDataField("enumeration_0").getChoices().size() == 2;
//    }
//
//    @Test
//    void testSetDataProperties() {
//        def aCase = importHelper.createCase("test set data properties", setDataNet)
//
//        def taskId = importHelper.getTaskId("", aCase.stringId)
//        def caze = dataService.setData(taskId, ImportHelper.populateDataset([
//                "button_0": [
//                        "type"      : "button",
//                        "properties": ["stretch": "true"]
//                ],
//                "button_1": [
//                        "type"      : "button",
//                        "properties": ["stretch": "true"]
//                ]
//        ] as Map)).getCase()
//
//        assert caze.getDataField("button_0").getComponent().getName() == "raised";
//        assert caze.getDataField("button_0").getComponent().getProperties().isEmpty();
//        assert caze.getDataField("button_0").getDataRefComponents().get("t1") == null;
//
//        assert caze.getDataField("button_1").getComponent() == null;
//        assert caze.getDataField("button_1").getDataRefComponents().get("t1").getName() == "stroked";
//        assert caze.getDataField("button_1").getDataRefComponents().get("t1").getProperties().get("stretch") == "true";
//    }
//
//    @Test
//    void testChangeProperties() {
//        def aCase = importHelper.createCase("test change properties", setDataNet)
//
//        def caze = dataService.changeComponentProperties(aCase, 'button_0', new HashMap<String, String>() {
//            {
//                put("stretch", "true");
//            }
//        }).getCase();
//
//        assert caze.getDataField("button_0").getComponent().getName() == "raised";
//        assert caze.getDataField("button_0").getComponent().getProperties().get("stretch") == "true";
//        assert caze.getDataField("button_0").getDataRefComponents().get("t1") == null
//
//        caze = dataService.changeComponentProperties(aCase, 'button_1', new HashMap<String, String>() {
//            {
//                put("stretch", "true");
//            }
//        }).getCase();
//
//        assert caze.getDataField("button_1").getComponent().getName() == 'default';
//        assert caze.getDataField("button_1").getComponent().getProperties().get("stretch") == "true";
//        assert caze.getDataField("button_1").getDataRefComponents().get("t1").getName() == "stroked";
//        assert caze.getDataField("button_1").getDataRefComponents().get("t1").getProperties().isEmpty();
//
//        caze = dataService.changeComponentProperties(aCase, "t1", 'button_1', new HashMap<String, String>() {
//            {
//                put("stretch", "true");
//            }
//        }).getCase();
//
//        assert caze.getDataField("button_1").getDataRefComponents().get("t1").getName() == "stroked";
//        assert caze.getDataField("button_1").getDataRefComponents().get("t1").getProperties().get("stretch") == "true";
//    }
}
