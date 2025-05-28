package com.netgrif.application.engine.history

import com.netgrif.application.engine.TestHelper

import com.netgrif.application.engine.history.service.IHistoryService
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.NumberField
import com.netgrif.application.engine.petrinet.domain.params.ImportProcessParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams
import com.netgrif.application.engine.workflow.domain.params.SetDataParams
import com.netgrif.application.engine.workflow.domain.params.TaskParams
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

    private Process net

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        ImportPetriNetEventOutcome net = petriNetService.importProcess(new ImportProcessParams(new FileInputStream("src/test/resources/all_data.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().activeActorId))
        assert net.getProcess() != null
        this.net = net.getProcess()

    }

    @Test
    void findAllAssignTaskEventLogsByCaseIdTest() {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(net)
                .title("Test assign")
                .authorId(superCreator.getLoggedSuper().activeActorId)
                .build()
        Case caze = workflowService.createCase(createCaseParams).getCase()
        int count = historyService.findAllAssignTaskEventLogsByCaseId(caze.getStringId()).size()
        assert count == 0
        String task = caze.getTaskStringId("t1")
        taskService.assignTask(new TaskParams(task, superCreator.getLoggedSuper().activeActorId))
        Thread.sleep(1000) // HistoryService::save is @Async
        assert historyService.findAllAssignTaskEventLogsByCaseId(caze.getStringId()).size() == count + 2 // 2 PRE POST
    }

    @Test
    void findAllFinishTaskEventLogsByCaseId() {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(net)
                .title("Test finish")
                .authorId(superCreator.getLoggedSuper().activeActorId)
                .build()
        Case caze = workflowService.createCase(createCaseParams).getCase()
        int count = historyService.findAllFinishTaskEventLogsByCaseId(caze.getStringId()).size()
        assert count == 0
        String task = caze.getTaskStringId("t1")
        taskService.assignTask(new TaskParams(task, superCreator.getLoggedSuper().activeActorId))
        assert historyService.findAllFinishTaskEventLogsByCaseId(caze.getStringId()).size() == count
        taskService.finishTask(new TaskParams(task, superCreator.getLoggedSuper().activeActorId))
        Thread.sleep(1000) // HistoryService::save is @Async
        assert historyService.findAllFinishTaskEventLogsByCaseId(caze.getStringId()).size() == count + 2  // 2 PRE POST
    }

    @Test
    void findAllSetDataEventLogsByCaseId() {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(net)
                .title("Test set data")
                .authorId(superCreator.getLoggedSuper().activeActorId)
                .build()
        Case caze = workflowService.createCase(createCaseParams).getCase()
        int count = historyService.findAllSetDataEventLogsByCaseId(caze.getStringId()).size()
        assert count == 0
        String task = caze.getTaskStringId("t1")
        dataService.setData(new SetDataParams(task, DataSet.of("number", new NumberField(rawValue: 110101116103114105102)),
                superCreator.getLoggedSuper().activeActorId))
        Thread.sleep(1000) // HistoryService::save is @Async
        assert historyService.findAllSetDataEventLogsByCaseId(caze.getStringId()).size() == count + 3  // 3 PRE EXECUTION POST
    }

}
