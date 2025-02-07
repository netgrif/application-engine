package com.netgrif.application.engine.auth

import com.netgrif.application.engine.TestHelper
import com.netgrif.core.auth.domain.IUser
import com.netgrif.core.auth.domain.LoggedUser
import com.netgrif.core.auth.domain.PasswordCredential
import com.netgrif.core.auth.domain.User
import com.netgrif.application.engine.auth.service.UserDetailsServiceImpl
import com.netgrif.adapter.auth.service.UserService
import com.netgrif.core.petrinet.domain.PetriNet
import com.netgrif.core.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.adapter.petrinet.service.PetriNetService
import com.netgrif.adapter.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.security.service.ISecurityContextService
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class SecurityContextTest {

    @Autowired
    private ISecurityContextService securityContextService

    @Autowired
    private UserDetailsServiceImpl userDetailsService

    @Autowired
    private ActionDelegate delegate

    @Autowired
    private ProcessRoleService processRoleService

    @Autowired
    private TestHelper testHelper

    @Autowired
    private UserService userService

    @Autowired
    private PetriNetService petriNetService

    @Autowired
    private SuperCreatorRunner superCreator

    private IUser user

    private PetriNet net


    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        user = new User()
        user.setUsername('test@email.com')
        user.setEmail('test@email.com')
        user.addCredential(new PasswordCredential('password', 0, true))
        user = userService.saveUser(user, null)
        assert user != null

        net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, userService.transformToLoggedUser(userService.getLoggedOrSystem())).getNet()
        assert net != null
    }

    @Test
    void addRole() {
        Set<String> roleIds = net.getRoles().keySet()

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userService.transformToLoggedUser(user), userService.transformToLoggedUser(user).getPassword(), userService.transformToLoggedUser(user).getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token)

        // situation 1
        processRoleService.assignRolesToUser(user.getStringId(), roleIds, superCreator.getLoggedSuper())
        def updatedUser = userService.findById(user.getStringId(), null)
        assert ((LoggedUser) SecurityContextHolder.getContext().authentication.principal).getProcessRoles() != updatedUser.getProcessRoles().stream().map(r -> r.getStringId()).collect(Collectors.toSet())

        securityContextService.reloadSecurityContext(userService.transformToLoggedUser(updatedUser))
        assert ((LoggedUser) SecurityContextHolder.getContext().authentication.principal).getProcessRoles() == updatedUser.getProcessRoles().stream().map(r -> r.getStringId()).collect(Collectors.toSet())

        // situation 2
        processRoleService.assignRolesToUser(user.getStringId(), Collections.singleton(roleIds.getAt(0)), superCreator.getLoggedSuper())
        updatedUser = userService.findById(user.getStringId(), null)
        assert ((LoggedUser) SecurityContextHolder.getContext().authentication.principal).getProcessRoles() != updatedUser.getProcessRoles().stream().map(r -> r.getStringId()).collect(Collectors.toSet())

        securityContextService.forceReloadSecurityContext((LoggedUser) SecurityContextHolder.getContext().authentication.principal)
        assert ((LoggedUser) SecurityContextHolder.getContext().authentication.principal).getProcessRoles() == updatedUser.getProcessRoles().stream().map(r -> r.getStringId()).collect(Collectors.toSet())
    }
}
