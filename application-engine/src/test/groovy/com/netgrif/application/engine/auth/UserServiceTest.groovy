package com.netgrif.application.engine.auth

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.auth.service.AuthorityService
import com.netgrif.application.engine.auth.service.GroupService
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.objects.auth.domain.Authority
import com.netgrif.application.engine.objects.auth.domain.User
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.startup.ImportHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.time.LocalDateTime

import static org.junit.jupiter.api.Assertions.assertNull

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class UserServiceTest {

    @Autowired
    private TestHelper helper

    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private UserService userService

    @Autowired
    private GroupService groupService

    @Autowired
    private ProcessRoleService processRoleService;

    @Autowired
    private ImportHelper importHelper

    private PetriNet petriNet

    private ProcessRole dummyRole

    private Authority userAuth

    private AbstractUser user

    @BeforeEach
    void init() {
        helper.truncateDbs()

        Optional<PetriNet> petriNetOptional = importHelper.createNet("user_service_test.xml")
        assert petriNetOptional.isPresent()
        petriNet = petriNetOptional.get()

        dummyRole = processRoleService.findById(petriNet.getRoles().find { it.value.importId == "dummy" }.value.stringId)

        userAuth = authorityService.getOrCreate(Authority.user)
    }

    @Test
    void testCreate() {
        user = createUser()
        assert user != null && userAuth.get_id() != null
    }

    @Test
    void testAssignRole() {
        user = createUser()
        assert user.getProcessRoles().size() == 1
        user = userService.addRole(user, dummyRole.get_id())
        assert user.getProcessRoles().size() == 2 && user.getProcessRoles().stream().anyMatch { it.stringId == dummyRole.stringId }

    }

    @Test
    void testRemoveAllByStateAndExpirationDateBefore() {
        user = createUser()
        user.setState(UserState.INACTIVE)
        ((User) user).setExpirationDate(LocalDateTime.now())
        userService.saveUser(user, null)

        userService.removeAllByStateAndExpirationDateBefore(UserState.INACTIVE, LocalDateTime.now(), null)
        assertNull(userService.findById(user.stringId, null))
    }

    @Test
    void findAllByProcessRoles__idIn() {
        user = createUser()

        assert user.getProcessRoles().size() == 1
        user = userService.addRole(user, dummyRole.get_id())
        assert user.getProcessRoles().size() == 2 && user.getProcessRoles().stream().anyMatch { it.stringId == dummyRole.stringId }

        List<AbstractUser> userList = userService.findAllByProcessRoles(Set.of(dummyRole.get_id()), null, Pageable.unpaged())
        assert userList.size() == 1 && userList.getFirst().stringId == user.stringId
    }

    @Test
    void searchAllCoMembersByProcessRoleIdsWithinSharedGroups() {
        User requester = createUser("requester@netgrif.com")
        User sharedGroupMember = createUser("shared-group-member@netgrif.com")
        User otherGroupMember = createUser("other-group-member@netgrif.com")

        def defaultSystemGroup = groupService.getDefaultSystemGroup()
        groupService.removeUser(defaultSystemGroup, requester)
        groupService.removeUser(defaultSystemGroup, sharedGroupMember)
        groupService.removeUser(defaultSystemGroup, otherGroupMember)
        groupService.addUser(groupService.getDefaultUserGroup(requester), sharedGroupMember)

        sharedGroupMember = (User) userService.addRole(sharedGroupMember, dummyRole.get_id())
        otherGroupMember = (User) userService.addRole(otherGroupMember, dummyRole.get_id())
        def loggedUser = ActorTransformer.toLoggedUser(requester)

        Page<AbstractUser> usersWithRole = userService.searchAllCoMembers(
                null, Set.of(dummyRole.get_id()), Collections.emptySet(), loggedUser, Pageable.unpaged())
        assert usersWithRole.content.any { it.stringId == sharedGroupMember.stringId }
        assert usersWithRole.content.every { it.stringId != otherGroupMember.stringId }
        assert usersWithRole.content.every { it.stringId != requester.stringId }

        Page<AbstractUser> usersWithoutRole = userService.searchAllCoMembers(
                null, Collections.emptySet(), Set.of(dummyRole.get_id()), loggedUser, Pageable.unpaged())
        assert usersWithoutRole.content.any { it.stringId == requester.stringId }
        assert usersWithoutRole.content.every { it.stringId != sharedGroupMember.stringId }
        assert usersWithoutRole.content.every { it.stringId != otherGroupMember.stringId }
    }

    @Test
    void findAllByStateAndExpirationDateBefore() {
        user = createUser()
        user.setState(UserState.INACTIVE)
        ((User) user).setExpirationDate(LocalDateTime.now())
        userService.saveUser(user, null)

        List<AbstractUser> userList = userService.findAllByStateAndExpirationDateBefore(UserState.INACTIVE, LocalDateTime.now(), null, Pageable.unpaged())
        assert userList.size() == 1 && userList.getFirst().stringId == user.stringId
    }

    @Test
    void findAllByIdInAndState() {
        user = createUser()
        user.setState(UserState.ACTIVE)
        ((User) user).setExpirationDate(LocalDateTime.now())
        userService.saveUser(user, null)

        Page<AbstractUser> userPage = userService.findAllCoMembers(ActorTransformer.toLoggedUser(user), Pageable.ofSize(10))
        assert userPage.getContent().size() == 2
        assert userPage.getContent().stream().anyMatch(user -> user.getStringId() != null && !user.getStringId().isEmpty());
        assert userPage.content.any { it.stringId == user.stringId }
    }

    private User createUser() {
        return createUser("dummy@netgrif.com")
    }

    private User createUser(String username) {
        Optional<AbstractUser> userOptional = userService.findUserByUsername(username, null)
        User user = null
        if (userOptional.isEmpty()) {
            user = new com.netgrif.application.engine.adapter.spring.auth.domain.User()
            user.setFirstName("Dummy")
            user.setLastName("User")
            user.setUsername(username)
            user.setEmail(username)
            user.setPassword("password")
            user.setState(UserState.ACTIVE)
            user.setAuthoritySet([userAuth] as Set)
            user = (User) userService.createUser(user, null)
        }
        return user
    }
}
