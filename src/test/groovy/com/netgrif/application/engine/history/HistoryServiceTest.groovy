package com.netgrif.application.engine.history

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.history.service.IHistoryService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.NumberField
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
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
class HistoryServiceTest {

    @Autowired
    private IHistoryService historyService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ITaskService taskService

    @Autowired
    private IDataService dataService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IUserService userService

    private PetriNet net

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null
        this.net = net.getNet()

    }

    @Test
    void findAllAssignTaskEventLogsByCaseIdTest() {
        CreateCaseParams createCaseParams = CreateCaseParams.builder()
                .petriNet(net)
                .title("Test assign")
                .color("")
                .loggedUser(superCreator.getLoggedSuper())
                .build()
        Case caze = workflowService.createCase(createCaseParams).getCase()
        int count = historyService.findAllAssignTaskEventLogsByCaseId(caze.getStringId()).size()
        assert count == 0
        String task = caze.tasks.values().find { it.transitionId == "1" }.taskStringId
        taskService.assignTask(superCreator.getLoggedSuper(), task)
        Thread.sleep(1000) // HistoryService::save is @Async
        assert historyService.findAllAssignTaskEventLogsByCaseId(caze.getStringId()).size() == count + 2 // 2 PRE POST
    }

    @Test
    void findAllFinishTaskEventLogsByCaseId() {
        CreateCaseParams createCaseParams = CreateCaseParams.builder()
                .petriNet(net)
                .title("Test finish")
                .color("")
                .loggedUser(superCreator.getLoggedSuper())
                .build()
        Case caze = workflowService.createCase(createCaseParams).getCase()
        int count = historyService.findAllFinishTaskEventLogsByCaseId(caze.getStringId()).size()
        assert count == 0
        String task = caze.tasks.values().find { it.transitionId == "1" }.taskStringId
        taskService.assignTask(superCreator.getLoggedSuper(), task)
        assert historyService.findAllFinishTaskEventLogsByCaseId(caze.getStringId()).size() == count
        taskService.finishTask(superCreator.getLoggedSuper(), task)
        Thread.sleep(1000) // HistoryService::save is @Async
        assert historyService.findAllFinishTaskEventLogsByCaseId(caze.getStringId()).size() == count + 2  // 2 PRE POST
    }

    @Test
    void findAllSetDataEventLogsByCaseId() {
        CreateCaseParams createCaseParams = CreateCaseParams.builder()
                .petriNet(net)
                .title("Test set data")
                .color("")
                .loggedUser(superCreator.getLoggedSuper())
                .build()
        Case caze = workflowService.createCase(createCaseParams).getCase()
        int count = historyService.findAllSetDataEventLogsByCaseId(caze.getStringId()).size()
        assert count == 0
        String task = caze.tasks.values().find { it.transitionId == "1" }.taskStringId
        dataService.setData(task, DataSet.of("number", new NumberField(rawValue: 110101116103114105102)), userService.loggedOrSystem)
        Thread.sleep(1000) // HistoryService::save is @Async
        assert historyService.findAllSetDataEventLogsByCaseId(caze.getStringId()).size() == count + 3  // 3 PRE EXECUTION POST
    }

}
