package com.netgrif.application.engine.event

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.authorization.domain.ProcessRole
import com.netgrif.application.engine.utils.UniqueKeyMapWrapper
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
    private UniqueKeyMapWrapper<ProcessRole> roles

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
        TestHelper.login(superCreator.superIdentity)
        def _case = importHelper.createCase("case", net)
        assert _case.dataSet.get("newVariable_1").rawValue == "value"
    }

    @Test
    void roleActionsTest() {
        userService.metaClass.groovyShellTestMethod = { String string, I18nString i18nString -> println("groovyShellTestMethod") }

        def userOpt = userService.findById(superCreator.superIdentity.mainActorId)
        assert userOpt.isPresent()
//        def roleCount = actor.roles.size()
        def roles = roleService.findAll()
        def roleId = "newRole_1"
        def role = roles.find {it.importId == roleId}
        assert role != null
        roleService.assignRolesToActor(userOpt.get().stringId, [role.stringId] as Set)
        // todo: release/8.0.0 some assertions to check shell?
//        actor = userService.findByEmail(userService.getSystem().getEmail())
//        assert actor.roles.size() == roleCount + 1
    }

    @Test
    void fieldActionsTest() {
        TestHelper.login(superCreator.superIdentity)
        def _case = importHelper.createCase("case", net)
        importHelper.assignTaskToSuper("task", _case.getStringId())
        def task = taskService.searchOne(QTask.task.transitionId.eq("t1") & QTask.task.caseId.eq(_case.stringId))
        assert task != null
        assert task.assigneeId != null
    }
}
