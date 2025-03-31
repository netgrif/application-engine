package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.files.StorageResolverService
import com.netgrif.application.engine.files.interfaces.IStorageService
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.dataset.StorageField
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior
import com.netgrif.application.engine.petrinet.service.PetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.interfaces.ICaseImportExportService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter

@Slf4j
@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class CaseImportExportTest {

    private final String testNetFileName = "nae_1843.xml"
    private final String testNetIdentifier = "data/export_data"
    private final String outputFileLocation = "src" + File.separator + "test" + File.separator + "resources" + File.separator
    private final String outputFileName = "case_export_test.zip"
    private final String outputPath = outputFileLocation + outputFileName

    @Autowired
    private SuperCreatorRunner superCreator

    @Autowired
    private PetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private ICaseImportExportService caseImportExportService

    @Autowired
    private StorageResolverService resolverService

    @Autowired
    private IUserService userService

    @Autowired
    private ITaskService taskService

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        importHelper.createNet(testNetFileName).get()
    }

    @Test
    void exportCase() {
        Case toExport = createCaseAndSetData()
        importHelper.assignTaskToSuper("Start task", toExport.stringId)
        toExport = importHelper.finishTaskAsSuper("Start task", toExport.stringId).getCase()
        File outputFile = new File(outputPath)
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            caseImportExportService.exportCasesWithFiles(List.of(toExport), fos)
            assert outputFile.exists() && !outputFile.isDirectory()
            assert outputFile.length() > 0
        } catch (IOException e) {
            log.error("IO exception occured", e)
        }
        workflowService.deleteCase(toExport)
    }

    @Test
    void importCase() {
        try (FileInputStream fis = new FileInputStream(outputPath)) {
            List<Case> importedCases = caseImportExportService.importCasesWithFiles(fis)
            assert importedCases != null && !importedCases.isEmpty()
            Case importedCase = importedCases[0]
            assertDataImport(importedCase)
            assertDataBehavior(importedCase)
            assert importedCase.tasks.size() == 2
            assert importedCase.tasks.collect { it.transition }.containsAll(["t1", "t2"])
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
    }

    Case createCaseAndSetData() {
        Case testCase = workflowService.createCaseByIdentifier(testNetIdentifier, "export case", "", superCreator.getLoggedSuper()).getCase()
        Map dataSet = [
                "number"                  : ["type": "number", "value": 24.3],
                "number_currency"         : ["type": "number", "value": 223],
                "text"                    : ["type": "text", "value": "test text"],
                "password_data"           : ["type": "text", "value": "password"],
                "text_area"               : ["type": "text", "value": "text area"],
                "enumeration_autocomplete": ["type": "enumeration", "value": "Alice"],
                "enumeration_list"        : ["type": "enumeration", "value": "Alice"],
                "enumeration_map"         : ["type": "enumeration_map", "value": "al"],
                "multichoice"             : ["type": "multichoice", "value": ["Alice", "Carol"]],
                "multichoice_list"        : ["type": "multichoice", "value": ["Alice", "Carol"]],
                "multichoice_map"         : ["type": "multichoice_map", "value": ["al", "ca"]],
                "boolean"                 : ["type": "boolean", "value": "true"],
                "date"                    : ["type": "date", "value": LocalDate.of(2025, Month.APRIL, 1).format(DateTimeFormatter.ISO_DATE)],
                "datetime"                : ["type": "dateTime", "value": LocalDateTime.of(2025, Month.APRIL, 1, 17, 23).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))],
                "taskRef"                 : ["type": "taskRef", "value": [testCase.tasks.find { it.transition == "t2" }.task]],
                "test_i18n"               : ["type": "i18n", "value": new I18nString("test i18n")],
                "user"                    : ["type": "user", "value": userService.findByEmail("super@netgrif.com", true).stringId],
                "userList1"               : ["type": "userList", "value": [userService.findByEmail("super@netgrif.com", true).stringId]],
                "button"                  : ["type": "button", "value": 45],
                "caseRef_field"           : ["type": "caseRef", "value": [testCase.stringId]],
                "stringCollection_field"  : ["type": "stringCollection", "value": ["test_value_1", "test_value_2"]],
        ]
        setFiles(testCase, dataSet)
        return importHelper.setTaskData(testCase.tasks.find { it.transition == "1" }.task, dataSet).getCase()
    }

    void setFiles(Case testCase, Map dataSet) {
        StorageField<?> fileField = testCase.getField("file") as StorageField<?>
        IStorageService fileStorageService = resolverService.resolve(fileField.getStorageType())
        String fileFieldpath = fileStorageService.getPath(testCase.stringId, fileField.stringId, "arc_order_test.xml")
        fileStorageService.save(fileField, fileFieldpath, new FileInputStream(new File(outputFileLocation.concat("arc_order_test.xml"))))
        dataSet.put("file", ["type": "file", "value": "arc_order_test.xml:".concat(fileFieldpath)])
    }

    static void assertDataImport(Case importedCase) {
        assert importedCase.dataSet["number"].value == 24.3
        assert importedCase.dataSet["number_currency"].value == 223
        assert importedCase.dataSet["text"].value == "test text"
        assert importedCase.dataSet["password_data"].value == "password"
        assert importedCase.dataSet["text_area"].value == "text area"
        assert importedCase.dataSet["enumeration_autocomplete"].value.defaultValue == "Alice"
        assert importedCase.dataSet["enumeration"].value.defaultValue == "changed_option"
        assert importedCase.dataSet["enumeration"].choices[0].defaultValue == "changed_option"
        assert importedCase.dataSet["enumeration_list"].value.defaultValue == "Alice"
        assert importedCase.dataSet["enumeration_map"].value == "al"
        assert (importedCase.dataSet["multichoice"].value as Set<I18nString>).size() == 2 && (importedCase.dataSet["multichoice"].value as Set<I18nString>).stream().filter { ["Alice", "Carol"].contains(it.defaultValue) }.toList().size() == 2
        assert (importedCase.dataSet["multichoice_list"].value as Set<I18nString>).size() == 2 && (importedCase.dataSet["multichoice_list"].value as Set<I18nString>).stream().filter { ["Alice", "Carol"].contains(it.defaultValue) }.toList().size() == 2
        assert (importedCase.dataSet["multichoice_map"].value as Set).containsAll(["al", "ca"])
        assert importedCase.dataSet["boolean"].value == true
        assert importedCase.dataSet["date"].value.equals(LocalDate.of(2025, Month.APRIL, 1))
        assert importedCase.dataSet["datetime"].value.equals(LocalDateTime.of(2025, Month.APRIL, 1, 17, 23))
        assert importedCase.dataSet["taskRef"].value.contains(importedCase.tasks.find { it.transition == "t2" }.task)
        assert importedCase.dataSet["test_i18n"].value.defaultValue == "test i18n"
        assert importedCase.dataSet["user"].value == null
        assert importedCase.dataSet["userList1"].value == null
        assert importedCase.dataSet["button"].value == 45
        assert importedCase.dataSet["caseRef_field"].value.contains(importedCase.stringId)
        assert importedCase.dataSet["stringCollection_field"].value.containsAll(["test_value_1", "test_value_2"])
        assert importedCase.dataSet["caseRef_change_allowed_nets"].allowedNets.containsAll(["changed_allowed_nets"])
        assert importedCase.dataSet["text"].validations.find { validation -> validation.validationRule == "email" } != null
    }

    static void assertDataBehavior(Case importedCase) {
        assert importedCase.dataSet["number"].behavior.get("t1").size() == 1 && importedCase.dataSet["number"].behavior.get("t1")[0].name() == FieldBehavior.VISIBLE.name()
        assert importedCase.dataSet["text"].behavior.get("t1").size() == 2 && importedCase.dataSet["text"].behavior.get("t1").collect { it.name() }.containsAll([FieldBehavior.EDITABLE.name(), FieldBehavior.REQUIRED.name()])
        assert importedCase.dataSet["enumeration"].behavior.get("t1").size() == 1 && importedCase.dataSet["enumeration"].behavior.get("t1")[0].name() == FieldBehavior.HIDDEN.name()
        assert importedCase.dataSet["multichoice"].behavior.get("t1").size() == 1 && importedCase.dataSet["multichoice"].behavior.get("t1")[0].name() == FieldBehavior.FORBIDDEN.name()
        assert importedCase.dataSet["date"].behavior.get("t1").size() == 2 && importedCase.dataSet["date"].behavior.get("t1").collect { it.name() }.containsAll([FieldBehavior.EDITABLE.name(), FieldBehavior.OPTIONAL.name()])
    }
}