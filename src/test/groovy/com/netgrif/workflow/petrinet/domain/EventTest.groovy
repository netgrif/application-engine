package com.netgrif.workflow.petrinet.domain

import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.ipc.TaskExecutionTest
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository
import com.netgrif.workflow.workflow.service.TaskService
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
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

    private def stream = { String name ->
        return TaskExecutionTest.getClassLoader().getResourceAsStream(name)
    }

    Case instance

    @Test
    void testEventImport() {
        caseRepository.deleteAll()
        taskRepository.deleteAll()
        netRepository.deleteAll()

        def net = importer.importPetriNet(stream(EVENT_NET_FILE), EVENT_NET_TITLE, EVENT_NET_INITS).get()
        instance = helper.createCase(EVENT_NET_CASE, net)

        def outcome = helper.assignTaskToSuper(EVENT_NET_TASK, instance.stringId)
        assertActionsRuned("${EVENT_NET_TASK}_assign")

        helper.finishTaskAsSuper(EVENT_NET_TASK, instance.stringId)
        assertActionsRuned("${EVENT_NET_TASK}_finish")

        helper.assignTaskToSuper(EVENT_NET_TASK, instance.stringId)
        helper.cancelTaskAsSuper(EVENT_NET_TASK, instance.stringId)
        assertActionsRuned("${EVENT_NET_TASK}_cancel")
    }

    private void assertActionsRuned(String fieldIdWithoutPhase) {
        instance = caseRepository.findOne(instance.stringId)
        assert instance.dataSet["${fieldIdWithoutPhase}_pre" as String].value as String == "${fieldIdWithoutPhase}_pre"
        assert instance.dataSet["${fieldIdWithoutPhase}_post" as String].value as String == "${fieldIdWithoutPhase}_post"
    }
}