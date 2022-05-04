package com.netgrif.application.engine.pdf.service

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.configuration.ApplicationContextProvider
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.pdf.generator.config.PdfResource
import com.netgrif.application.engine.pdf.generator.config.types.PdfPageNumberFormat
import com.netgrif.application.engine.pdf.generator.domain.PdfField
import com.netgrif.application.engine.pdf.generator.domain.PdfTextField
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfGenerator
import com.netgrif.application.engine.petrinet.domain.DataGroup
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class PdfGeneratorTest {

    @Autowired
    private Importer importer

    @Autowired
    private IPdfGenerator pdfGenerator

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private UserService userService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private PdfResource pdfResource

    @Autowired
    private IDataService dataService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestHelper testHelper

    @Value('${nae.pdf.resources.outputFolder}')
    private String pdfOutputFolder;

    @Value('${nae.pdf.resources.templateResource}')
    private String pdfTemplateFolder

    public static final String[] TESTING_DATA = ["pdf_test_1.xml", "pdf_test_2.xml", "pdf_test_3.xml", "all_data_pdf.xml", "flow.xml", "datagroup_test_layout.xml", "simple_taskref.xml"]

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void beforeTest() {
        testHelper.truncateDbs()
    }


    @Test
    void testAllData() {
        PdfResource pdfResource = applicationContext.getBean(PdfResource.class)
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(stream(TESTING_DATA[3]), VersionType.MAJOR, userService.getSystem().transformToLoggedUser())
        Case testCase = workflowService.createCase(net.getNet().getStringId(), "Test PDF", "", userService.getSystem().transformToLoggedUser()).getCase()
        testCase.getPetriNet().getTransition("1").setDataGroups(getDataGroupMap(dataService.getDataGroups(testCase.getTasks()[0].getTask(), Locale.ENGLISH).getData()))
        pdfResource.setOutputResource(new ClassPathResource(pdfOutputFolder+"/out_" + TESTING_DATA[3] + "_.pdf"))
        pdfGenerator.setupPdfGenerator(pdfResource)
        pdfGenerator.generatePdf(testCase, "1", pdfResource)

        File file = new File(pdfOutputFolder+"/out_" + TESTING_DATA[3] + "_.pdf")
        assert file.exists()
    }

    @Test
    void testingNormal() {
        PdfResource pdfResource = applicationContext.getBean(PdfResource.class)
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(stream(TESTING_DATA[0]), VersionType.MAJOR, userService.getSystem().transformToLoggedUser())
        Case testCase = workflowService.createCase(net.getNet().getStringId(), "Test PDF", "", userService.getSystem().transformToLoggedUser()).getCase()
        testCase.getPetriNet().getTransition("1").setDataGroups(getDataGroupMap(dataService.getDataGroups(testCase.getTasks()[0].getTask(), Locale.ENGLISH).getData()))
        pdfResource.setOutputResource(new ClassPathResource(pdfOutputFolder+"/out_" + TESTING_DATA[0] + "_.pdf"))
        pdfGenerator.setupPdfGenerator(pdfResource)
        pdfGenerator.generatePdf(testCase, "1", pdfResource)

        File file = new File(pdfOutputFolder+"/out_" + TESTING_DATA[0] + "_.pdf")
        assert file.exists()
    }

    @Test
    void testingWithTemplate() {
        PdfResource pdfResource = applicationContext.getBean(PdfResource.class)
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(stream(TESTING_DATA[1]), VersionType.MAJOR, userService.getSystem().transformToLoggedUser())
        Case testCase = workflowService.createCase(net.getNet().getStringId(), "Test PDF", "", userService.getSystem().transformToLoggedUser()).getCase()
        testCase.getPetriNet().getTransition("1").setDataGroups(getDataGroupMap(dataService.getDataGroups(testCase.getTasks()[0].getTask(), Locale.ENGLISH).getData()))
        pdfResource.setOutputResource(new ClassPathResource(pdfOutputFolder+"/out_" + TESTING_DATA[1] + "_.pdf"))
        pdfResource.setMarginLeft(75)
        pdfResource.setMarginRight(75)
        pdfResource.setMarginTitle(100)
        pdfResource.updateProperties()
        pdfResource.setTemplateResource(new ClassPathResource(pdfTemplateFolder))
        pdfGenerator.setupPdfGenerator(pdfResource)
        pdfGenerator.generatePdf(testCase, "1", pdfResource)

        File file = new File(pdfOutputFolder+"/out_" + TESTING_DATA[1] + "_.pdf")
        assert file.exists()
    }

    @Test
    void testingCustomFunction() {
        PdfResource pdfResource = applicationContext.getBean(PdfResource.class)
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(stream(TESTING_DATA[1]), VersionType.MAJOR, userService.getSystem().transformToLoggedUser())
        Case testCase = workflowService.createCase(net.getNet().getStringId(), "Test PDF", "", userService.getSystem().transformToLoggedUser()).getCase()
        testCase.getPetriNet().getTransition("1").setDataGroups(getDataGroupMap(dataService.getDataGroups(testCase.getTasks()[0].getTask(), Locale.ENGLISH).getData()))
        String filename = pdfResource.getOutputDefaultName()
        String storagePath = pdfResource.getOutputFolder() + File.separator + testCase.stringId + "-" + "fileField1" + "-" + pdfResource.getOutputDefaultName()
        pdfResource.setOutputResource(new ClassPathResource(storagePath))
        pdfResource.setMarginTitle(100)
        pdfResource.setMarginLeft(75)
        pdfResource.setMarginRight(75)
        pdfResource.updateProperties()
        pdfGenerator.setupPdfGenerator(pdfResource)
        pdfGenerator.generatePdf(testCase, "1", pdfResource)

        File file = new File(storagePath)
        assert file.exists()
    }

    @Test
    void testingLongDocument() {
        PdfResource pdfResource = applicationContext.getBean(PdfResource.class)
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(stream(TESTING_DATA[2]), VersionType.MAJOR, userService.getSystem().transformToLoggedUser())
        Case testCase = workflowService.createCase(net.getNet().getStringId(), "Test PDF", "", userService.getSystem().transformToLoggedUser()).getCase()
        testCase.getPetriNet().getTransition("1").setDataGroups(getDataGroupMap(dataService.getDataGroups(testCase.getTasks()[0].getTask(), Locale.ENGLISH).getData()))
        pdfResource.setOutputResource(new ClassPathResource(pdfOutputFolder+"/out_" + TESTING_DATA[2] + "_.pdf"))
        pdfGenerator.setupPdfGenerator(pdfResource)
        pdfGenerator.generatePdf(testCase, "1", pdfResource)

        File file = new File(pdfOutputFolder+"/out_" + TESTING_DATA[2] + "_.pdf")
        assert file.exists()
    }

    @Test
    void testingPageNumber() {
        PdfResource pdfResource = applicationContext.getBean(PdfResource.class)
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(stream(TESTING_DATA[2]), VersionType.MAJOR, userService.getSystem().transformToLoggedUser())
        Case testCase = workflowService.createCase(net.getNet().getStringId(), "Test PDF", "", userService.getSystem().transformToLoggedUser()).getCase()
        testCase.getPetriNet().getTransition("1").setDataGroups(getDataGroupMap(dataService.getDataGroups(testCase.getTasks()[0].getTask(), Locale.ENGLISH).getData()))
        pdfResource.setNumberFormat(Locale.US)
        pdfResource.setPageNumberPosition(pdfResource.getMarginLeft())
        pdfResource.setPageNumberFormat(PdfPageNumberFormat.SIMPLE)
        pdfResource.setOutputResource(new ClassPathResource(pdfOutputFolder+"/out_page_number_.pdf"))
        pdfGenerator.setupPdfGenerator(pdfResource)
        pdfGenerator.generatePdf(testCase, "1", pdfResource)

        File file = new File(pdfOutputFolder+"/out_" + TESTING_DATA[2] + "_.pdf")
        assert file.exists()
    }

    @Test
    void testingCustomField() {
        PdfResource pdfResource = applicationContext.getBean(PdfResource.class)
        PdfField pdf = new PdfTextField("footer_company_title",
                null,
                "Netgrif Application Engine",
                FieldType.TEXT,
                pdfResource.getMarginLeft(),
                pdfResource.getPageHeight() - pdfResource.getMarginBottom(),
                (int) (pdfResource.getPageDrawableWidth() / pdfResource.getFormGridCols()),
                pdfResource.getLineHeight(),
                pdfResource)

        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(stream(TESTING_DATA[2]), VersionType.MAJOR, userService.getSystem().transformToLoggedUser())
        Case testCase = workflowService.createCase(net.getNet().getStringId(), "Test PDF", "", userService.getSystem().transformToLoggedUser()).getCase()
        testCase.getPetriNet().getTransition("1").setDataGroups(getDataGroupMap(dataService.getDataGroups(testCase.getTasks()[0].getTask(), Locale.ENGLISH).getData()))
        pdfResource.setOutputResource(new ClassPathResource(pdfOutputFolder+"/out_custom_field.pdf"))
        pdfGenerator.setupPdfGenerator(pdfResource)
        pdfGenerator.addCustomField(pdf, pdfResource)
        pdfGenerator.generatePdf(testCase, "1", pdfResource)

        File file = new File(pdfOutputFolder+"/out_" + TESTING_DATA[2] + "_.pdf")
        assert file.exists()
    }

    @Test
    void testFlowLayout() {
        PdfResource pdfResource = applicationContext.getBean(PdfResource.class)
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(stream(TESTING_DATA[4]), VersionType.MAJOR, userService.getSystem().transformToLoggedUser())
        Case testCase = workflowService.createCase(net.getNet().getStringId(), "Test PDF", "", userService.getSystem().transformToLoggedUser()).getCase()
        testCase.getPetriNet().getTransition("t1").setDataGroups(getDataGroupMap(dataService.getDataGroups(testCase.getTasks()[0].getTask(), Locale.ENGLISH).getData()))
        pdfResource.setOutputResource(new ClassPathResource(pdfOutputFolder+"/out_" + TESTING_DATA[4] + "_.pdf"))
        pdfGenerator.setupPdfGenerator(pdfResource)
        pdfGenerator.generatePdf(testCase, "t1", pdfResource)

        File file = new File(pdfOutputFolder+"/out_" + TESTING_DATA[4] + "_.pdf")
        assert file.exists()
    }

    @Test
    void testDataGroup() {
        PdfResource pdfResource = applicationContext.getBean(PdfResource.class)
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(stream(TESTING_DATA[5]), VersionType.MAJOR, userService.getSystem().transformToLoggedUser())
        Case testCase = workflowService.createCase(net.getNet().getStringId(), "Test PDF", "", userService.getSystem().transformToLoggedUser()).getCase()
        pdfResource.setOutputResource(new ClassPathResource(pdfOutputFolder+"/out_" + TESTING_DATA[5] + "_.pdf"))
        pdfGenerator.setupPdfGenerator(pdfResource)
        pdfGenerator.generatePdf(testCase, "t1", pdfResource)

        File file = new File(pdfOutputFolder+"/out_" + TESTING_DATA[5] + "_.pdf")
        assert file.exists()
    }

    @Test
    void testTaskRef() {
        PdfResource pdfResource = applicationContext.getBean(PdfResource.class)
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(stream(TESTING_DATA[6]), VersionType.MAJOR, userService.getSystem().transformToLoggedUser())
        Case testCase = workflowService.createCase(net.getNet().getStringId(), "Test PDF", "", userService.getSystem().transformToLoggedUser()).getCase()
        pdfResource.setOutputResource(new ClassPathResource(pdfOutputFolder+"/out_" + TESTING_DATA[6] + "_.pdf"))
        pdfGenerator.setupPdfGenerator(pdfResource)
        pdfGenerator.generatePdf(testCase, "t1", pdfResource)

        File file = new File(pdfOutputFolder+"/out_" + TESTING_DATA[6] + "_.pdf")
        assert file.exists()
    }


    private Map<String, DataGroup> getDataGroupMap(List<DataGroup> dataGroupList) {
        Map<String, DataGroup> dataGroupMap = new HashMap<>()
        dataGroupList.each {
            dataGroupMap.put(it.stringId, it)
        }
        return dataGroupMap
    }


}
