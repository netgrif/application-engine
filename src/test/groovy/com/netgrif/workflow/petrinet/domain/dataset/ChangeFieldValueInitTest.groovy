package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.QTask
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetOutcome
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@ActiveProfiles(["test"])
@RunWith(SpringRunner.class)
class ChangeFieldValueInitTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private ITaskService taskService

    @Autowired
    private IWorkflowService workflowService

    @Before
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testInitValues() {
        ImportPetriNetOutcome optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/change_field_value_init.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        Case useCase = importHelper.createCase("test", optNet.getNet())

        assert useCase.dataSet["text_static"].value == "TEST VALUE"
        assert useCase.dataSet["text_dynamic"].value == "TEST VALUE DYNAMIC"

        useCase = execute("transition0", useCase)

        assert useCase.dataSet["text_static"].value == "CHANGED VALUE OF text_static"
        assert useCase.dataSet["text_dynamic"].value == "CHANGED VALUE OF text_dynamic"

        useCase = execute("transition1", useCase)

        assert useCase.dataSet["text_static"].value == "TEST VALUE"
        assert useCase.dataSet["text_dynamic"].value == "TEST VALUE DYNAMIC"

        useCase = execute("transition2", useCase)

        assert useCase.dataSet["text_static"].value == "TEST VALUE DYNAMIC"
        assert useCase.dataSet["text_dynamic"].value == "TEST VALUE"

    }

    Case execute(String trans, Case useCase) {
        Task task = taskService.searchOne(QTask.task.caseId.eq(useCase.getStringId()) & QTask.task.transitionId.eq(trans))
        taskService.assignTask(task.stringId)
        taskService.finishTask(task.stringId)
        return reload(useCase)
    }

    Case reload(Case useCase) {
        return workflowService.findOne(useCase.getStringId())
    }
}
