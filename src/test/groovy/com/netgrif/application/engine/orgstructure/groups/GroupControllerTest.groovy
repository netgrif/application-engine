package com.netgrif.application.engine.orgstructure.groups

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.AuthorizingObject
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.startup.ImportHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class GroupControllerTest {

    private MockMvc mvc

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IUserService userService

    Authentication userAuth

    private static final String USER_EMAIL = "test@nae.com"

    private static final String GROUP_VIEW_MY_URL = "/api/group/my"
    private static final String MEMBERSHIPS_MY_URL = "/api/group/membership"


    @BeforeEach
    void init() {
        testHelper.truncateDbs()

        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def auths = importHelper.createAuthorities(["user": [
                AuthorizingObject.GROUP_VIEW_OWN.name(),
                AuthorizingObject.GROUP_MEMBERSHIP_SELF.name()
        ]])

        importHelper.createUser(new User(name: "Role", surname: "User", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
                auths.get("user").toArray() as Authority[], [] as ProcessRole[])
        userAuth = new UsernamePasswordAuthenticationToken(userService.findByEmail(USER_EMAIL, false).transformToLoggedUser(), "password", auths.get("user"))
    }

    @Test
    void testViewMyGroups() {
        mvc.perform(get(GROUP_VIEW_MY_URL)
                .with(authentication(this.userAuth)))
                .andExpect(status().isOk())
    }

    @Test
    void testMyMemberships() {
        mvc.perform(get(MEMBERSHIPS_MY_URL)
                .with(authentication(this.userAuth)))
                .andExpect(status().isOk())
    }
}
