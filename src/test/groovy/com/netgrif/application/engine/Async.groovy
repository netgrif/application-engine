package com.netgrif.application.engine

import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.util.logging.Slf4j
import org.junit.Ignore
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
@Ignore
class Async {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private ITaskService taskService

    @Autowired
    private IWorkflowService workflowService

    @Test
    void testAsyncRun() {
        testHelper.truncateDbs()
        def netOptional = importHelper.createNet("async.xml")
        assert netOptional.isPresent()
        def net = netOptional.get()
        def $case = importHelper.createCase("Async run", net)

        def t1Id = $case.tasks.find { it.transition == "t1" }.task

        taskService.assignTask(t1Id)
        taskService.finishTask(t1Id)

        $case = workflowService.findOne($case.stringId)
        def tasks = taskService.findAllByCase($case.stringId, Locale.UK)

        assert $case.tasks.size() == 0
        assert $case.activePlaces["p1"] == null
        assert $case.activePlaces["p2"] == 1
        assert $case.activePlaces["p3"] == null
        assert $case.activePlaces["p4"] == 1
        // TODO: NAE-1645 value == null
        assert $case.dataSet.get("text_0").rawValue as String == "A"
        assert $case.dataSet.get("text_1").rawValue as String == "B"
        assert $case.dataSet.get("text_2").rawValue as String == "K"
        assert tasks.empty
    }
}