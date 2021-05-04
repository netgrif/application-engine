package com.netgrif.workflow.workflow

import com.netgrif.workflow.importer.service.throwable.MissingIconKeyException
import com.netgrif.workflow.petrinet.domain.I18nString
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.dataset.FileListFieldValue
import com.netgrif.workflow.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class NewInitTest {

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private SuperCreator superCreator

    @Test
    void newInitTest() throws IOException, MissingIconKeyException, MissingPetriNetMetaDataException {
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/nae_1276_Init_value_as_choice.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        Case initTestCase = workflowService.createCase(petriNetService.getNewestVersionByIdentifier("new_init_test").stringId,"New init test", "", superCreator.getLoggedSuper())
        assert (initTestCase.dataSet["new_init_multichoice"].value as List<I18nString>).stream().any {it.defaultValue == "Bob"}
        assert (initTestCase.dataSet["new_init_multichoice"].value as List<I18nString>).stream().any {it.defaultValue == "Alice"}
        assert (initTestCase.dataSet["old_init_multichoice"].value as List<I18nString>).stream().any {it.defaultValue == "Bob"}
        assert (initTestCase.dataSet["old_init_multichoice"].value as List<I18nString>).stream().any {it.defaultValue == "Alice"}
        assert (initTestCase.dataSet["new_init_multichoice_map"].value as List<String>).stream().any {it == "al"}
        assert (initTestCase.dataSet["new_init_multichoice_map"].value as List<String>).stream().any {it == "ca"}
        assert (initTestCase.dataSet["old_init_multichoice_map"].value as List<String>).stream().any {it == "al"}
        assert (initTestCase.dataSet["old_init_multichoice_map"].value as List<String>).stream().any {it == "ca"}
        assert !(initTestCase.dataSet["new_init_taskref"].value as List).empty
        assert !(initTestCase.dataSet["old_init_taskref"].value as List).empty
        assert (initTestCase.dataSet["new_init_fileList"].value as FileListFieldValue).namesPaths.stream().any {it.getName() == "test-file.txt"}
        assert (initTestCase.dataSet["new_init_fileList"].value as FileListFieldValue).namesPaths.stream().any {it.getName() == "test-file-list.txt"}
        assert (initTestCase.dataSet["old_init_fileList"].value as FileListFieldValue).namesPaths.stream().any {it.getName() == "test-file.txt"}
        assert (initTestCase.dataSet["old_init_fileList"].value as FileListFieldValue).namesPaths.stream().any {it.getName() == "test-file-list.txt"}
    }
}
