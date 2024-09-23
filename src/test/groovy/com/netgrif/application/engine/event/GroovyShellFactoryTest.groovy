package com.netgrif.application.engine.event

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.LoggedUser
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
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
class GroovyShellFactoryTest extends EngineTest {

    public static final String FILE_NAME = "groovy_shell_test.xml"

    private Process net

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
        assert _case.dataSet.get("newVariable_1").rawValue == "value"
    }

    @Test
    void roleActionsTest() {
        userService.metaClass.groovyShellTestMethod = { String string, I18nString i18nString -> println("groovyShellTestMethod") }

        def user = userService.findByEmail(userService.getSystem().getEmail())
        def processRoleCount = user.processRoles.size()
        def roles = roleService.findAll(net.getStringId())
        def roleIds = ["anonymous", "default", "newRole_1"]
        assert roles.size() == roleIds.size()
        roles.each { assert it.importId in roleIds }
        roleService.assignRolesToUser(
                user.getStringId(),
                new HashSet<String>(roles.collect { it.stringId } + user.processRoles.collect { it.stringId }),
                new LoggedUser("", "a", "", [])
        )
        user = userService.findByEmail(userService.getSystem().getEmail())
        assert user.processRoles.size() == processRoleCount + 1
    }

    @Test
    void fieldActionsTest() {
        def _case = importHelper.createCase("case", net)
        importHelper.assignTaskToSuper("task", _case.getStringId())
        def task = taskService.searchOne(QTask.task.transitionId.eq("t1"))
        assert task != null
        assert task.assigneeId != null
    }

    @Autowired
    GroovyShellFactoryTest(ImportHelper importHelper, WebApplicationContext wac, TestHelper testHelper, ITaskService taskService, IProcessRoleService roleService, IUserService userService) {
        super(importHelper, wac, testHelper, taskService, roleService, userService)
    }
}
