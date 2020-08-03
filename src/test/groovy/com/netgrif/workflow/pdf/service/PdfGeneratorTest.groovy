package com.netgrif.workflow.pdf.service


import com.netgrif.workflow.auth.service.UserService
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.ipc.TaskApiTest
import com.netgrif.workflow.pdf.generator.config.PdfResource
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfGenerator
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["dev"])
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
    private IDataService dataService;

    @Autowired
    private PdfResource pdfResource;

    public static final String[] TESTING_DATA = ["pdf_test_1.xml", "pdf_test_2.xml", "pdf_test_3.xml"]

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
}

    @Test
    void testPdfGenerator(){
        TESTING_DATA.each {it ->
            Optional<PetriNet> net = petriNetService.importPetriNet(stream(it), "major", userService.getSystem().transformToLoggedUser())
            Case testCase = workflowService.createCase(net.get().getStringId(), "Test PDF", "", userService.getSystem().transformToLoggedUser())
            dataService.getDataGroups(testCase.getTasks()[0].getTask(), Locale.ENGLISH)
            pdfResource.setOutputResource(new ClassPathResource("src/main/resources/out_" + it + "_.pdf"))
            pdfGenerator.convertCaseForm(testCase, "1", pdfResource)
        }
    }

}
