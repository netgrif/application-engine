package com.netgrif.workflow.auth

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.IUser
import com.netgrif.workflow.auth.domain.LoggedUser
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.configuration.properties.NaeOAuthProperties
import com.netgrif.workflow.oauth.domain.OAuthLoggedUser
import com.netgrif.workflow.oauth.service.OAuthUserService
import com.netgrif.workflow.oauth.service.interfaces.IOAuthUserService
import com.netgrif.workflow.oauth.service.interfaces.IRemoteUserResourceService
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.startup.SystemUserRunner
import com.netgrif.workflow.utils.FullPageRequest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.hateoas.MediaTypes
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@AutoConfigureMockMvc
@SpringBootTest(properties = [
        "nae.oauth.enabled=true",
        "nae.oauth.remote-user-base=true",
        "nae.oauth.keycloak=true"])
class OAuthUserServiceTest {

    public static final String TEST_USER = "bezak@netgrif.com"

    private MockMvc mvc
    private Authentication auth

    @Autowired
    private NaeOAuthProperties oAuthProperties

    @Autowired
    private IUserService userService

    @Autowired
    private IProcessRoleService roleService

    @Autowired
    private IAuthorityService authorityService

    @Autowired
    private IRemoteUserResourceService remoteUserResourceService

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private SystemUserRunner systemUserRunner

    @Autowired
    private TestHelper testHelper

    @Before
    void before() {
        testHelper.truncateDbs()
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()
        auth = new UsernamePasswordAuthenticationToken(superCreator.superUser.transformToLoggedUser(), "", superCreator.superUser.authorities)
    }

    @Test
    void testSuperUserFindByUsername() {
        assert userService instanceof IOAuthUserService
        def userResource = remoteUserResourceService.findUserByUsername(oAuthProperties.superUsername)
        IUser remoteUser = (userService as IOAuthUserService).findByUsername(oAuthProperties.superUsername)

        assert remoteUser.stringId != null && remoteUser.stringId == userResource.id
        assert remoteUser.name != null && remoteUser.name == userResource.firstName
        assert remoteUser.surname != null && remoteUser.surname == userResource.lastName
        assert remoteUser.email != null && remoteUser.email == userResource.email
    }

    @Test
    void testUserSearch() {
        Page<IUser> users = userService.searchAllCoMembers(oAuthProperties.superUsername, [], [], userService.loggedOrSystem.transformToLoggedUser(), false, new FullPageRequest())
        assert !users.content.isEmpty()
        assert users[0].stringId != null
        assert users[0].name != null
        assert users[0].surname != null
        assert users[0].email != null
    }

    @Test
    void testSaveNew() {
        IUser user = (userService as IOAuthUserService).findByUsername(TEST_USER)
        user = userService.saveNew(user)
        assert !user.authorities.empty
        assert user.authorities.any {it.name == authorityService.getOrCreate(Authority.user).name }
        assert user.processRoles.any { it.stringId == roleService.defaultRole().stringId }
        LoggedUser loggedUser = user.transformToLoggedUser()
        assert loggedUser.authorities == user.authorities
        assert loggedUser.processRoles.sort() == user.processRoles.collect { it.stringId }.sort()
        assert loggedUser.fullName == user.getFullName()
        assert loggedUser.id == user.stringId

        IUser found = userService.findById(user.stringId, false)
        assert found.stringId == user.stringId
    }

    @Test
    void testSystem() {
        IUser user = userService.getSystem()
        assert user != null
    }

    @Test
    void testUserResource() {
        mvc.perform(get("/api/user/me?small=false")
                .accept(MediaTypes.HAL_JSON_VALUE)
                .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath('$.email').value((auth.getPrincipal() as LoggedUser).getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath('$.authorities').isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath('$.processRoles').isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath('$.remoteGroups').exists())
                .andExpect(MockMvcResultMatchers.jsonPath('$.id').value(superCreator.superUser.getStringId()))
                .andReturn()

        mvc.perform(get("/api/user/me?small=true")
                .accept(MediaTypes.HAL_JSON_VALUE)
                .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath('$.email').value((auth.getPrincipal() as LoggedUser).getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath('$.authorities').isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath('$.processRoles').isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath('$.id').value(superCreator.superUser.getStringId()))
                .andReturn()
    }

    @Test
    void testPagedUserResource() {
        mvc.perform(get("/api/user?small=false")
                .accept(MediaTypes.HAL_JSON_VALUE)
                .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath('$._embedded.users[0].id').exists())
                .andExpect(MockMvcResultMatchers.jsonPath('$._embedded.users[0].name').exists())
                .andExpect(MockMvcResultMatchers.jsonPath('$._embedded.users[0].surname').exists())
                .andExpect(MockMvcResultMatchers.jsonPath('$._embedded.users[0].processRoles').exists())
                .andExpect(MockMvcResultMatchers.jsonPath('$._embedded.users[0].authorities').exists())
    }

    @Test
    void testTransformToLoggedUser() {
        IUser user = (userService as OAuthUserService).findByUsername(oAuthProperties.superUsername)
        user = userService.resolveById(user.getStringId(), false)
        OAuthLoggedUser loggedUser = user.transformToLoggedUser() as OAuthLoggedUser
        assert loggedUser.id == user.stringId
        assert loggedUser.email == user.email
        assert loggedUser.fullName == user.fullName
        assert loggedUser.authorities.sort() == user.authorities.sort()
        assert loggedUser.processRoles.sort() == user.processRoles.collect { it.stringId }.sort()

        IUser user2 = loggedUser.transformToUser()
        assert user.stringId == user2.stringId
        assert user.email == user2.email
        assert user.fullName == user2.fullName
        assert user.authorities.sort() == user2.authorities.sort()
        assert user.processRoles.collect { it.stringId }.sort() == user2.processRoles.collect { it.stringId }.sort()
    }

}
