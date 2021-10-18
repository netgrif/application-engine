package com.netgrif.workflow.importer

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.domain.TaskPair
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import com.netgrif.workflow.workflow.web.responsebodies.TaskReference
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.core.io.Resource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class DefaultRolePermissionsTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private ITaskService taskService

    @Value("classpath:role_permissions_default_role_undefined.xml")
    private Resource undefinedDefaultRoleNet

    private static final String TRANSITION_ID = 't1'

    @Before
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    void undefinedDefaultRole() {
        NetCaseTask instances = importAndCreate(undefinedDefaultRoleNet)

        assert instances.net.isDefaultRoleEnabled() == false
        assert instances.net.getPermissions().isEmpty()
        assert instances.net.getTransition(TRANSITION_ID).getRoles().isEmpty()

        assert instances.aCase.getPermissions().isEmpty()

        assert instances.task.getRoles().isEmpty()
    }


    private NetCaseTask importAndCreate(Resource model) {
        Optional<PetriNet> optNet = petriNetService.importPetriNet(model.inputStream, VersionType.MAJOR, superCreator.loggedSuper)

        assert optNet.isPresent()

        Case aCase = workflowService.createCase(optNet.get().stringId, '', '', superCreator.loggedSuper)

        assert aCase != null
        assert !aCase.getTasks().isEmpty()
        assert aCase.getTasks().size() == 1

        List<TaskPair> temp = new ArrayList<>(aCase.getTasks())
        Task task = taskService.findOne(temp.get(0).task)

        assert task != null

        return new NetCaseTask(optNet.get(), aCase, task)
    }

    private class NetCaseTask {
        PetriNet net
        Case aCase
        Task task

        NetCaseTask(PetriNet net, Case aCase, Task task) {
            this.net = net
            this.aCase = aCase
            this.task = task
        }
    }
}
