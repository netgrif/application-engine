package com.netgrif.application.engine.event

import com.netgrif.adapter.auth.domain.LoggedUserImpl
import com.netgrif.application.engine.TestHelper
import com.netgrif.core.auth.domain.LoggedUser
import com.netgrif.adapter.auth.service.UserService
import com.netgrif.core.petrinet.domain.I18nString
import com.netgrif.core.petrinet.domain.PetriNet
import com.netgrif.adapter.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.adapter.workflow.domain.QTask
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.core.workflow.domain.ProcessResourceId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.context.WebApplicationContext

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class GroovyShellFactoryTest {

    private static final String USER_EMAIL = "test@test.com"
    private static final String USER_PASSW = "password"

    public static final String FILE_NAME = "groovy_shell_test.xml"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ITaskService taskService

    @Autowired
    private ProcessRoleService roleService

    @Autowired
    private UserService userService

    private PetriNet net


    @BeforeEach
    void before() {
        testHelper.truncateDbs()

        def testNet = importHelper.createNet(FILE_NAME)
        assert testNet.isPresent()
        net = testNet.get()
    }


    @Test
    void caseFieldsExpressionTest() {
        def _case = importHelper.createCase("case", net)
        assert _case.dataSet["newVariable_1"].value == "value"
    }

    @Test
    void roleActionsTest() {
        roleService.metaClass.groovyShellTestMethod = { String string, I18nString i18nString -> println("groovyShellTestMethod") }

        def user = userService.findUserByUsername(userService.getSystem().getEmail(), null)
        def processRoleCount = user.get().processRoles.size()
        def roles = roleService.findAll(net.getStringId())
        assert roles.size() == 1
        roleService.assignRolesToUser(
                user.get().stringId,
                new HashSet<ProcessResourceId>(roles.collect { it._id } + user.get().processRoles.collect { it._id }),
                new LoggedUserImpl("", "a", [] as Set, [] as Set)
        )
        user = userService.findUserByUsername(userService.getSystem().getEmail(), null)
        assert user.get().processRoles.size() == processRoleCount + 1
    }

    @Test
    void fieldActionsTest() {
        def _case = importHelper.createCase("case", net)
        importHelper.assignTaskToSuper("task", _case.getStringId())
        def task = taskService.searchOne(QTask.task.transitionId.eq("t1"))
        assert task != null
        assert task.getUserId() != null
    }
}
