package com.netgrif.workflow.pdf.service

import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.service.UserService;
import com.netgrif.workflow.importer.service.Importer;
import com.netgrif.workflow.ipc.TaskApiTest;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfGenerator;
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

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

    public static final String[] TESTING_DATA = ["data_test.xml", "newmodel.xml", "newmodel2.xml", "personal_information.xml",
    "old_with_dg.xml", "new_without_dg.xml"]

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
}

    @Test
    void testPdfGenerator(){
        //Optional<PetriNet> net = petriNetService.importPetriNet(stream(TESTING_DATA), "dev", userService.getSystem().transformToLoggedUser())
        Case testCase = workflowService.createCase(net.get().getStringId(), "Test PDF", "", userService.getSystem().transformToLoggedUser())
        dataService.getDataGroups(testCase.getTasks()[0].getTask(), Locale.ENGLISH)
        pdfGenerator.convertCaseForm(testCase, "1")
    }

}
