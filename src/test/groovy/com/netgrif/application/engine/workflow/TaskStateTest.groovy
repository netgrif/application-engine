package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.importer.service.AllDataConfiguration
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams
import com.netgrif.application.engine.workflow.domain.params.TaskParams
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static com.netgrif.application.engine.workflow.domain.State.DISABLED
import static com.netgrif.application.engine.workflow.domain.State.ENABLED

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class TaskStateTest {

    @Autowired
    private TestHelper testHelper
    @Autowired
    private ImportHelper importHelper
    @Autowired
    private ITaskService taskService
    @Autowired
    private IWorkflowService workflowService
    @Autowired
    private SuperCreator superCreator
    @Autowired
    private AllDataConfiguration allDataConfiguration

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
    }

    @Test
    void testTaskState() {
        def netOptional = importHelper.createNet("NAE-1858_task_state.xml")
        assert netOptional.isPresent()
        PetriNet net = netOptional.get()

        CreateCaseParams createCaseParams = CreateCaseParams.builder()
                .petriNet(net)
                .title("Test Case")
                .color("")
                .loggedUser(superCreator.superUser.transformToLoggedUser())
                .build()
        Case useCase = workflowService.createCase(createCaseParams)?.case
        assert useCase

        List<Task> tasks = taskService.findAllByCase(useCase.stringId)
        tasks.each { task ->
            assert (task.title as String) == "N" ? task.state == DISABLED : task.state == ENABLED
        }
    }

    @Test
    void testTaskState2() {
        def netOptional = importHelper.createNet("NAE-1858_task_state_2.xml")
        assert netOptional.isPresent()
        PetriNet net = netOptional.get()

        CreateCaseParams createCaseParams = CreateCaseParams.builder()
                .petriNet(net)
                .title("Test Case")
                .color("")
                .loggedUser(superCreator.superUser.transformToLoggedUser())
                .build()
        Case useCase = workflowService.createCase(createCaseParams)?.case
        assert useCase

        4.times { index ->
            List<Task> tasks = taskService.findAllByCase(useCase.stringId)
            String transitionId = "t${index + 1}"
            tasks.each { t->
                assert t.transitionId in [transitionId, allDataConfiguration.allData.id] ? t.state == ENABLED : t.state == DISABLED
            }
            Task task = tasks.find {it.transitionId == transitionId}
            taskService.assignTask(new TaskParams(task))
            tasks = taskService.findAllByCase(useCase.stringId)
            tasks.each { t->
                assert t.transitionId in [allDataConfiguration.allData.id] ? t.state == ENABLED : t.state == DISABLED
            }
            taskService.finishTask(new TaskParams(task))
        }
    }
}