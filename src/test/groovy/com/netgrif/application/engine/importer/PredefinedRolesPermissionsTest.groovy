package com.netgrif.application.engine.importer

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.importer.service.RoleFactory
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRolePermission
import com.netgrif.application.engine.petrinet.domain.roles.RolePermission
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.TaskPair
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static org.junit.jupiter.api.Assertions.assertThrows

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class PredefinedRolesPermissionsTest {

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


    @Value("classpath:predefinedPermissions/role_permissions_default_role_defined.xml")
    private Resource definedDefaultRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_default_role_shadowed.xml")
    private Resource shadowedDefaultRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_default_role_custom.xml")
    private Resource customDefaultRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_default_role_negative.xml")
    private Resource negativeDefaultRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_default_role_combined.xml")
    private Resource combinedDefaultRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_default_role_missing.xml")
    private Resource missingDefaultRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_default_role_reserved.xml")
    private Resource reservedDefaultRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_default_role_shadowed_userref.xml")
    private Resource shadowedUserRefDefaultRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_default_role_disabled.xml")
    private Resource disabledReferencedDefaultRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_default_role_shadowed_usersref.xml")
    private Resource shadowedUsersRefDefaultRoleNet

    @Value("classpath:predefinedPermissions/role_permissions_anonymous_role_defined.xml")
    private Resource definedAnonymousRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_anonymous_role_shadowed.xml")
    private Resource shadowedAnonymousRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_anonymous_role_custom.xml")
    private Resource customAnonymousRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_anonymous_role_negative.xml")
    private Resource negativeAnonymousRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_anonymous_role_combined.xml")
    private Resource combinedAnonymousRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_anonymous_role_missing.xml")
    private Resource missingAnonymousRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_anonymous_role_reserved.xml")
    private Resource reservedAnonymousRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_anonymous_role_shadowed_userref.xml")
    private Resource shadowedUserRefAnonymousRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_anonymous_role_disabled.xml")
    private Resource disabledReferencedAnonymousRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_anonymous_role_shadowed_usersref.xml")
    private Resource shadowedUsersRefAnonymousRoleNet

    @Value("classpath:predefinedPermissions/role_permissions_combined_roles_undefined.xml")
    private Resource undefinedCombinedRoleNet
    @Value("classpath:predefinedPermissions/role_permissions_combined_roles_defined.xml")
    private Resource definedCombinedRoleNet


    private static final String TRANSITION_ID = 't1'
    private static final String NET_ROLE_ID = 'netRole'
    private String DEFAULT_ROLE_ID
    private String ANONYMOUS_ROLE_ID

    @BeforeEach
    public void before() {
        testHelper.truncateDbs()
        assert processRoleService.defaultRole() != null
        DEFAULT_ROLE_ID = processRoleService.defaultRole().stringId
        assert DEFAULT_ROLE_ID != null
        assert processRoleService.anonymousRole() != null
        ANONYMOUS_ROLE_ID = processRoleService.anonymousRole().stringId
        assert ANONYMOUS_ROLE_ID != null
    }

    //    DEFAULT ROLE =================================
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
                        (RolePermission.ASSIGN)  : true,
                        (RolePermission.CANCEL)  : true,
                        (RolePermission.FINISH)  : true,
                        (RolePermission.VIEW)    : true,
                        (RolePermission.SET)     : true,
                        (RolePermission.DELEGATE): true
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, true, false)
    }

    @Test
    void shadowDefaultRole() {
        testPermissions(shadowedDefaultRoleNet, [
                (NET_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.DELETE): true,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (NET_ROLE_ID): [
                        (RolePermission.VIEW)    : true,
                        (RolePermission.DELEGATE): true,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, true, false)
    }

    @Test
    void customDefaultRole() {
        testPermissions(customDefaultRoleNet, [
                (DEFAULT_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.DELETE): true,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (RolePermission.VIEW)    : true,
                        (RolePermission.DELEGATE): true,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, true, false)
    }

    @Test
    void negativeDefaultRole() {
        testPermissions(negativeDefaultRoleNet, [
                (DEFAULT_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : false,
                        (ProcessRolePermission.DELETE): false,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (RolePermission.VIEW)    : false,
                        (RolePermission.DELEGATE): false,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, true, false)
    }

    @Test
    void combinedDefaultRole() {
        testPermissions(combinedDefaultRoleNet, [
                (DEFAULT_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.DELETE): true,
                ],
                (NET_ROLE_ID)    : [
                        (ProcessRolePermission.VIEW)  : false,
                        (ProcessRolePermission.DELETE): false,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (RolePermission.VIEW)    : true,
                        (RolePermission.DELEGATE): true,
                ],
                (NET_ROLE_ID)    : [
                        (RolePermission.VIEW)    : false,
                        (RolePermission.DELEGATE): false,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, true, false)
    }

    @Test()
    void missingDefaultRole() {
        testPermissions(missingDefaultRoleNet, [:] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (RolePermission.VIEW)    : true,
                        (RolePermission.DELEGATE): true,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, false, false)
    }

    @Test()
    void reservedDefaultRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            importAndCreate(reservedDefaultRoleNet)
        });
    }

    @Test()
    void defaultShadowedByUserRef() {
        testPermissions(shadowedUserRefDefaultRoleNet, [:] as Map<String, Map<ProcessRolePermission, Boolean>>, [:] as Map<String, Map<RolePermission, Boolean>>, true, false)
    }

    @Test
    void disabledReferencedDefaultRole() {
        testPermissions(disabledReferencedDefaultRoleNet, [
                (DEFAULT_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.DELETE): true,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (RolePermission.VIEW)    : true,
                        (RolePermission.DELEGATE): true,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, false, false)
    }

    @Test()
    void defaultShadowedByUsersRef() {
        testPermissions(shadowedUsersRefDefaultRoleNet, [:] as Map<String, Map<ProcessRolePermission, Boolean>>, [:] as Map<String, Map<RolePermission, Boolean>>, true, false)
    }

    //    ANONYMOUS ROLE =================================
    @Test
    void definedAnonymousRole() {
        testPermissions(definedAnonymousRoleNet, [
                (ANONYMOUS_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.CREATE): true,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (ANONYMOUS_ROLE_ID): [
                        (RolePermission.ASSIGN): true,
                        (RolePermission.CANCEL): true,
                        (RolePermission.FINISH): true,
                        (RolePermission.VIEW)  : true,
                        (RolePermission.SET)   : true,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, false, true)
    }

    @Test
    void shadowAnonymousRole() {
        testPermissions(shadowedAnonymousRoleNet, [
                (NET_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.DELETE): true,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (NET_ROLE_ID): [
                        (RolePermission.VIEW)    : true,
                        (RolePermission.DELEGATE): true,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, false, true)
    }

    @Test
    void customAnonymousRole() {
        testPermissions(customAnonymousRoleNet, [
                (ANONYMOUS_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.DELETE): true,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (ANONYMOUS_ROLE_ID): [
                        (RolePermission.VIEW)    : true,
                        (RolePermission.DELEGATE): true,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, false, true)
    }

    @Test
    void negativeAnonymousRole() {
        testPermissions(negativeAnonymousRoleNet, [
                (ANONYMOUS_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : false,
                        (ProcessRolePermission.DELETE): false,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (ANONYMOUS_ROLE_ID): [
                        (RolePermission.VIEW)    : false,
                        (RolePermission.DELEGATE): false,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, false, true)
    }

    @Test
    void combinedAnonymousRole() {
        testPermissions(combinedAnonymousRoleNet, [
                (ANONYMOUS_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.DELETE): true,
                ],
                (NET_ROLE_ID)      : [
                        (ProcessRolePermission.VIEW)  : false,
                        (ProcessRolePermission.DELETE): false,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (ANONYMOUS_ROLE_ID): [
                        (RolePermission.VIEW)    : true,
                        (RolePermission.DELEGATE): true,
                ],
                (NET_ROLE_ID)      : [
                        (RolePermission.VIEW)    : false,
                        (RolePermission.DELEGATE): false,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, false, true)
    }

    @Test()
    void missingAnonymousRole() {
        testPermissions(missingAnonymousRoleNet, [:] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (ANONYMOUS_ROLE_ID): [
                        (RolePermission.VIEW)    : true,
                        (RolePermission.DELEGATE): true,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, false, false)
    }

    @Test()
    void reservedAnonymousRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            importAndCreate(reservedAnonymousRoleNet)
        });
    }

    @Test()
    void anonymousShadowedByUserRef() {
        testPermissions(shadowedUserRefAnonymousRoleNet, [:] as Map<String, Map<ProcessRolePermission, Boolean>>, [:] as Map<String, Map<RolePermission, Boolean>>, false, true)
    }

    @Test
    void disabledReferencedAnonymousRole() {
        testPermissions(disabledReferencedAnonymousRoleNet, [
                (ANONYMOUS_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.DELETE): true,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (ANONYMOUS_ROLE_ID): [
                        (RolePermission.VIEW)    : true,
                        (RolePermission.DELEGATE): true,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, false, false)
    }

    @Test()
    void anonymousShadowedByUsersRef() {
        testPermissions(shadowedUsersRefAnonymousRoleNet, [:] as Map<String, Map<ProcessRolePermission, Boolean>>, [:] as Map<String, Map<RolePermission, Boolean>>, false, true)
    }

    // COMBINED ROLES ======================================
    @Test
    void undefinedCombinedRole() {
        testPermissions(undefinedCombinedRoleNet, [:], [:], false, false)
    }

    @Test
    void definedCombinedRole() {
        testPermissions(definedCombinedRoleNet, [
                (DEFAULT_ROLE_ID)  : [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.DELETE): true,
                        (ProcessRolePermission.CREATE): true,
                ],
                (ANONYMOUS_ROLE_ID): [
                        (ProcessRolePermission.VIEW)  : true,
                        (ProcessRolePermission.CREATE): true,
                ]
        ] as Map<String, Map<ProcessRolePermission, Boolean>>, [
                (DEFAULT_ROLE_ID)  : [
                        (RolePermission.ASSIGN)  : true,
                        (RolePermission.CANCEL)  : true,
                        (RolePermission.FINISH)  : true,
                        (RolePermission.VIEW)    : true,
                        (RolePermission.SET)     : true,
                        (RolePermission.DELEGATE): true
                ],
                (ANONYMOUS_ROLE_ID): [
                        (RolePermission.ASSIGN): true,
                        (RolePermission.CANCEL): true,
                        (RolePermission.FINISH): true,
                        (RolePermission.VIEW)  : true,
                        (RolePermission.SET)   : true,
                ]
        ] as Map<String, Map<RolePermission, Boolean>>, true, true)
    }


    private void testPermissions(Resource model, Map<String, Map<ProcessRolePermission, Boolean>> processPermissions, Map<String, Map<RolePermission, Boolean>> taskPermissions, boolean defaultRoleEnabled, boolean anonymousRoleEnabled) {
        NetCaseTask instances = importAndCreate(model)
        String netRoleId = instances.net.getRoles().keySet().find({ it -> it != DEFAULT_ROLE_ID && it != ANONYMOUS_ROLE_ID })

        Map<String, Map<String, Boolean>> processPerms = transformPermissionMap(processPermissions, netRoleId);
        Map<String, Map<String, Boolean>> taskPerms = transformPermissionMap(taskPermissions, netRoleId);

        def negativeProcessView = processPerms.findAll { it -> it.value.containsKey("view") && !it.value.get("view") }.collect { it -> it.key }
        def negativeTaskView = taskPerms.findAll { it -> it.value.containsKey("view") && !it.value.get("view") }.collect { it -> it.key }

        assert instances.net.isDefaultRoleEnabled() == defaultRoleEnabled
        assert instances.net.isAnonymousRoleEnabled() == anonymousRoleEnabled
        assert instances.net.getPermissions() == processPerms
        assert instances.net.negativeViewRoles == negativeProcessView
        assert instances.net.getTransition(TRANSITION_ID).roles == taskPerms
        assert instances.net.getTransition(TRANSITION_ID).negativeViewRoles == negativeTaskView

        processPerms = processPerms.findAll { it -> it.value.containsKey("view") || it.value.containsKey("delete") }
        processPerms.forEach({ k, v -> v.remove("create") })

        assert instances.aCase.getPermissions() == processPerms
        assert instances.aCase.negativeViewRoles == negativeProcessView

        assert instances.task.getRoles() == taskPerms
        assert instances.task.negativeViewRoles == negativeTaskView
    }

    private NetCaseTask importAndCreate(Resource model) {
        ImportPetriNetEventOutcome importOutcome = petriNetService.importPetriNet(model.inputStream, VersionType.MAJOR, superCreator.loggedSuper)

        assert importOutcome.getNet() != null

        PetriNet net = importOutcome.getNet()

        CreateCaseEventOutcome createCaseOutcome = workflowService.createCase(net.stringId, '', '', superCreator.loggedSuper)
        assert createCaseOutcome.getCase() != null
        Case aCase = createCaseOutcome.getCase()

        assert aCase != null
        assert !aCase.getTasks().isEmpty()
        assert aCase.getTasks().size() == 1

        List<TaskPair> temp = new ArrayList<>(aCase.getTasks())
        Task task = taskService.findOne(temp.get(0).task)

        assert task != null

        return new NetCaseTask(net, aCase, task)
    }

    private Map<String, Map<String, Boolean>> transformPermissionMap(Map<String, Map<Object, Boolean>> input, String netRoleId) {
        return input.collectEntries { it -> [it.key == DEFAULT_ROLE_ID || it.key == ANONYMOUS_ROLE_ID ? it.key : netRoleId, it.value.collectEntries { ti -> [ti.key.toString(), ti.value] }] } as Map<String, Map<String, Boolean>>
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
