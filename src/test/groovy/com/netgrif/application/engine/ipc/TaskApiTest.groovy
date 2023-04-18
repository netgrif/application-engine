package com.netgrif.application.engine.ipc

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.history.domain.baseevent.EventLog
import com.netgrif.application.engine.history.domain.baseevent.repository.EventLogRepository
import com.netgrif.application.engine.history.domain.petrinetevents.ImportPetriNetEventLog
import com.netgrif.application.engine.history.domain.taskevents.AssignTaskEventLog
import com.netgrif.application.engine.history.domain.taskevents.CancelTaskEventLog
import com.netgrif.application.engine.history.domain.taskevents.FinishTaskEventLog
import com.netgrif.application.engine.history.domain.taskevents.TaskEventLog
import com.netgrif.application.engine.history.domain.userevents.UserEventLog
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.NumberField
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class TaskApiTest extends EngineTest {

    @Autowired
    private EventLogRepository eventLogRepository

    private boolean initialised = false

    @BeforeEach
    void setup() {
        if (!initialised) {
            truncateDbs()
            initialised = true
        }
    }

    public static final String TASK_SEARCH_NET_FILE = "ipc_task_search.xml"

    @Test
    void testTaskSearch() {
        def netOptional = petriNetService.importPetriNet(stream(TASK_SEARCH_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert netOptional.getNet() != null

        PetriNet net = netOptional.getNet()
        5.times {
            importHelper.createCase(TASK_EVENTS_NET_TITLE, net)
        }
        Case useCase = importHelper.createCase(TASK_EVENTS_NET_TITLE, net)

        importHelper.assignTaskToSuper(TASK_EVENTS_TASK, useCase.stringId)
        importHelper.finishTaskAsSuper(TASK_EVENTS_TASK, useCase.stringId)

        def caseOpt = caseRepository.findById(useCase.stringId)

        assert caseOpt.isPresent()
        useCase = caseOpt.get()

        assert useCase.dataSet.get("field").rawValue == 6
        assert useCase.dataSet.get("task_one").rawValue == net.stringId
        assert useCase.dataSet.get("paged").rawValue == 2
    }

    public static final String TASK_EVENTS_NET_FILE = "task_events.xml"
    public static final String TASK_EVENTS_NET_TITLE = "Task events"
    public static final String TASK_EVENTS_NET_INITIALS = "TEN"
    public static final String TASK_EVENTS_TASK = "Task"

    @Test
    void testTaskEventActions() {
        ImportPetriNetEventOutcome netOptional = petriNetService.importPetriNet(stream(TASK_EVENTS_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert netOptional.getNet() != null

        PetriNet net = netOptional.getNet()
        Case useCase = importHelper.createCase(TASK_EVENTS_NET_TITLE, net)
        importHelper.assignTaskToSuper(TASK_EVENTS_TASK, useCase.stringId)
        importHelper.finishTaskAsSuper(TASK_EVENTS_TASK, useCase.stringId)

        List<EventLog> log = eventLogRepository.findAll()

        assert log.findAll {
            it instanceof AssignTaskEventLog && it.transitionId == "work_task"
        }.size() == 2
        assert log.findAll {
            it instanceof CancelTaskEventLog && it.transitionId == "work_task"
        }.size() == 1
        assert log.findAll {
            it instanceof FinishTaskEventLog && it.transitionId == "work_task"
        }.size() == 1
    }

    public static final String LIMITS_NET_FILE = "test_inter_data_actions_static.xml"
    public static final String LIMITS_NET_TITLE = "Limits"
    public static final String LIMITS_NET_INITIALS = "Lim"
    public static final String LEASING_NET_FILE = "test_inter_data_actions_dynamic.xml"
    public static final String LEASING_NET_INITIALS = "LEA"
    public static final String LEASING_NET_TITLE = "Leasing"
    public static final String LEASING_NET_TASK_EDIT_COST = "T2"

    @Test
    @Disabled("spusta 2 krat")
    void testTaskExecution() {
        ImportPetriNetEventOutcome limitsNetOptional = petriNetService.importPetriNet(stream(LIMITS_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        ImportPetriNetEventOutcome leasingNetOptional = petriNetService.importPetriNet(stream(LEASING_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert limitsNetOptional.getNet() != null
        assert leasingNetOptional.getNet() != null

        PetriNet limitsNet = limitsNetOptional.getNet()
        PetriNet leasingNet = leasingNetOptional.getNet()

        Case limits = importHelper.createCase("Limits BA", limitsNet)
        Case leasing1 = importHelper.createCase("Leasing 1", leasingNet)
        Case leasing2 = importHelper.createCase("Leasing 2", leasingNet)

        importHelper.assignTaskToSuper(LEASING_NET_TASK_EDIT_COST, leasing1.stringId)
        importHelper.setTaskData(LEASING_NET_TASK_EDIT_COST, leasing1.stringId, new DataSet([
                "1": new NumberField(rawValue: 30_000 as Double)
        ] as Map<String, Field<?>>))
        importHelper.finishTaskAsSuper(LEASING_NET_TASK_EDIT_COST, leasing1.stringId)

        def limitsOpt = caseRepository.findById(limits.stringId)
        def leasing1Opt = caseRepository.findById(leasing1.stringId)
        def leasing2Opt = caseRepository.findById(leasing2.stringId)

        assert limitsOpt.isPresent()
        assert leasing1Opt.isPresent()
        assert leasing2Opt.isPresent()
        limits = limitsOpt.get()
        leasing1 = leasing1Opt.get()
        leasing2 = leasing2Opt.get()

//@formatter:off
        assert limits.dataSet.get("limit").rawValue as Double == 970_000 as Double
        assert leasing1.dataSet.get("2").rawValue as Double == 970_000 as Double
        assert leasing1.dataSet.get("1").rawValue as Double == 30_000 as Double
        assert leasing2.dataSet.get("2").rawValue as Double == 970_000 as Double
        assert leasing2.dataSet.get("1").rawValue as Double == 0 as Double
//@formatter:on

        importHelper.assignTaskToSuper(LEASING_NET_TASK_EDIT_COST, leasing2.stringId)
        importHelper.setTaskData(LEASING_NET_TASK_EDIT_COST, leasing2.stringId, new DataSet([
                "1": new NumberField(rawValue: 20_000 as Double)
        ] as Map<String, Field<?>>))
        importHelper.finishTaskAsSuper(LEASING_NET_TASK_EDIT_COST, leasing2.stringId)

        limitsOpt = caseRepository.findById(limits.stringId)
        leasing1Opt = caseRepository.findById(leasing1.stringId)
        leasing2Opt = caseRepository.findById(leasing2.stringId)

        assert limitsOpt.isPresent()
        assert leasing1Opt.isPresent()
        assert leasing2Opt.isPresent()
        limits = limitsOpt.get()
        leasing1 = leasing1Opt.get()
        leasing2 = leasing2Opt.get()

        assert limits.dataSet.get("limit").rawValue as Double == 950_000 as Double
        assert leasing1.dataSet.get("2").rawValue as Double == 950_000 as Double
        assert leasing1.dataSet.get("1").rawValue as Double == 30_000 as Double
        assert leasing2.dataSet.get("2").rawValue as Double == 950_000 as Double
        assert leasing2.dataSet.get("1").rawValue as Double == 20_000 as Double
    }

    public static final String TASK_BULK_NET_FILE = "ipc_bulk.xml"
    public static final String TASK_BULK_NET_TITLE = "Bulk events"
    public static final String TASK_BULK_NET_INITIALS = "BLK"
    public static final String TASK_BULK_TASK = "Task"

    @Test
    void testTaskBulkActions() {
        def netOptional = petriNetService.importPetriNet(stream(TASK_BULK_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert netOptional.getNet() != null
        PetriNet net = netOptional.getNet()

        10.times {
            importHelper.createCase("Case $it", net)
        }

        Case control = importHelper.createCase("Control case", net)
        importHelper.assignTaskToSuper(TASK_BULK_TASK, control.stringId)
        importHelper.finishTaskAsSuper(TASK_BULK_TASK, control.stringId)

        assert taskRepository.findAll(QTask.task.userId.eq(userService.system.getStringId())).size() == 2
    }

    public static final String TASK_GETTER_NET_FILE = "ipc_data.xml"
    public static final String TASK_GETTER_NET_TITLE = "Data getter"
    public static final String TASK_GETTER_NET_INITIALS = "GET"
    public static final String TASK_GETTER_TASK = "Enabled"
    public static final String DATA_TEXT = "data_text"
    public static final String DATA_NUMBER = "data_number"

    @Test
    void testGetData() {
        def netOptional = petriNetService.importPetriNet(stream(TASK_GETTER_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert netOptional.getNet() != null
        PetriNet net = netOptional.getNet()

        def case1 = importHelper.createCase("Case 1", net)
        importHelper.setTaskData(TASK_GETTER_TASK, case1.stringId, new DataSet([
                (DATA_TEXT)  : new TextField(rawValue: "text"),
                (DATA_NUMBER): new NumberField(rawValue: 13)
        ] as Map<String, Field<?>>))

        Case control = importHelper.createCase("Control case", net)
        importHelper.assignTaskToSuper(TASK_GETTER_TASK, control.stringId)

        def controlOpt = caseRepository.findById(control.stringId)

        assert controlOpt.isPresent()
        control = controlOpt.get()
        assert control.dataSet.get(DATA_TEXT).rawValue == "text"
        assert control.dataSet.get(DATA_NUMBER).rawValue == 13
    }

    public static final String TASK_SETTER_NET_FILE = "ipc_set_data.xml"
    public static final String TASK_SETTER_NET_TITLE = "Data śetter"
    public static final String TASK_SETTER_NET_INITIALS = "SET"
    public static final String TASK_SETTER_TASK = "Enabled"

    @Test
    void testSetData() {
        def netOptional = petriNetService.importPetriNet(stream(TASK_SETTER_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert netOptional.getNet() != null
        PetriNet net = netOptional.getNet()

        def control = importHelper.createCase("Control case", net)
        def case1 = importHelper.createCase("Case 1", net)

        importHelper.assignTaskToSuper(TASK_SETTER_TASK, control.stringId)
        def case1Opt = caseRepository.findById(case1.stringId)

        assert case1Opt.isPresent()
        case1 = case1Opt.get()
        assert case1.dataSet.get(DATA_TEXT).rawValue == "some text"
        assert case1.dataSet.get(DATA_NUMBER).rawValue == 10
    }
}