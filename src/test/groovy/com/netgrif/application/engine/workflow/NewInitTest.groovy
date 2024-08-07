package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue
import com.netgrif.application.engine.petrinet.domain.dataset.FileListFieldValue
import com.netgrif.application.engine.petrinet.domain.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.transform.CompileStatic
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
@CompileStatic
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
        petriNetService.importPetriNet(new ImportPetriNetParams(
                new FileInputStream("src/test/resources/petriNets/nae_1276_Init_value_as_choice.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()))
        CreateCaseParams createCaseParams = CreateCaseParams.builder()
                .petriNet(petriNetService.getNewestVersionByIdentifier("new_init_test"))
                .title("New init test")
                .color("")
                .loggedUser(superCreator.getLoggedSuper())
                .build()
        Case initTestCase = workflowService.createCase(createCaseParams).getCase()
//        TODO: release/8.0.0
//        assert (initTestCase.dataSet["new_init_multichoice"].value as List<I18nString>).stream().any { ((I18nString) it).defaultValue == "Bob" }
//        assert (initTestCase.dataSet["new_init_multichoice"].value as List<I18nString>).stream().any { ((I18nString) it).defaultValue == "Alice" }
//        assert (initTestCase.dataSet["old_init_multichoice"].value as List<I18nString>).stream().any { ((I18nString) it).defaultValue == "Bob" }
//        assert (initTestCase.dataSet["old_init_multichoice"].value as List<I18nString>).stream().any { ((I18nString) it).defaultValue == "Alice" }
//        assert (initTestCase.dataSet["new_init_multichoice_map"].value as List<String>).stream().any { it == "al" }
//        assert (initTestCase.dataSet["new_init_multichoice_map"].value as List<String>).stream().any { it == "ca" }
//        assert (initTestCase.dataSet["old_init_multichoice_map"].value as List<String>).stream().any { it == "al" }
//        assert (initTestCase.dataSet["old_init_multichoice_map"].value as List<String>).stream().any { it == "ca" }
//        assert !(initTestCase.dataSet["new_init_taskref"].value as List).empty
//        assert !(initTestCase.dataSet["old_init_taskref"].value as List).empty
//        assert (initTestCase.dataSet["new_init_fileList"].value as FileListFieldValue).namesPaths.stream().any { ((FileFieldValue) it).getName() == "test-file.txt" }
//        assert (initTestCase.dataSet["new_init_fileList"].value as FileListFieldValue).namesPaths.stream().any { ((FileFieldValue) it).getName() == "test-file-list.txt" }
//        assert (initTestCase.dataSet["old_init_fileList"].value as FileListFieldValue).namesPaths.stream().any { ((FileFieldValue) it).getName() == "test-file.txt" }
//        assert (initTestCase.dataSet["old_init_fileList"].value as FileListFieldValue).namesPaths.stream().any { ((FileFieldValue) it).getName() == "test-file-list.txt" }
//        assert (initTestCase.dataSet["init_i18n"].value as I18nString).defaultValue == "Default i18n test value"
//        assert (initTestCase.dataSet["init_i18n"].value as I18nString).translations.containsKey("sk")
//        assert (initTestCase.dataSet["init_i18n"].value as I18nString).translations.containsKey("de")
//        assert (initTestCase.dataSet["init_i18n"].value as I18nString).translations.get("sk") == "Default SK i18n test value"
//        assert (initTestCase.dataSet["init_i18n"].value as I18nString).translations.get("de") == "Default DE i18n test value"
    }
}
