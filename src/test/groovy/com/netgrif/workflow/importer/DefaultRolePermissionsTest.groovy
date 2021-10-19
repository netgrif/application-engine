package com.netgrif.workflow.importer

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.importer.service.RoleFactory
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole
import com.netgrif.workflow.petrinet.domain.roles.ProcessRolePermission
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.workflow.petrinet.domain.roles.RolePermission
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.domain.TaskPair
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import groovy.transform.CompileStatic
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@CompileStatic
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

    @Autowired
    private ProcessRoleRepository roleRepository;

    @Autowired
    private RoleFactory roleFactory


    @Value("classpath:role_permissions_default_role_undefined.xml")
    private Resource undefinedDefaultRoleNet

    @Value("classpath:role_permissions_default_role_defined.xml")
    private Resource definedDefaultRoleNet


    private static final String TRANSITION_ID = 't1'

    private String DEFAULT_ROLE_ID

    @Before
    public void before() {
        testHelper.truncateDbs()
        DEFAULT_ROLE_ID = roleRepository.findByName_DefaultValue(ProcessRole.DEFAULT_ROLE).stringId
    }

    @Test
    void undefinedDefaultRole() {
        testPermissions(undefinedDefaultRoleNet, [:], [:], false)
    }

    @Test
    void definedDefaultRole() {
        testPermissions(definedDefaultRoleNet, [
                (DEFAULT_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.DELETE): true,
                        (ProcessRolePermission.CREATE): true,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (RolePermission.PERFORM) : true,
                        (RolePermission.DELEGATE): true
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, true)
    }

    private void testPermissions(Resource model, Map<String, Map<ProcessRolePermission, Boolean>> processPermissions, Map<String, Map<RolePermission, Boolean>> taskPermissions, boolean defaultRoleEnabled) {
        NetCaseTask instances = importAndCreate(model)

        def processPerms = processPermissions.collectEntries { it -> [it.key, it.value.collectEntries {ti -> [ti.key.toString(), ti.value]}]}
        def taskPerms = taskPermissions.collectEntries { it -> [it.key, it.value.collectEntries {ti -> [ti.key.toString(), ti.value]}]}


        assert instances.net.isDefaultRoleEnabled() == defaultRoleEnabled
        assert instances.net.getPermissions() == processPerms

        assert instances.aCase.getPermissions() == processPerms

        assert instances.task.getRoles() == taskPerms
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
