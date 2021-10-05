package com.netgrif.workflow.event

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.LoggedUser
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.petrinet.domain.I18nString
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.QTask
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

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
    private IProcessRoleService roleService

    @Autowired
    private IUserService userService

    private PetriNet net


    @BeforeEach
    void before() {
        testHelper.truncateDbs()

        def testNet = importHelper.createNet(FILE_NAME)
        assert testNet.isPresent()
        net = testNet.get()
    }

    @Test
    void adminConsoleTest() {
        MockMvc mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def auths = importHelper.createAuthorities(["systemAdmin": Authority.systemAdmin])
        importHelper.createUser(new User(name: "Admin", surname: "User", email: USER_EMAIL, password: USER_PASSW, state: UserState.ACTIVE),
                [auths.get("systemAdmin")] as Authority[],
                [] as ProcessRole[])

        def adminAuth = new UsernamePasswordAuthenticationToken(USER_EMAIL, USER_PASSW)
        adminAuth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))

        // call the validation method in ActionDelegate that takes an I18nString as an argument.
        // if I18nString was not imported from GroovyShellConfiguration (application.properties) throws an error
        // if I18nString was imported by a bad class loader throws a "method with signature not found" error
        mvc.perform(post("/api/admin/run")
                .content("validation(\"String\", new I18nString(\"I18nString\"))")
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andDo({
                    assert !it.getResponse().getContentAsString().contains("error")
                })
    }

    @Test
    void caseFieldsExpressionTest() {
        def _case = importHelper.createCase("case", net)
        assert _case.dataSet["newVariable_1"].value == "value"
    }

    @Test
    void roleActionsTest() {
        roleService.metaClass.groovyShellTestMethod = { String string, I18nString i18nString -> println("groovyShellTestMethod") }

        def user = userService.findByEmail(userService.getSystem().getEmail(), false)
        def processRoleCount = user.processRoles.size()
        def roles = roleService.findAll(net.getStringId())
        assert roles.size() == 1
        roleService.assignRolesToUser(
                user.getStringId(),
                new HashSet<String>(roles.collect({it.getStringId()})),
                new LoggedUser(-1, "a", "", [])
        )
        user = userService.findByEmail(userService.getSystem().getEmail(), false)
        assert user.processRoles.size() == processRoleCount + 1
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
