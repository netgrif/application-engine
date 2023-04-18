package com.netgrif.application.engine

import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.State
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@Slf4j
@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class Async extends EngineTest {

    @Test
    void testAsyncRun() {
        truncateDbs()
        def netOptional = importHelper.createNet("async.xml")
        assert netOptional.isPresent()
        def net = netOptional.get()
        def $case = importHelper.createCase("Async run", net)

        def t1Id = $case.getTaskStringId("t1")

        taskService.assignTask(t1Id)
        taskService.finishTask(t1Id)

        $case = workflowService.findOne($case.stringId)
        List<Task> tasks = taskService.findAllByCase($case.stringId)

        assert $case.activePlaces["p1"] == null
        assert $case.activePlaces["p2"] == 1
        assert $case.activePlaces["p3"] == null
        assert $case.activePlaces["p4"] == 1
        assert $case.dataSet.get("text_0").rawValue as String == "A"
        assert $case.dataSet.get("text_1").rawValue as String == "B"
        assert $case.dataSet.get("text_2").rawValue as String == "K"
        assert tasks.find {it.transitionId == "t1"}.state == State.DISABLED
        assert tasks.find {it.transitionId == "t2"}.state == State.DISABLED
    }
}
