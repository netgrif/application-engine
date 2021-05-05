package com.netgrif.workflow.petrinet.domain.dataset


import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ProcessEventsStaticDataSetTest {

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private PetriNetRepository repository

    @Autowired
    private IDataService dataService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private ITaskService taskService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private TestHelper testHelper

    @Before
    void beforeAll() {
        testHelper.truncateDbs()
    }

    @Test
    void testEvents() {
        PetriNet net = importNet("constructor_destructor_with_static_fields.xml")

        assert net.staticDataSet["static_text"].value == "Process event POST"
        assert net.staticDataSet["static_number"].value == (net.staticDataSet["static_text"].value as String).length()

        Case useCase = newCase("Test", net)
        net = petriNetService.getPetriNet(net.stringId)

        assert net.staticDataSet["static_text_2"].value == "Case event POST"
        assert net.staticDataSet["static_number_2"].value == (net.staticDataSet["static_text_2"].value as String).length()
    }



    Case newCase(String title, PetriNet net) {
        return workflowService.createCase(net.stringId, title, "", superCreator.loggedSuper)
    }

    Case execute(String trans, Case useCase) {
        String taskId = getTaskId(trans, useCase)
        taskService.assignTask(taskId)
        taskService.finishTask(taskId)
        return workflowService.findOne(useCase.stringId)
    }

    String getTaskId(String trans, Case useCase) {
        return useCase.tasks.find { it.transition == trans }.task
    }

    PetriNet importNet(String file) {
        def netOpt = petriNetService.importPetriNet(new FileInputStream("src/test/resources/$file"), VersionType.MAJOR, superCreator.getLoggedSuper())
        return netOpt.get()
    }
}
