package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
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

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testInitValues() {
        ImportPetriNetEventOutcome optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/change_field_value_init.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
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
