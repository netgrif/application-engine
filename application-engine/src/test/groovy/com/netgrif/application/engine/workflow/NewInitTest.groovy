package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException
import com.netgrif.application.engine.objects.petrinet.domain.I18nString
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileListFieldValue
import com.netgrif.application.engine.objects.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.workflow.params.CreateCaseParams
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@TestPropertySource(properties = "netgrif.engine.storage.minio.enabled=true")
@SpringBootTest
class NewInitTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private SuperCreatorRunner superCreator

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void newInitTest() throws IOException, MissingIconKeyException, MissingPetriNetMetaDataException {
        petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/nae_1276_Init_value_as_choice.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreator.getLoggedSuper())
                .build())
        Case initTestCase = workflowService.createCase(CreateCaseParams.with()
                .process(petriNetService.getDefaultVersionByIdentifier("new_init_test"))
                .title("New init test")
                .color("")
                .author(superCreator.loggedSuper)
                .build()).getCase()
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
//        TODO not testing if file was really uploaded
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
