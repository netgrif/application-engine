package com.netgrif.workflow.importer

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.importer.service.RoleFactory
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.roles.ProcessRolePermission
import com.netgrif.workflow.petrinet.domain.roles.RolePermission
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.domain.TaskPair
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
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

    @Autowired
    private IProcessRoleService processRoleService

    @Autowired
    private RoleFactory roleFactory


    @Value("classpath:role_permissions_default_role_undefined.xml")
    private Resource undefinedDefaultRoleNet

    @Value("classpath:role_permissions_default_role_defined.xml")
    private Resource definedDefaultRoleNet

    @Value("classpath:role_permissions_default_role_shadowed.xml")
    private Resource shadowedDefaultRoleNet

    @Value("classpath:role_permissions_default_role_custom.xml")
    private Resource customDefaultRoleNet

    @Value("classpath:role_permissions_default_role_negative.xml")
    private Resource negativeDefaultRoleNet

    @Value("classpath:role_permissions_default_role_combined.xml")
    private Resource combinedDefaultRoleNet

    @Value("classpath:role_permissions_default_role_missing.xml")
    private Resource missingDefaultRoleNet


    private static final String TRANSITION_ID = 't1'
    private static final String NET_ROLE_ID = 'netRole'
    private String DEFAULT_ROLE_ID

    @Before
    public void before() {
        testHelper.truncateDbs()
        DEFAULT_ROLE_ID = processRoleService.defaultRole().stringId
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

    @Test
    void shadowDefaultRole() {
        testPermissions(shadowedDefaultRoleNet, [
                (NET_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.DELETE)  : true,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (NET_ROLE_ID): [
                        (RolePermission.VIEW) : true,
                        (RolePermission.DELEGATE) : true,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, true)
    }

    @Test
    void customDefaultRole() {
        testPermissions(customDefaultRoleNet, [
                (DEFAULT_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.DELETE)  : true,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (RolePermission.VIEW) : true,
                        (RolePermission.DELEGATE) : true,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, true)
    }

    @Test
    void negativeDefaultRole() {
        testPermissions(negativeDefaultRoleNet, [
                (DEFAULT_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : false,
                        (ProcessRolePermission.DELETE)  : false,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (RolePermission.VIEW) : false,
                        (RolePermission.DELEGATE) : false,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, true)
    }

    @Test
    void combinedDefaultRole() {
        testPermissions(combinedDefaultRoleNet, [
                (DEFAULT_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.DELETE)  : true,
                ],
                (NET_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : false,
                        (ProcessRolePermission.DELETE)  : false,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (RolePermission.VIEW) : true,
                        (RolePermission.DELEGATE) : true,
                ],
                (NET_ROLE_ID): [
                        (RolePermission.VIEW) : false,
                        (RolePermission.DELEGATE) : false,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, true)
    }

    @Test(expected = IllegalArgumentException.class)
    void missingDefaultRole() {
        importAndCreate(missingDefaultRoleNet)
    }

    private void testPermissions(Resource model, Map<String, Map<ProcessRolePermission, Boolean>> processPermissions, Map<String, Map<RolePermission, Boolean>> taskPermissions, boolean defaultRoleEnabled) {
        NetCaseTask instances = importAndCreate(model)
        String netRoleId = instances.net.getRoles().keySet().find({ it -> it != DEFAULT_ROLE_ID })

        Map<String, Map<String, Boolean>> processPerms = transformPermissionMap(processPermissions, netRoleId);
        Map<String, Map<String, Boolean>> taskPerms = transformPermissionMap(taskPermissions, netRoleId);

        def negativeProcessView = processPerms.findAll {it -> it.value.containsKey("view") && !it.value.get("view") }.collect {it -> it.key}
        def negativeTaskView = taskPerms.findAll {it -> it.value.containsKey("view") && !it.value.get("view") }.collect {it -> it.key}

        assert instances.net.isDefaultRoleEnabled() == defaultRoleEnabled
        assert instances.net.getPermissions() == processPerms
        assert instances.net.negativeViewRoles == negativeProcessView
        assert instances.net.getTransition(TRANSITION_ID).roles == taskPerms
        assert instances.net.getTransition(TRANSITION_ID).negativeViewRoles == negativeTaskView

        processPerms = processPerms.findAll {it -> it.value.containsKey("view") || it.value.containsKey("delete")}
        processPerms.forEach({ k , v  -> v.remove("create")})

        assert instances.aCase.getPermissions() == processPerms
        assert instances.aCase.negativeViewRoles == negativeProcessView

        assert instances.task.getRoles() == taskPerms
        assert instances.task.negativeViewRoles == negativeTaskView
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

    private Map<String, Map<String, Boolean>> transformPermissionMap(Map<String, Map<Object, Boolean>> input, String netRoleId) {
        return input.collectEntries { it -> [it.key == DEFAULT_ROLE_ID ? DEFAULT_ROLE_ID : netRoleId, it.value.collectEntries { ti -> [ti.key.toString(), ti.value]}]} as Map<String, Map<String, Boolean>>
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
