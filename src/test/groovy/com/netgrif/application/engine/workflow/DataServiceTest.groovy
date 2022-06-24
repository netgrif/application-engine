package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.DataGroup
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
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
    private SuperCreator superCreator

    @Autowired
    private IDataService dataService

    @Autowired
    private TestHelper testHelper

    private PetriNet agreementNet

    @BeforeEach
    void beforeAll() {
        testHelper.truncateDbs()

        def net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/data_service_referenced.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null
        net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/data_service_taskref.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null
        this.net = net.getNet()
        def agreementNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/agreement.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert agreementNet.getNet() != null
        this.agreementNet = agreementNet.getNet()
    }

    private PetriNet net

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

        def changes = dataService.saveFile(taskId, fileField.stringId, file)
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
}
