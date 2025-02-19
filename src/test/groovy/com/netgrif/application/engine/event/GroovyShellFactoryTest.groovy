package com.netgrif.application.engine.event

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.auth.domain.LoggedUser
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.domain.roles.Role
import com.netgrif.application.engine.utils.UniqueKeyMap
import com.netgrif.application.engine.workflow.domain.QTask
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class GroovyShellFactoryTest extends EngineTest {

    public static final String FILE_NAME = "groovy_shell_test.xml"

    private Process net
    private UniqueKeyMap<String, Role> roles

    @BeforeEach
    @Override
    void before() {
        super.before()

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
        def roleCount = user.roles.size()
        def roles = roleService.findAll()
        def roleId = "newRole_1"
        def role = roles.find {it.importId == roleId}
        assert role != null
        roleService.assignRolesToUser(
                user.getStringId(),
                new HashSet<String>([role.stringId] + user.roles.collect { it.stringId }),
                new LoggedUser("", "a", "", [])
        )
        user = userService.findByEmail(userService.getSystem().getEmail())
        assert user.roles.size() == roleCount + 1
    }

    @Test
    void fieldActionsTest() {
        def _case = importHelper.createCase("case", net)
        importHelper.assignTaskToSuper("task", _case.getStringId())
        def task = taskService.searchOne(QTask.task.transitionId.eq("t1"))
        assert task != null
        assert task.assigneeId != null
    }
}
