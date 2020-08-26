package com.netgrif.workflow.workflow

import com.netgrif.workflow.petrinet.domain.DataGroup
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldByFileFieldContainer
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedField
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
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

    @Before
    void beforeAll() {
        def net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/data_service_referenced.xml"), "major", superCreator.getLoggedSuper())
        assert net.isPresent()
        net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/data_service_taskref.xml"), "major", superCreator.getLoggedSuper())
        assert net.isPresent()
        this.net = net.get()
    }

    private PetriNet net

    // NAE-970
    @Test
    void testTaskrefedFileFieldAction() {
        def aCase = importHelper.createCase("Case", this.net)
        assert aCase != null

        def taskId = importHelper.getTaskId(TASK_TITLE, aCase.stringId)
        assert taskId != null

        importHelper.assignTaskToSuper(TASK_TITLE, aCase.stringId)

        List<DataGroup> datagroups = dataService.getDataGroups(taskId, Locale.ENGLISH)
        assert datagroups.stream().filter({it -> it.fields.size() > 0}).count() == 2
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
        def fieldDataGroup = datagroups.find {it -> it.fields.find( {field -> (field.name == fieldTitle) }) != null}
        assert fieldDataGroup != null
        LocalisedField field = fieldDataGroup.fields.find( { field -> (field.name == fieldTitle) }) as LocalisedField
        assert field != null
        return field
    }
}
