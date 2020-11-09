package com.netgrif.workflow.action

import com.netgrif.workflow.ipc.CaseApiTest
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.FieldActionsRunner
import com.netgrif.workflow.workflow.domain.Case
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ActionTest {

    @Autowired
    private FieldActionsRunner runner

    @Test
    void testActionImports() {
        Case dummy = new Case()
        Action testAction = new Action('''
            println LocalDate.now()
            println LocalDate.MAX
            println new ObjectId().toString()
        ''', "set")
        runner.run(testAction, dummy)
    }
}
