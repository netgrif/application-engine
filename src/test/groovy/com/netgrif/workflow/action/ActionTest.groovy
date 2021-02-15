package com.netgrif.workflow.action


import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.FieldActionsRunner
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.Task
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

    @Test
    void testActionImports() {
//        Case dummy = new Case()
//        Task task = Task.with()._id(new ObjectId()).transitionId("0").build()
//        Action testAction = new Action('''
//            println LocalDate.now()
//            println LocalDate.MAX
//            println new ObjectId().toString()
//        ''', "set")
//        runner.run(testAction, dummy, Optional.of(task))
    }
}
