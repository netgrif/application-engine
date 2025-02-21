package com.netgrif.application.engine.importer

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.importer.service.AllDataConfiguration
import com.netgrif.application.engine.importer.service.PermissionFactory
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission
import com.netgrif.application.engine.authorization.domain.permissions.TaskPermission
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.authorization.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.TaskPair
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.transform.CompileStatic
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
@CompileStatic
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
    private IProcessRoleService roleService

    @Autowired
    private PermissionFactory roleFactory

    @Autowired
    private AllDataConfiguration configuration

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
        assert roleService.defaultRole() != null
        DEFAULT_ROLE_ID = roleService.defaultRole().stringId
        assert DEFAULT_ROLE_ID != null
        assert roleService.anonymousRole() != null
        ANONYMOUS_ROLE_ID = roleService.anonymousRole().stringId
        assert ANONYMOUS_ROLE_ID != null
    }

    //    DEFAULT ROLE =================================
    @Test
    void definedDefaultRole() {
        testPermissions(definedDefaultRoleNet, [
                (DEFAULT_ROLE_ID): [
                        (CasePermission.VIEW)  : true,
                        (CasePermission.DELETE): true,
                        (CasePermission.CREATE): true,
                ]
        ] as Map<String, Map<CasePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (TaskPermission.ASSIGN)  : true,
                        (TaskPermission.CANCEL)  : true,
                        (TaskPermission.FINISH)  : true,
                        (TaskPermission.VIEW)    : true,
//                        TODO: release/8.0.0
//                        (RolePermission.SET)     : true,
//                        (TaskPermission.DELEGATE): true
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, true, false)
    }

    @Test
    void shadowDefaultRole() {
        testPermissions(shadowedDefaultRoleNet, [
                (NET_ROLE_ID): [
                        (CasePermission.VIEW)  : true,
                        (CasePermission.DELETE): true,
                ]
        ] as Map<String, Map<CasePermission, Boolean>>, [
                (NET_ROLE_ID): [
                        (TaskPermission.VIEW)    : true,
//                        (TaskPermission.DELEGATE): true,
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, true, false)
    }

    @Test
    void customDefaultRole() {
        testPermissions(customDefaultRoleNet, [
                (DEFAULT_ROLE_ID): [
                        (CasePermission.VIEW)  : true,
                        (CasePermission.DELETE): true,
                ]
        ] as Map<String, Map<CasePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (TaskPermission.VIEW)    : true,
//                        (TaskPermission.DELEGATE): true,
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, true, false)
    }

    @Test
    void negativeDefaultRole() {
        testPermissions(negativeDefaultRoleNet, [
                (DEFAULT_ROLE_ID): [
                        (CasePermission.VIEW)  : false,
                        (CasePermission.DELETE): false,
                ]
        ] as Map<String, Map<CasePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (TaskPermission.VIEW)    : false,
//                        (TaskPermission.DELEGATE): false,
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, true, false)
    }

    @Test
    void combinedDefaultRole() {
        testPermissions(combinedDefaultRoleNet, [
                (DEFAULT_ROLE_ID): [
                        (CasePermission.VIEW)  : true,
                        (CasePermission.DELETE): true,
                ],
                (NET_ROLE_ID)    : [
                        (CasePermission.VIEW)  : false,
                        (CasePermission.DELETE): false,
                ]
        ] as Map<String, Map<CasePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (TaskPermission.VIEW)    : true,
//                        (TaskPermission.DELEGATE): true,
                ],
                (NET_ROLE_ID)    : [
                        (TaskPermission.VIEW)    : false,
//                        (TaskPermission.DELEGATE): false,
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, true, false)
    }

    @Test()
    void missingDefaultRole() {
        testPermissions(missingDefaultRoleNet, [:] as Map<String, Map<CasePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (TaskPermission.VIEW)    : true,
//                        (TaskPermission.DELEGATE): true,
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, false, false)
    }

    @Test()
    void reservedDefaultRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            importAndCreate(reservedDefaultRoleNet)
        });
    }

    @Test()
    void defaultShadowedByUserRef() {
        testPermissions(shadowedUserRefDefaultRoleNet, [:] as Map<String, Map<CasePermission, Boolean>>, [:] as Map<String, Map<TaskPermission, Boolean>>, true, false)
    }

    @Test
    void disabledReferencedDefaultRole() {
        testPermissions(disabledReferencedDefaultRoleNet, [
                (DEFAULT_ROLE_ID): [
                        (CasePermission.VIEW)  : true,
                        (CasePermission.DELETE): true,
                ]
        ] as Map<String, Map<CasePermission, Boolean>>, [
                (DEFAULT_ROLE_ID): [
                        (TaskPermission.VIEW)    : true,
//                        (TaskPermission.DELEGATE): true,
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, false, false)
    }

    @Test()
    void defaultShadowedByUsersRef() {
        testPermissions(shadowedUsersRefDefaultRoleNet, [:] as Map<String, Map<CasePermission, Boolean>>, [:] as Map<String, Map<TaskPermission, Boolean>>, true, false)
    }

    //    ANONYMOUS ROLE =================================
    @Test
    void definedAnonymousRole() {
        testPermissions(definedAnonymousRoleNet, [
                (ANONYMOUS_ROLE_ID): [
                        (CasePermission.VIEW)  : true,
                        (CasePermission.CREATE): true,
                ]
        ] as Map<String, Map<CasePermission, Boolean>>, [
                (ANONYMOUS_ROLE_ID): [
                        (TaskPermission.ASSIGN): true,
                        (TaskPermission.CANCEL): true,
                        (TaskPermission.FINISH): true,
                        (TaskPermission.VIEW)  : true,
//                        TODO: release/8.0.0
//                        (RolePermission.SET)   : true,
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, false, true)
    }

    @Test
    void shadowAnonymousRole() {
        testPermissions(shadowedAnonymousRoleNet, [
                (NET_ROLE_ID): [
                        (CasePermission.VIEW)  : true,
                        (CasePermission.DELETE): true,
                ]
        ] as Map<String, Map<CasePermission, Boolean>>, [
                (NET_ROLE_ID): [
                        (TaskPermission.VIEW)    : true,
//                        (TaskPermission.DELEGATE): true,
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, false, true)
    }

    @Test
    void customAnonymousRole() {
        testPermissions(customAnonymousRoleNet, [
                (ANONYMOUS_ROLE_ID): [
                        (CasePermission.VIEW)  : true,
                        (CasePermission.DELETE): true,
                ]
        ] as Map<String, Map<CasePermission, Boolean>>, [
                (ANONYMOUS_ROLE_ID): [
                        (TaskPermission.VIEW)    : true,
//                        (TaskPermission.DELEGATE): true,
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, false, true)
    }

    @Test
    void negativeAnonymousRole() {
        testPermissions(negativeAnonymousRoleNet, [
                (ANONYMOUS_ROLE_ID): [
                        (CasePermission.VIEW)  : false,
                        (CasePermission.DELETE): false,
                ]
        ] as Map<String, Map<CasePermission, Boolean>>, [
                (ANONYMOUS_ROLE_ID): [
                        (TaskPermission.VIEW)    : false,
//                        (TaskPermission.DELEGATE): false,
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, false, true)
    }

    @Test
    void combinedAnonymousRole() {
        testPermissions(combinedAnonymousRoleNet, [
                (ANONYMOUS_ROLE_ID): [
                        (CasePermission.VIEW)  : true,
                        (CasePermission.DELETE): true,
                ],
                (NET_ROLE_ID)      : [
                        (CasePermission.VIEW)  : false,
                        (CasePermission.DELETE): false,
                ]
        ] as Map<String, Map<CasePermission, Boolean>>, [
                (ANONYMOUS_ROLE_ID): [
                        (TaskPermission.VIEW)    : true,
//                        (TaskPermission.DELEGATE): true,
                ],
                (NET_ROLE_ID)      : [
                        (TaskPermission.VIEW)    : false,
//                        (TaskPermission.DELEGATE): false,
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, false, true)
    }

    @Test()
    void missingAnonymousRole() {
        testPermissions(missingAnonymousRoleNet, [:] as Map<String, Map<CasePermission, Boolean>>, [
                (ANONYMOUS_ROLE_ID): [
                        (TaskPermission.VIEW)    : true,
//                        (TaskPermission.DELEGATE): true,
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, false, false)
    }

    @Test()
    void reservedAnonymousRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            importAndCreate(reservedAnonymousRoleNet)
        });
    }

    @Test()
    void anonymousShadowedByUserRef() {
        testPermissions(shadowedUserRefAnonymousRoleNet, [:] as Map<String, Map<CasePermission, Boolean>>, [:] as Map<String, Map<TaskPermission, Boolean>>, false, true)
    }

    @Test
    void disabledReferencedAnonymousRole() {
        testPermissions(disabledReferencedAnonymousRoleNet, [
                (ANONYMOUS_ROLE_ID): [
                        (CasePermission.VIEW)  : true,
                        (CasePermission.DELETE): true,
                ]
        ] as Map<String, Map<CasePermission, Boolean>>, [
                (ANONYMOUS_ROLE_ID): [
                        (TaskPermission.VIEW)    : true,
//                        (TaskPermission.DELEGATE): true,
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, false, false)
    }

    @Test()
    void anonymousShadowedByUsersRef() {
        testPermissions(shadowedUsersRefAnonymousRoleNet, [:] as Map<String, Map<CasePermission, Boolean>>, [:] as Map<String, Map<TaskPermission, Boolean>>, false, true)
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
                        (CasePermission.VIEW)  : true,
                        (CasePermission.DELETE): true,
                        (CasePermission.CREATE): true,
                ],
                (ANONYMOUS_ROLE_ID): [
                        (CasePermission.VIEW)  : true,
                        (CasePermission.CREATE): true,
                ]
        ] as Map<String, Map<CasePermission, Boolean>>, [
                (DEFAULT_ROLE_ID)  : [
                        (TaskPermission.ASSIGN)  : true,
                        (TaskPermission.CANCEL)  : true,
                        (TaskPermission.FINISH)  : true,
                        (TaskPermission.VIEW)    : true,
//                        TODO: release/8.0.0
//                        (RolePermission.SET)     : true,
//                        (TaskPermission.DELEGATE): true
                ],
                (ANONYMOUS_ROLE_ID): [
                        (TaskPermission.ASSIGN): true,
                        (TaskPermission.CANCEL): true,
                        (TaskPermission.FINISH): true,
                        (TaskPermission.VIEW)  : true,
//                        TODO: release/8.0.0
//                        (RolePermission.SET)   : true,
                ]
        ] as Map<String, Map<TaskPermission, Boolean>>, true, true)
    }


    private void testPermissions(Resource model, Map<String, Map<CasePermission, Boolean>> processPermissions, Map<String, Map<TaskPermission, Boolean>> taskPermissions, boolean defaultRoleEnabled, boolean anonymousRoleEnabled) {
        NetCaseTask instances = importAndCreate(model)
        // TODO: release/8.0.0 fix
//        String netRoleId = instances.net.getRoles().keySet().find({ it -> it != DEFAULT_ROLE_ID && it != ANONYMOUS_ROLE_ID })
        String netRoleId = ""

        Map<String, Map<CasePermission, Boolean>> processPerms = transformProcessRolePermissionMap(processPermissions, netRoleId);
        Map<String, Map<TaskPermission, Boolean>> taskPerms = transformRolePermissionMap(taskPermissions, netRoleId);

        def negativeProcessView = processPerms.findAll { it -> it.value.containsKey(CasePermission.VIEW) && !it.value.get(CasePermission.VIEW) }.collect { it -> it.key }
        def negativeTaskView = taskPerms.findAll { it -> it.value.containsKey(TaskPermission.VIEW) && !it.value.get(TaskPermission.VIEW) }.collect { it -> it.key }

//        TODO: release/8.0.0
//        assert instances.net.isDefaultRoleEnabled() == defaultRoleEnabled
//        assert instances.net.isAnonymousRoleEnabled() == anonymousRoleEnabled
//        assert instances.net.getPermissions() == processPerms
//        assert instances.net.negativeViewRoles == negativeProcessView
//        assert instances.net.getTransition(TRANSITION_ID).roles == taskPerms
//        assert instances.net.getTransition(TRANSITION_ID).negativeViewRoles == negativeTaskView
//
//        processPerms = processPerms.findAll { it -> it.value.containsKey(ProcessRolePermission.VIEW) || it.value.containsKey(ProcessRolePermission.DELETE) }
//        processPerms.forEach({ k, v -> v.remove(ProcessRolePermission.CREATE) })
//
//        assert instances.aCase.getPermissions() == processPerms
//        assert instances.aCase.negativeViewRoles == negativeProcessView
//
//        assert instances.task.getPermissions() == taskPerms
//        assert instances.task.negativeViewRoles == negativeTaskView
    }

    private NetCaseTask importAndCreate(Resource model) {
        ImportPetriNetEventOutcome importOutcome = petriNetService.importPetriNet(model.inputStream, VersionType.MAJOR, superCreator.loggedSuper)

        assert importOutcome.getNet() != null

        Process net = importOutcome.getNet()

        CreateCaseEventOutcome createCaseOutcome = workflowService.createCase(net.stringId, '', '', superCreator.loggedSuper)
        assert createCaseOutcome.getCase() != null
        Case aCase = createCaseOutcome.getCase()

        assert aCase != null
        assert aCase.getTasks().size() == 2

        List<TaskPair> temp = aCase.getTasks().values() as List<TaskPair>
        Task task = taskService.findOne(temp.find { it.transitionId != configuration.allData.id }.taskStringId)

        assert task != null

        return new NetCaseTask(net, aCase, task)
    }

    // todo 2058 method name
    private Map<String, Map<CasePermission, Boolean>> transformProcessRolePermissionMap(Map<String, Map<CasePermission, Boolean>> input, String netRoleId) {
        return input.collectEntries { it ->
            [it.key == DEFAULT_ROLE_ID || it.key == ANONYMOUS_ROLE_ID ? it.key : netRoleId, it.value.collectEntries { ti -> [ti.key, ti.value] }]
        } as Map<String, Map<CasePermission, Boolean>>
    }

    private Map<String, Map<TaskPermission, Boolean>> transformRolePermissionMap(Map<String, Map<TaskPermission, Boolean>> input, String netRoleId) {
        return input.collectEntries { it ->
            [it.key == DEFAULT_ROLE_ID || it.key == ANONYMOUS_ROLE_ID ? it.key : netRoleId, it.value.collectEntries { ti -> [ti.key, ti.value] }]
        } as Map<String, Map<TaskPermission, Boolean>>
    }

    private class NetCaseTask {
        Process net
        Case aCase
        Task task

        NetCaseTask(Process net, Case aCase, Task task) {
            this.net = net
            this.aCase = aCase
            this.task = task
        }
    }
}
