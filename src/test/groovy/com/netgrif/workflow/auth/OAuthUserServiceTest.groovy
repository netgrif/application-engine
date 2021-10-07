package com.netgrif.workflow.auth

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.IUser
import com.netgrif.workflow.auth.domain.LoggedUser
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.configuration.properties.NaeOAuthProperties
import com.netgrif.workflow.oauth.domain.OAuthLoggedUser
import com.netgrif.workflow.oauth.domain.RemoteGroupResource
import com.netgrif.workflow.oauth.domain.RemoteUserResource
import com.netgrif.workflow.oauth.domain.repositories.OAuthUserRepository
import com.netgrif.workflow.oauth.service.OAuthUserService
import com.netgrif.workflow.oauth.service.interfaces.IOAuthUserService
import com.netgrif.workflow.oauth.service.interfaces.IRemoteGroupResourceService
import com.netgrif.workflow.oauth.service.interfaces.IRemoteUserResourceService
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.utils.FullPageRequest
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.hateoas.MediaTypes
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@AutoConfigureMockMvc
@SpringBootTest(properties = [
        "nae.oauth.enabled=true",
        "nae.oauth.remote-user-base=true"])
class OAuthUserServiceTest {

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
    protected OAuthUserRepository repository

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private ProcessRoleRepository roleRepository

    @Autowired
    private TestHelper testHelper

    @Autowired
    private MockMvc mvc;

    private LoggedUser fakeLogged

    private Authentication auth

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        fakeLogged = new OAuthLoggedUser("4", new ObjectId().toString(), "super@netgrif.com", superCreator.superUser.authorities)
        fakeLogged.processRoles = superCreator.superUser.processRoles.collect { it.stringId } as Set
        fakeLogged.setFullName("Fake User")
        auth = new UsernamePasswordAuthenticationToken(fakeLogged, "n/a", fakeLogged.authorities)

        IUser fake = userService.resolveById("4", true)
        fake.processRoles = superCreator.superUser.processRoles
        fake.authorities = superCreator.superUser.authorities
        userService.save(fake)
    }

    @TestConfiguration
    static class TestConfig {

        static class TestUser implements RemoteUserResource {

            String username
            String id
            String email
            String firstName
            String lastName

            TestUser(String username, String id, String email, String firstName, String lastName) {
                this.username = username
                this.id = id
                this.email = email
                this.firstName = firstName
                this.lastName = lastName
            }
        }

        static class TestGroup implements RemoteGroupResource {

            String name
            String id

            TestGroup(String name, String id) {
                this.name = name
                this.id = id
            }
        }

        @Bean
        @Primary
        IRemoteGroupResourceService remoteGroupResourceService() {
            return new IRemoteGroupResourceService() {
                Page listGroups(Pageable pageable) {
                    return null
                }

                Page searchGroups(String searchString, Pageable pageable, boolean small) {
                    return null
                }

                long countGroups() {
                    return 0
                }

                long countGroups(String searchString) {
                    return 0
                }

                RemoteGroupResource find(String id) {
                    return null
                }

                List members(String id) {
                    return []
                }

                List groupsOfUser(String id) {
                    return [new TestGroup("Group", "1")]
                }
            }
        }

        @Bean
        @Primary
        IRemoteUserResourceService remoteUserResourceService() {
            return new IRemoteUserResourceService() {
                LinkedList<RemoteUserResource> users = [
                        new TestUser("test1@netgrif.com", "1", "test1@netgrif.com", "Test1", "TEST-1"),
                        new TestUser("test2@netgrif.com", "2", "test2@netgrif.com", "Test2", "TEST-2"),
                        new TestUser("test3@netgrif.com", "3", "test3@netgrif.com", "Test3", "TEST-3"),
                        new TestUser("system-user", "4", "super@netgrif.com", "Admin", "Netgrif"),
                ]

                Page listUsers(Pageable pageable) {
                    return new PageImpl(users, pageable, users.size())
                }

                Page searchUsers(String searchString, Pageable pageable, boolean small) {
                    List<RemoteUserResource> found = users.findAll { it.email.contains(searchString) } as List<RemoteUserResource>
                    return new PageImpl(found, pageable, found.size())
                }

                long countUsers() {
                    return users.size()
                }

                long countUsers(String searchString) {
                    return users.size()
                }

                RemoteUserResource findUserByUsername(String username) {
                    return users.find { it.username == username }
                }

                RemoteUserResource findUser(String id) {
                    return users.find { it.id == id }
                }

                @Override
                List findUsers(Set ids) {
                    return users.findAll { ids.contains(it.getId()) }
                }

                RemoteUserResource findUserByEmail(String email) {
                    return users.find { it.email == email }
                }
            }
        }
    }


    @Test
    void testSuperUserFindByUsername() {
        assert userService instanceof IOAuthUserService
        def userResource = remoteUserResourceService.findUserByUsername("system-user")
        IUser remoteUser = (userService as IOAuthUserService).findByUsername("system-user")

        assert remoteUser.stringId != null && remoteUser.stringId == userResource.id
        assert remoteUser.name != null && remoteUser.name == userResource.firstName
        assert remoteUser.surname != null && remoteUser.surname == userResource.lastName
        assert remoteUser.email != null && remoteUser.email == userResource.email
    }

    @Test
    void testSaveNew() {
        IUser user = (userService as IOAuthUserService).findByUsername("system-user")
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
                .andExpect(MockMvcResultMatchers.jsonPath('$.email').value(fakeLogged.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath('$.authorities').isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath('$.processRoles').isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath('$.remoteGroups[0].name').value("Group"))
                .andExpect(MockMvcResultMatchers.jsonPath('$.id').value(fakeLogged.getId()))
                .andReturn()

        mvc.perform(get("/api/user/me?small=true")
                .accept(MediaTypes.HAL_JSON_VALUE)
                .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath('$.email').value(fakeLogged.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath('$.authorities').isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath('$.processRoles').isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath('$.id').value(fakeLogged.getId()))
                .andReturn()
    }

    @Test
    @DisplayName("Paged User Resource")
//    @WithMockUser(username = "YourUsername", password = "YourPassword", roles = "USER")
    void testPagedUserResource() {
        mvc.perform(get("/api/user?small=false")
                .accept(MediaTypes.HAL_JSON_VALUE)
                .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath('$._embedded.users[0].id').value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath('$._embedded.users[0].name').value("Test1"))
                .andExpect(MockMvcResultMatchers.jsonPath('$._embedded.users[0].surname').value("TEST-1"))
                .andExpect(MockMvcResultMatchers.jsonPath('$._embedded.users[0].processRoles').exists())
                .andExpect(MockMvcResultMatchers.jsonPath('$._embedded.users[0].authorities').exists())
    }

    @Test
    void testTransformToLoggedUser() {
        IUser user = (userService as OAuthUserService).findByUsername("system-user")
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

    @Test
    @Disabled("expected = IllegalArgumentException.class")
    void testResolveByIdThrow() {
        userService.resolveById("FAKE_ID", false)
    }

    @Test
    void testResolveById() {
        RemoteUserResource resource = remoteUserResourceService.findUser("1")
        IUser user = userService.resolveById(resource.id, false)
        assert user.stringId == resource.id
        assert user.surname == resource.lastName
        assert user.name == resource.firstName
        assert user.email == resource.email
        assert repository.findByOauthId(user.stringId) != null
    }

    @Test
    void testUserSearch() {
        Page<IUser> users = userService.searchAllCoMembers("super", [], [], userService.loggedOrSystem.transformToLoggedUser(), false, new FullPageRequest())
        assert !users.content.isEmpty()
        assert users[0].stringId == "4"
        assert users[0].name == "Admin"
        assert users[0].surname == "Netgrif"
        assert users[0].email == "super@netgrif.com"
    }

    @Test
    @DisplayName("ROLE Test")
    void testSearchAllCoMembersWithRoles() {
        ProcessRole role1 = new ProcessRole()
        role1.setName("Role 1")
        role1 = roleRepository.save(role1)

        ProcessRole role2 = new ProcessRole()
        role2.setName("Role 2")
        role2 = roleRepository.save(role2)

        ProcessRole role3 = new ProcessRole()
        role3.setName("Role 3")
        role3 = roleRepository.save(role3)

        IUser user1 = userService.resolveById("1", true)
        IUser user2 = userService.resolveById("2", true)
        user1.addProcessRole(role1)
        user1.addProcessRole(role2)
        user2.addProcessRole(role2)
        user2.addProcessRole(role3)
        userService.save(user1)
        userService.save(user2)

        List<IUser> foundUsers = userService.searchAllCoMembers("", [role2._id], [role3._id], superCreator.loggedSuper, true, PageRequest.of(0, 20)).content
        assert foundUsers.size() == 1 && foundUsers[0].stringId == user1.stringId

        foundUsers = userService.searchAllCoMembers(user1.getEmail(), [role2._id], [role3._id], superCreator.loggedSuper, true, PageRequest.of(0, 20)).content
        assert foundUsers.size() == 1 && foundUsers[0].stringId == user1.stringId

        foundUsers = userService.searchAllCoMembers("", [], [], superCreator.loggedSuper, true, PageRequest.of(0, 20)).content
        assert foundUsers.any { it.stringId == user1.stringId }
        assert foundUsers.any { it.stringId == user2.stringId }
        assert foundUsers.any { it.stringId == "3"}
        assert foundUsers.any { it.stringId == "4" }

        foundUsers = userService.searchAllCoMembers("", [], [role3._id], superCreator.loggedSuper, true, PageRequest.of(0, 20)).content
        assert foundUsers.size() == 3 && foundUsers[0].stringId == user1.stringId
        assert !foundUsers.any { it.stringId == user2.stringId }
    }

}
