package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.authentication.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.TaskField
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class TaskRefInitTest {

    @Autowired
    private ITaskService taskService

    @Autowired
    private ImportHelper helper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IUserService userService

    @Autowired
    private TestHelper testHelper

    Process net = null
    Process autoTrigger = null

    @BeforeEach
    void initNet() {
        testHelper.truncateDbs()
        net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/taskref_init.xml"), VersionType.MAJOR, userService.loggedOrSystem.transformToLoggedUser()).getNet()
        autoTrigger = petriNetService.importPetriNet(new FileInputStream("src/test/resources/autotrigger_taskref.xml"), VersionType.MAJOR, userService.loggedOrSystem.transformToLoggedUser()).getNet()
        assert net != null
    }

    @Test
    void testInitValue() {
        Case aCase = helper.createCase("Test task ref init", net)

        Task task1 = taskService.searchOne(QTask.task.caseId.eq(aCase.stringId) & QTask.task.transitionId.eq("t1"))
        Task task2 = taskService.searchOne(QTask.task.caseId.eq(aCase.stringId) & QTask.task.transitionId.eq("t2"))
        Task task3 = taskService.searchOne(QTask.task.caseId.eq(aCase.stringId) & QTask.task.transitionId.eq("t3"))

        List<String> taskref_0_values = ((TaskField) aCase.dataSet.get("taskRef_0")).rawValue
        List<String> taskref_1_values = ((TaskField) aCase.dataSet.get("taskRef_1")).rawValue
        List<String> taskref_2_values = ((TaskField) aCase.dataSet.get("taskRef_2")).rawValue
        List<String> taskref_3_values = ((TaskField) aCase.dataSet.get("taskRef_3")).rawValue

        assert taskref_0_values.containsAll([task1.stringId, task3.stringId]) && taskref_0_values.size() == 2
        assert taskref_1_values.containsAll([task2.stringId]) && taskref_1_values.size() == 1
        assert taskref_2_values.containsAll([task1.stringId, task2.stringId]) && taskref_2_values.size() == 2
        assert taskref_3_values == null
    }

    @Test
    void autoTriggerTaskRef() {
        Case bCase = helper.createCase("Task ref init with auto trigger", autoTrigger)

        String taskId = bCase.getTaskStringId("t1")
        List<String> value = bCase.dataSet.get("tema").rawValue as List<String>
        assert value.contains(taskId) &&
                value.size() == 1
    }
}
