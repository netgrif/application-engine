package com.netgrif.application.engine.petrinet.domain

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.authentication.domain.repositories.UserRepository
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.DefaultRoleRunner
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.startup.SystemUserRunner
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
import com.netgrif.application.engine.workflow.service.TaskService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-test.properties"
)
class EventTest {

    public static final String EVENT_NET_FILE = "event_test.xml"
    public static final String EVENT_NET_TITLE = "Events"
    public static final String EVENT_NET_INITS = "EVN"
    public static final String EVENT_NET_TASK = "task"
    public static final String EVENT_NET_CASE = "Event case"

    @Autowired
    private Importer importer

    @Autowired
    private ImportHelper helper

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private TaskService taskService

    @Autowired
    private PetriNetRepository netRepository

    @Autowired
    private TaskRepository taskRepository

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private DefaultRoleRunner roleRunner

    @Autowired
    private MongoTemplate template

    @Autowired
    private UserRepository userRepository

    @Autowired
    private SystemUserRunner userRunner

    @Autowired
    private IPetriNetService petriNetService;
    @Autowired
    private TestHelper testHelper

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    Case instance
    TaskEventOutcome outcome

    @Test
    void testEventImport() {
        testHelper.truncateDbs()

        Process net = petriNetService.importPetriNet(stream(EVENT_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()
        instance = helper.createCase(EVENT_NET_CASE, net)

        outcome = helper.assignTaskToSuper(EVENT_NET_TASK, instance.stringId)
        assertAssignOutcome()

        outcome = helper.finishTaskAsSuper(EVENT_NET_TASK, instance.stringId)
        assertFinishOutcome()

        helper.assignTaskToSuper(EVENT_NET_TASK, instance.stringId)
        outcome = helper.cancelTaskAsSuper(EVENT_NET_TASK, instance.stringId)
        assertCancelOutcome()
    }

    private void assertAssignOutcome() {
        asserOutcome("${EVENT_NET_TASK}_assign", "Task assigned")
    }

    private void assertFinishOutcome() {
        asserOutcome("${EVENT_NET_TASK}_finish", "Task finished")
    }

    private void assertCancelOutcome() {
        asserOutcome("${EVENT_NET_TASK}_cancel", "Task cancelled")
        SetDataEventOutcome preDataChange = outcome.outcomes.get(0).outcomes.get(0) as SetDataEventOutcome
        SetDataEventOutcome chainedDataChange = preDataChange.outcomes.get(0).outcomes.get(0) as SetDataEventOutcome
        assert chainedDataChange.changedFields.get('chained').rawValue == "chained"
    }

    private void asserOutcome(String fieldIdWithoutPhase, String message) {
        def instanceOptional = caseRepository.findById(instance.stringId)

        assert instanceOptional.isPresent()
        instance = instanceOptional.get()

        assert instance.dataSet.get("${fieldIdWithoutPhase}_pre" as String).rawValue == "${fieldIdWithoutPhase}_pre"
        assert instance.dataSet.get("${fieldIdWithoutPhase}_post" as String).rawValue == "${fieldIdWithoutPhase}_post"

        assert outcome.message.defaultValue == message

        SetDataEventOutcome preDataChange = outcome.outcomes.get(0).outcomes.get(0) as SetDataEventOutcome
        SetDataEventOutcome postDataChange = outcome.outcomes.get(1).outcomes.get(0) as SetDataEventOutcome
        assert preDataChange.changedFields.get("${fieldIdWithoutPhase}_pre" as String)
        assert preDataChange.changedFields.get("${fieldIdWithoutPhase}_pre" as String).rawValue == "${fieldIdWithoutPhase}_pre"
        assert postDataChange.changedFields.get("${fieldIdWithoutPhase}_post" as String)
        assert postDataChange.changedFields.get("${fieldIdWithoutPhase}_post" as String).rawValue == "${fieldIdWithoutPhase}_post"
    }
}