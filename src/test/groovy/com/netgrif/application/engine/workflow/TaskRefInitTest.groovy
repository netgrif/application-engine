package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
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

    PetriNet net = null
    PetriNet autoTrigger = null

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
        Task task1 = taskService.searchOne(QTask.task.caseTitle.eq("Test task ref init") & QTask.task.transitionId.eq("t1"))
        Task task2 = taskService.searchOne(QTask.task.caseTitle.eq("Test task ref init") & QTask.task.transitionId.eq("t3"))

        assert ((List<String>) aCase.dataSet.get("taskRef_0").value).containsAll(Arrays.asList(task1.stringId, task2.stringId))
        assert ((List<String>) aCase.dataSet.get("taskRef_1").value).isEmpty()
        assert ((List<String>) aCase.dataSet.get("taskRef_2").value).contains(task1.stringId) & ((List<String>) aCase.dataSet.get("taskRef_2").value).size() == 1
        assert ((List<String>) aCase.dataSet.get("taskRef_3").value).isEmpty()
    }

    @Test
    void autoTriggerTaskRef() {
        Case bCase = helper.createCase("Task ref init with auto trigger", autoTrigger)
        assert ((List<String>) bCase.dataSet["tema"].value).contains(bCase.tasks.stream().filter({ t -> t.transition == "t1" }).findFirst().get().task) && ((List<String>) bCase.dataSet["tema"].value).size() == 1
    }
}
