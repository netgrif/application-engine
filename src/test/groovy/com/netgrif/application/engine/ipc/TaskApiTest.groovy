package com.netgrif.application.engine.ipc

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.history.domain.baseevent.EventLog
import com.netgrif.application.engine.history.domain.baseevent.repository.EventLogRepository
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
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
class TaskApiTest {

    @Autowired
    private Importer importer

    @Autowired
    private ImportHelper helper

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private TaskRepository taskRepository

    @Autowired
    private IUserService userService

    @Autowired
    private EventLogRepository eventLogRepository

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private TestHelper testHelper

    @Autowired
    private SuperCreator superCreator;

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }
    private boolean initialised = false

    @BeforeEach
    void setup() {
        if (!initialised) {
            testHelper.truncateDbs()
            initialised = true
        }
    }

    public static final String TASK_SEARCH_NET_FILE = "ipc_task_search.xml"

    @Test
    @Disabled("GroovyRuntime Could not find matching constructor")
    void testTaskSearch() {
        def netOptional = petriNetService.importPetriNet(stream(TASK_SEARCH_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert netOptional.getNet() != null

        PetriNet net = netOptional.getNet()
        5.times {
            helper.createCase(TASK_EVENTS_NET_TITLE, net)
        }
        Case useCase = helper.createCase(TASK_EVENTS_NET_TITLE, net)

        helper.assignTaskToSuper(TASK_EVENTS_TASK, useCase.stringId)
        helper.finishTaskAsSuper(TASK_EVENTS_TASK, useCase.stringId)

        def caseOpt = caseRepository.findById(useCase.stringId)

        assert caseOpt.isPresent()
        useCase = caseOpt.get()

        assert useCase.dataSet["field"].value == 6
        assert useCase.dataSet["task_one"].value == net.stringId
        assert useCase.dataSet["paged"].value == 2
    }

    public static final String TASK_EVENTS_NET_FILE = "task_events.xml"
    public static final String TASK_EVENTS_NET_TITLE = "Task events"
    public static final String TASK_EVENTS_NET_INITIALS = "TEN"
    public static final String TASK_EVENTS_TASK = "Task"

    @Test
    @Disabled()
    void testTaskEventActions() {
        def netOptional = petriNetService.importPetriNet(stream(TASK_EVENTS_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert netOptional.getNet() != null

        PetriNet net = netOptional.getNet()
        Case useCase = helper.createCase(TASK_EVENTS_NET_TITLE, net)
        helper.assignTaskToSuper(TASK_EVENTS_TASK, useCase.stringId)
        helper.finishTaskAsSuper(TASK_EVENTS_TASK, useCase.stringId)

        List<EventLog> log = eventLogRepository.findAll()

//        assert log.findAll {
//            it instanceof UserTaskEventLog && it.transitionId == "work_task" && it.message.contains("assigned")
//        }.size() == 2
//        assert log.findAll {
//            it instanceof UserTaskEventLog && it.transitionId == "work_task" && it.message.contains("canceled")
//        }.size() == 1
//        assert log.findAll {
//            it instanceof UserTaskEventLog && it.transitionId == "work_task" && it.message.contains("finished")
//        }.size() == 1
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
        def limitsNetOptional = petriNetService.importPetriNet(stream(LIMITS_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        def leasingNetOptional = petriNetService.importPetriNet(stream(LEASING_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert limitsNetOptional.getNet() != null
        assert leasingNetOptional.getNet() != null

        PetriNet limitsNet = limitsNetOptional.getNet()
        PetriNet leasingNet = leasingNetOptional.getNet()

        Case limits = helper.createCase("Limits BA", limitsNet)
        Case leasing1 = helper.createCase("Leasing 1", leasingNet)
        Case leasing2 = helper.createCase("Leasing 2", leasingNet)

        helper.assignTaskToSuper(LEASING_NET_TASK_EDIT_COST, leasing1.stringId)
        helper.setTaskData(LEASING_NET_TASK_EDIT_COST, leasing1.stringId, [
                "1": [
                        value: 30_000 as Double,
                        type : helper.FIELD_NUMBER
                ]
        ])
        helper.finishTaskAsSuper(LEASING_NET_TASK_EDIT_COST, leasing1.stringId)

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
        assert limits.dataSet["limit"].value as Double == 970_000 as Double
        assert leasing1.dataSet["2"].value as Double == 970_000 as Double
        assert leasing1.dataSet["1"].value as Double == 30_000 as Double
        assert leasing2.dataSet["2"].value as Double == 970_000 as Double
        assert leasing2.dataSet["1"].value as Double == 0 as Double
//@formatter:on

        helper.assignTaskToSuper(LEASING_NET_TASK_EDIT_COST, leasing2.stringId)
        helper.setTaskData(LEASING_NET_TASK_EDIT_COST, leasing2.stringId, [
                "1": [
                        value: 20_000 as Double,
                        type : helper.FIELD_NUMBER
                ]
        ])
        helper.finishTaskAsSuper(LEASING_NET_TASK_EDIT_COST, leasing2.stringId)

        limitsOpt = caseRepository.findById(limits.stringId)
        leasing1Opt = caseRepository.findById(leasing1.stringId)
        leasing2Opt = caseRepository.findById(leasing2.stringId)

        assert limitsOpt.isPresent()
        assert leasing1Opt.isPresent()
        assert leasing2Opt.isPresent()
        limits = limitsOpt.get()
        leasing1 = leasing1Opt.get()
        leasing2 = leasing2Opt.get()

        assert limits.dataSet["limit"].value as Double == 950_000 as Double
        assert leasing1.dataSet["2"].value as Double == 950_000 as Double
        assert leasing1.dataSet["1"].value as Double == 30_000 as Double
        assert leasing2.dataSet["2"].value as Double == 950_000 as Double
        assert leasing2.dataSet["1"].value as Double == 20_000 as Double
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
            helper.createCase("Case $it", net)
        }

        Case control = helper.createCase("Control case", net)
        helper.assignTaskToSuper(TASK_BULK_TASK, control.stringId)
        helper.finishTaskAsSuper(TASK_BULK_TASK, control.stringId)

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

        def case1 = helper.createCase("Case 1", net)
        helper.setTaskData(TASK_GETTER_TASK, case1.stringId, [
                (DATA_TEXT)  : [
                        "value": "text",
                        "type" : "text"
                ],
                (DATA_NUMBER): [
                        "value": 13,
                        "type" : "number"
                ]
        ])

        Case control = helper.createCase("Control case", net)
        helper.assignTaskToSuper(TASK_GETTER_TASK, control.stringId)

        def controlOpt = caseRepository.findById(control.stringId)

        assert controlOpt.isPresent()
        control = controlOpt.get()
        assert control.dataSet[DATA_TEXT].value == "text"
        assert control.dataSet[DATA_NUMBER].value == 13
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

        def control = helper.createCase("Control case", net)
        def case1 = helper.createCase("Case 1", net)

        helper.assignTaskToSuper(TASK_SETTER_TASK, control.stringId)
        def case1Opt = caseRepository.findById(case1.stringId)

        assert case1Opt.isPresent()
        case1 = case1Opt.get()
        assert case1.dataSet[DATA_TEXT].value == "some text"
        assert case1.dataSet[DATA_NUMBER].value == 10
    }
}