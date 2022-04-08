package com.netgrif.application.engine.action

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.FieldActionsRunner
import com.netgrif.application.engine.petrinet.domain.events.*
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ActionTest {

    @Autowired
    private FieldActionsRunner runner

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Test
    void testActionImports() {
        Case dummy = new Case()
        Task task = Task.with()._id(new ObjectId()).transitionId("0").build()
        Action testAction = new Action('''
            println LocalDate.now()
            println LocalDate.MAX
            println new ObjectId().toString()
        ''', "set")
        runner.run(testAction, dummy, Optional.of(task))
    }

    @Test
    void testImportActionWithId() {
        testHelper.truncateDbs()
        def netOptional = importHelper.createNet("NAE-1616_duplicate_action_id.xml")
        assert netOptional.isPresent()
        def net = netOptional.get()
        assert net.processEvents.get(ProcessEventType.UPLOAD).preActions.size() == 1
        assert net.processEvents.get(ProcessEventType.UPLOAD).preActions.first().definition.contains("process_upload_pre")
        assert net.processEvents.get(ProcessEventType.UPLOAD).postActions.size() == 1
        assert net.processEvents.get(ProcessEventType.UPLOAD).postActions.first().definition.contains("process_upload_post")
        assert net.caseEvents.get(CaseEventType.CREATE).preActions.size() == 1
        assert net.caseEvents.get(CaseEventType.CREATE).preActions.first().definition.contains("case_create_pre")
        assert net.caseEvents.get(CaseEventType.CREATE).postActions.size() == 1
        assert net.caseEvents.get(CaseEventType.CREATE).postActions.first().definition.contains("case_create_post")
        assert net.caseEvents.get(CaseEventType.DELETE).preActions.size() == 1
        assert net.caseEvents.get(CaseEventType.DELETE).preActions.first().definition.contains("case_delete_pre")
        assert net.caseEvents.get(CaseEventType.DELETE).postActions.size() == 1
        assert net.caseEvents.get(CaseEventType.DELETE).postActions.first().definition.contains("case_delete_post")
        def role = net.roles.values().first()
        assert role.events.get(EventType.ASSIGN).preActions.size() == 1
        assert role.events.get(EventType.ASSIGN).preActions.first().definition.contains("role_assign_pre")
        assert role.events.get(EventType.ASSIGN).postActions.size() == 1
        assert role.events.get(EventType.ASSIGN).postActions.first().definition.contains("role_assign_post")
        assert role.events.get(EventType.CANCEL).preActions.size() == 1
        assert role.events.get(EventType.CANCEL).preActions.first().definition.contains("role_cancel_pre")
        assert role.events.get(EventType.CANCEL).postActions.size() == 1
        assert role.events.get(EventType.CANCEL).postActions.first().definition.contains("role_cancel_post")
        def data = net.dataSet.values().first()
        assert data.events.get(DataEventType.GET).preActions.size() == 2
        assert data.events.get(DataEventType.GET).preActions.find {it.importId == "NAE_1616-12"}.definition.contains("data_trigger_get")
        assert data.events.get(DataEventType.GET).preActions.find {it.importId == "NAE_1616-15"}.definition.contains("data_get_pre")
        assert data.events.get(DataEventType.GET).postActions.size() == 1
        assert data.events.get(DataEventType.GET).postActions.first().definition.contains("data_get_post")
        assert data.events.get(DataEventType.SET).preActions.size() == 1
        assert data.events.get(DataEventType.SET).preActions.first().definition.contains("data_set_pre")
        assert data.events.get(DataEventType.SET).postActions.size() == 2
        assert data.events.get(DataEventType.SET).postActions.find {it.importId == "NAE_1616-11"}.definition.contains("data_trigger_set")
        assert data.events.get(DataEventType.SET).postActions.find {it.importId == "NAE_1616-14"}.definition.contains("data_set_post")
        def task = net.getTransitions().values().first()
        assert task.events.get(EventType.ASSIGN).preActions.size() == 1
        assert task.events.get(EventType.ASSIGN).preActions.first().definition.contains("task_assign_pre")
        assert task.events.get(EventType.ASSIGN).postActions.size() == 1
        assert task.events.get(EventType.ASSIGN).postActions.first().definition.contains("task_assign_post")
        assert task.events.get(EventType.CANCEL).preActions.size() == 1
        assert task.events.get(EventType.CANCEL).preActions.first().definition.contains("task_cancel_pre")
        assert task.events.get(EventType.CANCEL).postActions.size() == 1
        assert task.events.get(EventType.CANCEL).postActions.first().definition.contains("task_cancel_post")
        assert task.events.get(EventType.DELEGATE).preActions.size() == 1
        assert task.events.get(EventType.DELEGATE).preActions.first().definition.contains("task_delegate_pre")
        assert task.events.get(EventType.DELEGATE).postActions.size() == 1
        assert task.events.get(EventType.DELEGATE).postActions.first().definition.contains("task_delegate_post")
        assert task.events.get(EventType.FINISH).preActions.size() == 1
        assert task.events.get(EventType.FINISH).preActions.first().definition.contains("task_finish_pre")
        assert task.events.get(EventType.FINISH).postActions.size() == 1
        assert task.events.get(EventType.FINISH).postActions.first().definition.contains("task_finish_post")
        def data_text_0 = task.dataSet.get("text_0")
        assert data_text_0.events.get(DataEventType.GET).preActions.size() == 2
        assert data_text_0.events.get(DataEventType.GET).preActions.find {it.importId == "NAE_1616-17"}.definition.contains("text_0_dataref_trigger_get")
        assert data_text_0.events.get(DataEventType.GET).preActions.find {it.importId == "NAE_1616-21"}.definition.contains("text_0_dataref_get_pre")
        assert data_text_0.events.get(DataEventType.GET).postActions.size() == 1
        assert data_text_0.events.get(DataEventType.GET).postActions.first().definition.contains("text_0_dataref_get_post")
        assert data_text_0.events.get(DataEventType.SET).preActions.size() == 1
        assert data_text_0.events.get(DataEventType.SET).preActions.first().definition.contains("text_0_dataref_set_pre")
        assert data_text_0.events.get(DataEventType.SET).postActions.size() == 2
        assert data_text_0.events.get(DataEventType.SET).postActions.find {it.importId == "NAE_1616-18"}.definition.contains("text_0_dataref_trigger_set")
        assert data_text_0.events.get(DataEventType.SET).postActions.find {it.importId == "NAE_1616-20"}.definition.contains("text_0_dataref_set_post")
        def data_text_1 = task.dataSet.get("text_1")
        assert data_text_1.events.get(DataEventType.GET).preActions.size() == 2
        assert data_text_1.events.get(DataEventType.GET).preActions.find {it.importId == "NAE_1616-31"}.definition.contains("text_1_dataref_trigger_get")
        assert data_text_1.events.get(DataEventType.GET).preActions.find {it.importId == "NAE_1616-35"}.definition.contains("text_1_dataref_get_pre")
        assert data_text_1.events.get(DataEventType.GET).postActions.size() == 1
        assert data_text_1.events.get(DataEventType.GET).postActions.first().definition.contains("text_1_dataref_get_post")
        assert data_text_1.events.get(DataEventType.SET).preActions.size() == 1
        assert data_text_1.events.get(DataEventType.SET).preActions.first().definition.contains("text_1_dataref_set_pre")
        assert data_text_1.events.get(DataEventType.SET).postActions.size() == 2
        assert data_text_1.events.get(DataEventType.SET).postActions.find {it.importId == "NAE_1616-32"}.definition.contains("text_1_dataref_trigger_set")
        assert data_text_1.events.get(DataEventType.SET).postActions.find {it.importId == "NAE_1616-34"}.definition.contains("text_1_dataref_set_post")
    }
}
