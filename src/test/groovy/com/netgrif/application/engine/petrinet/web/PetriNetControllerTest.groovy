package com.netgrif.application.engine.petrinet.web

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.authentication.domain.IdentityState
import com.netgrif.application.engine.authentication.domain.params.IdentityParams
import com.netgrif.application.engine.authorization.domain.ApplicationRole
import com.netgrif.application.engine.authorization.domain.Role
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.authorization.domain.ProcessRole
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ApplicationRoleRunner
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

//import com.netgrif.application.engine.orgstructure.domain.Group

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class PetriNetControllerTest {

    public static final String NET_FILE = "process_delete_test.xml"

    private static final String DELETE_PROCESS_URL = "/api/petrinet/"

    private static final String USER_EMAIL = "user@test.com"
    private static final String ADMIN_EMAIL = "admin@test.com"

    private MockMvc mvc

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ApplicationRoleRunner applicationRoleRunner

    private Process net

    private Authentication userAuth
    private Authentication adminAuth

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void before() {
        testHelper.truncateDbs()

        def net = petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR,
                superCreator.getLoggedSuper().activeActorId)
        assert net.getNet() != null

        this.net = net.getNet()

        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        importHelper.createIdentity(IdentityParams.with()
                .firstname(new TextField("Role"))
                .lastname(new TextField("Identity"))
                .username(new TextField(USER_EMAIL))
                .password(new TextField("password"))
                .build(), new ArrayList<Role>())

        userAuth = new UsernamePasswordAuthenticationToken(USER_EMAIL, "password")
        userAuth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))

        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE)
        importHelper.createIdentity(IdentityParams.with()
                .firstname(new TextField("Admin"))
                .lastname(new TextField("Identity"))
                .username(new TextField(ADMIN_EMAIL))
                .password(new TextField("password"))
                .build(), List.of(adminAppRole))

        adminAuth = new UsernamePasswordAuthenticationToken(ADMIN_EMAIL, "password")
        adminAuth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))
    }


    @Test
    void testDeleteProcess() {
        mvc.perform(delete(DELETE_PROCESS_URL + net.stringId)
                .with(authentication(this.userAuth)))
                .andExpect(status().isForbidden())

        mvc.perform(delete(DELETE_PROCESS_URL + net.stringId)
                .with(authentication(this.adminAuth)))
                .andExpect(status().isOk())
    }
}
