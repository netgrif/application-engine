package com.netgrif.workflow.ipc

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.history.domain.repository.EventLogRepository
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository
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
    private TestHelper testHelper

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }
    private boolean initialised = false

    @Before
    void setup() {
        if (!initialised) {
            testHelper.truncateDbs()
            initialised = true
        }
    }

    public static final String TASK_EVENTS_NET_FILE = "task_events.xml"
    public static final String TASK_EVENTS_NET_TITLE = "Task events"
    public static final String TASK_EVENTS_NET_INITIALS = "TEN"
    public static final String TASK_EVENTS_TASK = "Task"

    @Test
    void testTaskEventActions() {
        def netOptional = importer.importPetriNet(stream(TASK_EVENTS_NET_FILE), TASK_EVENTS_NET_TITLE, TASK_EVENTS_NET_INITIALS)

        assert netOptional.isPresent()

        PetriNet net = netOptional.get()
        Case useCase = helper.createCase(TASK_EVENTS_NET_TITLE, net)
        helper.assignTaskToSuper(TASK_EVENTS_TASK, useCase.stringId)
        helper.finishTaskAsSuper(TASK_EVENTS_TASK, useCase.stringId)

        User logged = userService.loggedOrSystem

        assert eventLogRepository.findAll()
    }

    public static final String LIMITS_NET_FILE = "test_inter_data_actions_static.xml"
    public static final String LIMITS_NET_TITLE = "Limits"
    public static final String LIMITS_NET_INITIALS = "Lim"
    public static final String LEASING_NET_FILE = "test_inter_data_actions_dynamic.xml"
    public static final String LEASING_NET_INITIALS = "LEA"
    public static final String LEASING_NET_TITLE = "Leasing"
    public static final String LEASING_NET_TASK_EDIT_COST = "T2"

    @Test
    void testTaskExecution() {
        def limitsNetOptional = importer.importPetriNet(stream(LIMITS_NET_FILE), LIMITS_NET_TITLE, LIMITS_NET_INITIALS)
        def leasingNetOptional = importer.importPetriNet(stream(LEASING_NET_FILE), LEASING_NET_TITLE, LEASING_NET_INITIALS)

        assert limitsNetOptional.isPresent()
        assert leasingNetOptional.isPresent()

        PetriNet limitsNet = limitsNetOptional.get()
        PetriNet leasingNet = leasingNetOptional.get()

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

        limits = caseRepository.findOne(limits.stringId)
        leasing1 = caseRepository.findOne(leasing1.stringId)
        leasing2 = caseRepository.findOne(leasing2.stringId)

//@formatter:off
        assert limits.dataSet["limit"].value as Double  == 970_000 as Double
        assert leasing1.dataSet["2"].value as Double    == 970_000 as Double
        assert leasing1.dataSet["1"].value as Double    ==  30_000 as Double
        assert leasing2.dataSet["2"].value as Double    == 970_000 as Double
        assert leasing2.dataSet["1"].value as Double    ==       0 as Double
//@formatter:on

        helper.assignTaskToSuper(LEASING_NET_TASK_EDIT_COST, leasing2.stringId)
        helper.setTaskData(LEASING_NET_TASK_EDIT_COST, leasing2.stringId, [
                "1": [
                        value: 20_000 as Double,
                        type : helper.FIELD_NUMBER
                ]
        ])
        helper.finishTaskAsSuper(LEASING_NET_TASK_EDIT_COST, leasing2.stringId)

        limits = caseRepository.findOne(limits.stringId)
        leasing1 = caseRepository.findOne(leasing1.stringId)
        leasing2 = caseRepository.findOne(leasing2.stringId)

//@formatter:off
        assert limits.dataSet["limit"].value as Double  == 950_000 as Double
        assert leasing1.dataSet["2"].value as Double    == 950_000 as Double
        assert leasing1.dataSet["1"].value as Double    ==  30_000 as Double
        assert leasing2.dataSet["2"].value as Double    == 950_000 as Double
        assert leasing2.dataSet["1"].value as Double    ==  20_000 as Double
//@formatter:on
    }
}