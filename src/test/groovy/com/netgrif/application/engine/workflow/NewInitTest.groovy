package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.FileListFieldValue
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class NewInitTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private SuperCreator superCreator

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void newInitTest() throws IOException, MissingIconKeyException, MissingPetriNetMetaDataException {
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/nae_1276_Init_value_as_choice.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        Case initTestCase = workflowService.createCase(petriNetService.getNewestVersionByIdentifier("new_init_test").stringId, "New init test", "", superCreator.getLoggedSuper()).getCase()
        assert (initTestCase.dataSet["new_init_multichoice"].value as List<I18nString>).stream().any { it.defaultValue == "Bob" }
        assert (initTestCase.dataSet["new_init_multichoice"].value as List<I18nString>).stream().any { it.defaultValue == "Alice" }
        assert (initTestCase.dataSet["old_init_multichoice"].value as List<I18nString>).stream().any { it.defaultValue == "Bob" }
        assert (initTestCase.dataSet["old_init_multichoice"].value as List<I18nString>).stream().any { it.defaultValue == "Alice" }
        assert (initTestCase.dataSet["new_init_multichoice_map"].value as List<String>).stream().any { it == "al" }
        assert (initTestCase.dataSet["new_init_multichoice_map"].value as List<String>).stream().any { it == "ca" }
        assert (initTestCase.dataSet["old_init_multichoice_map"].value as List<String>).stream().any { it == "al" }
        assert (initTestCase.dataSet["old_init_multichoice_map"].value as List<String>).stream().any { it == "ca" }
        assert !(initTestCase.dataSet["new_init_taskref"].value as List).empty
        assert !(initTestCase.dataSet["old_init_taskref"].value as List).empty
        assert (initTestCase.dataSet["new_init_fileList"].value as FileListFieldValue).namesPaths.stream().any { it.getName() == "test-file.txt" }
        assert (initTestCase.dataSet["new_init_fileList"].value as FileListFieldValue).namesPaths.stream().any { it.getName() == "test-file-list.txt" }
        assert (initTestCase.dataSet["old_init_fileList"].value as FileListFieldValue).namesPaths.stream().any { it.getName() == "test-file.txt" }
        assert (initTestCase.dataSet["old_init_fileList"].value as FileListFieldValue).namesPaths.stream().any { it.getName() == "test-file-list.txt" }
        assert (initTestCase.dataSet["init_i18n"].value as I18nString).defaultValue == "Default i18n test value"
        assert (initTestCase.dataSet["init_i18n"].value as I18nString).translations.containsKey("sk")
        assert (initTestCase.dataSet["init_i18n"].value as I18nString).translations.containsKey("de")
        assert (initTestCase.dataSet["init_i18n"].value as I18nString).translations.get("sk") == "Default SK i18n test value"
        assert (initTestCase.dataSet["init_i18n"].value as I18nString).translations.get("de") == "Default DE i18n test value"
    }
}
