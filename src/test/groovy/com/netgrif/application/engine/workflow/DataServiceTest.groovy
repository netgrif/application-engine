package com.netgrif.application.engine.workflow

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.DataGroup
import com.netgrif.core.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.logic.ChangedFieldByFileFieldContainer
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.LocalisedField
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.lang.reflect.Method

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class DataServiceTest {

    private static final String TASK_TITLE = "Transition";
    private static final String FILE_FIELD_TITLE = "File";
    private static final String TEXT_FIELD_TITLE = "Result";

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreatorRunner superCreator

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
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/data_service_referenced.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null

        net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/data_service_taskref.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null
        this.net = net.getNet()

        ImportPetriNetEventOutcome agreementNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/agreement.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert agreementNet.getNet() != null
        this.agreementNet = agreementNet.getNet()

        ImportPetriNetEventOutcome netoutcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/test_setData.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        assert netoutcome.getNet() != null;
        this.setDataNet = netoutcome.getNet();
    }

    @Test
    @Disabled
    void testTaskrefedFileFieldAction() {
        def aCase = importHelper.createCase("Case", this.net)
        assert aCase != null

        def taskId = importHelper.getTaskId(TASK_TITLE, aCase.stringId)
        assert taskId != null

        importHelper.assignTaskToSuper(TASK_TITLE, aCase.stringId)

        List<DataGroup> datagroups = dataService.getDataGroups(taskId, Locale.ENGLISH).getData()
        assert datagroups.stream().filter({ it -> it.fields.size() > 0 }).count() == 3
        LocalisedField fileField = findField(datagroups, FILE_FIELD_TITLE)

        MockMultipartFile file = new MockMultipartFile("data", "filename.txt", "text/plain", "hello world".getBytes())

        ChangedFieldByFileFieldContainer changes = dataService.saveFile(taskId, fileField.stringId, file)
        assert changes.changedFields.size() == 1
        LocalisedField textField = findField(datagroups, TEXT_FIELD_TITLE)
        assert changes.changedFields.containsKey(textField.stringId)
        assert changes.changedFields.get(textField.stringId).containsKey("value")
        assert changes.changedFields.get(textField.stringId).get("value") == "OK"
    }

    LocalisedField findField(List<DataGroup> datagroups, String fieldTitle) {
        def fieldDataGroup = datagroups.find { it -> it.fields.find({ field -> (field.name == fieldTitle) }) != null }
        assert fieldDataGroup != null
        LocalisedField field = fieldDataGroup.fields.find({ field -> (field.name == fieldTitle) }) as LocalisedField
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
        List<DataGroup> datagroups = dataService.getDataGroups(taskId, Locale.ENGLISH).getData()
        assert datagroups.get(1).getParentTaskRefId() == "taskRef_result"
        assert datagroups.get(2).getParentTaskRefId() == "taskRef_1"
        assert datagroups.get(3).getParentTaskRefId() == "taskRef_0"
    }

    @Test
    void testParseI18nStringValues() {
        ObjectMapper mapper = new ObjectMapper()
        ObjectNode i18nTranslations = mapper.createObjectNode()
        i18nTranslations.put("sk", "SK: This is default value")
        i18nTranslations.put("de", "DE: This is default value")

        ObjectNode i18nValue = mapper.createObjectNode()
        i18nValue.put("defaultValue", "This is default value")
        i18nValue.set("translations", i18nTranslations)

        Method method = dataService.getClass().getDeclaredMethod("parseI18nStringValues", new Class[]{ObjectNode.class})
        method.setAccessible(true)
        I18nString parsedValue = method.invoke(dataService, new Object[]{i18nValue}) as I18nString

        assert parsedValue.defaultValue == "This is default value"
        assert parsedValue.translations.size() == 2
        assert parsedValue.translations.containsKey("sk")
        assert parsedValue.translations.containsKey("de")
        assert parsedValue.translations["sk"] == "SK: This is default value"
        assert parsedValue.translations["de"] == "DE: This is default value"
    }

    @Test
    void testSetDataAllowednets() {
        def aCase = importHelper.createCase("test allowed nets", setDataNet)

        def taskId = importHelper.getTaskId("", aCase.stringId)
        def caze = dataService.setData(taskId, ImportHelper.populateDataset([
                "caseRef_1": [
                        "type": "caseRef",
                        "allowedNets" : [net.getStringId()]
                ]
        ] as Map)).getCase()

        assert caze.getDataField("caseRef_1").getAllowedNets().size() == 1;
        assert caze.getDataField("caseRef_1").getAllowedNets().get(0) == net.getStringId()
    }

    @Test
    void testSetDataOptions() {
        def aCase = importHelper.createCase("test set data options", setDataNet)

        def taskId = importHelper.getTaskId("", aCase.stringId)
        def caze = dataService.setData(taskId, ImportHelper.populateDataset([
                "enumeration_map_0": [
                        "type": "enumeration_map",
                        "options" : ["test1": "string1", "test2": new I18nString("string2")]
                ],
                "enumeration_0": [
                        "type": "enumeration",
                        "options" : ["test1": "string1", "test2": new I18nString("string2")]
                ]
        ] as Map)).getCase()

        assert caze.getDataField("enumeration_map_0").getOptions().size() == 2;
        assert caze.getDataField("enumeration_map_0").getOptions().get("test1").defaultValue == "string1"
        assert caze.getDataField("enumeration_map_0").getOptions().get("test2").defaultValue == "string2"

        assert caze.getDataField("enumeration_0").getChoices().size() == 2;
        assert caze.getDataField("enumeration_0").getChoices().toArray()[0].defaultValue == "string1"
        assert caze.getDataField("enumeration_0").getChoices().toArray()[1].defaultValue == "string2"
    }

    @Test
    void testSetDataChoices() {
        def aCase = importHelper.createCase("test set data choices", setDataNet)

        def taskId = importHelper.getTaskId("", aCase.stringId)
        def caze = dataService.setData(taskId, ImportHelper.populateDataset([
                "enumeration_0": [
                        "type": "enumeration",
                        "choices" : ["string1", new I18nString("string2")]
                ]
        ] as Map)).getCase()

        assert caze.getDataField("enumeration_0").getChoices().size() == 2;
    }

    @Test
    void testSetDataProperties() {
        def aCase = importHelper.createCase("test set data properties", setDataNet)

        def taskId = importHelper.getTaskId("", aCase.stringId)
        def caze = dataService.setData(taskId, ImportHelper.populateDataset([
                "button_0": [
                        "type": "button",
                        "properties" : ["stretch": "true"]
                ],
                "button_1": [
                        "type": "button",
                        "properties" : ["stretch": "true"]
                ]
        ] as Map)).getCase()

        assert caze.getDataField("button_0").getComponent().getName() == "raised";
        assert caze.getDataField("button_0").getComponent().getProperties().isEmpty();
        assert caze.getDataField("button_0").getDataRefComponents().get("t1") == null;

        assert caze.getDataField("button_1").getComponent() == null;
        assert caze.getDataField("button_1").getDataRefComponents().get("t1").getName() == "stroked";
        assert caze.getDataField("button_1").getDataRefComponents().get("t1").getProperties().get("stretch") == "true";
    }

    @Test
    void testChangeProperties() {
        def aCase = importHelper.createCase("test change properties", setDataNet)

        def caze = dataService.changeComponentProperties(aCase, 'button_0', new HashMap<String, String>(){{
            put("stretch", "true");}}).getCase();

        assert caze.getDataField("button_0").getComponent().getName() == "raised";
        assert caze.getDataField("button_0").getComponent().getProperties().get("stretch") == "true";
        assert caze.getDataField("button_0").getDataRefComponents().get("t1") == null

        caze = dataService.changeComponentProperties(aCase, 'button_1', new HashMap<String, String>(){{
            put("stretch", "true");}}).getCase();

        assert caze.getDataField("button_1").getComponent().getName() == 'default';
        assert caze.getDataField("button_1").getComponent().getProperties().get("stretch") == "true";
        assert caze.getDataField("button_1").getDataRefComponents().get("t1").getName() == "stroked";
        assert caze.getDataField("button_1").getDataRefComponents().get("t1").getProperties().isEmpty();

        caze = dataService.changeComponentProperties(aCase, "t1",'button_1', new HashMap<String, String>(){{
            put("stretch", "true");}}).getCase();

        assert caze.getDataField("button_1").getDataRefComponents().get("t1").getName() == "stroked";
        assert caze.getDataField("button_1").getDataRefComponents().get("t1").getProperties().get("stretch") == "true";
    }
}
