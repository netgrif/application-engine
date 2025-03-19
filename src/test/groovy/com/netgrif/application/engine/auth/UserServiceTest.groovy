package com.netgrif.application.engine.auth

import com.netgrif.adapter.auth.domain.mapper.LoggedUserMapper
import com.netgrif.adapter.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.auth.service.AuthorityService
import com.netgrif.auth.service.UserService
import com.netgrif.core.auth.domain.Authority
import com.netgrif.core.auth.domain.IUser
import com.netgrif.core.auth.domain.User
import com.netgrif.core.auth.domain.enums.UserState
import com.netgrif.core.petrinet.domain.PetriNet
import com.netgrif.core.petrinet.domain.VersionType
import com.netgrif.core.petrinet.domain.roles.ProcessRole
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

import static org.junit.jupiter.api.Assertions.assertThrows

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
    private ProcessRoleService processRoleService;

    @Autowired
    private ImportHelper importHelper

    private PetriNet petriNet

    private ProcessRole dummyRole

    private Authority userAuth

    private IUser user

    @BeforeEach
    void init() {
        helper.truncateDbs()

        Optional<PetriNet> petriNetOptional = importHelper.createNet("user_service_test.xml")
        assert petriNetOptional.isPresent()
        petriNet = petriNetOptional.get()

        dummyRole = processRoleService.findById(petriNet.getRoles().find {it.value.importId == "dummy"}.value.stringId)

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
        assert user.getProcessRoles().size() == 2 && user.getProcessRoles().stream().anyMatch {it.stringId == dummyRole.stringId}

    }

    @Test
    void testRemoveAllByStateAndExpirationDateBefore() {
        user = createUser()
        user.setState(UserState.INACTIVE)
        ((User) user).setExpirationDate(LocalDateTime.now())
        userService.saveUser(user, null)

        userService.removeAllByStateAndExpirationDateBefore(UserState.INACTIVE, LocalDateTime.now(), null)
        assertThrows(IllegalArgumentException.class, () -> {
            userService.findById(user.stringId, null)
        })
    }

    @Test
    void findAllByProcessRoles__idIn() {
        user = createUser()

        assert user.getProcessRoles().size() == 1
        user = userService.addRole(user, dummyRole.get_id())
        assert user.getProcessRoles().size() == 2 && user.getProcessRoles().stream().anyMatch {it.stringId == dummyRole.stringId}

        List<IUser> userList = userService.findAllByProcessRoles(Set.of(dummyRole.get_id()), null)
        assert userList.size() == 1 && userList.getFirst().stringId == user.stringId
    }

    @Test
    void findAllByStateAndExpirationDateBefore() {
        user = createUser()
        user.setState(UserState.INACTIVE)
        ((User) user).setExpirationDate(LocalDateTime.now())
        userService.saveUser(user, null)

        List<IUser> userList = userService.findAllByStateAndExpirationDateBefore(UserState.INACTIVE, LocalDateTime.now(), null)
        assert userList.size() == 1 && userList.getFirst().stringId == user.stringId
    }

    @Test
    void findAllByIdInAndState() {
        user = createUser()
        user.setState(UserState.ACTIVE)
        ((User) user).setExpirationDate(LocalDateTime.now())
        userService.saveUser(user, null)

        Page<IUser> userPage = userService.findAllCoMembers(LoggedUserMapper.toLoggedUser(user as User), Pageable.ofSize(10))
        assert userPage.getContent().size() == 2 && userPage.getContent().getFirst().stringId == user.stringId

    }

    private IUser createUser() {
        Optional<IUser> userOptional = userService.findUserByUsername("dummy@netgrif.com", null)
        User user = null
        if (userOptional.isEmpty()) {
            user = new com.netgrif.adapter.auth.domain.User()
            user.setFirstName("Dummy")
            user.setLastName("User")
            user.setUsername("dummy@netgrif.com")
            user.setEmail("dummy@netgrif.com")
            user.setPassword("password")
            user.setState(UserState.ACTIVE)
            user.setAuthorities([userAuth] as Set)
            user = (User) userService.createUser(user, null)
        }
        return user
    }
}