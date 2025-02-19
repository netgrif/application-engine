package com.netgrif.application.engine.auth

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.IUser
import com.netgrif.application.engine.auth.domain.LoggedUser
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.service.UserDetailsServiceImpl
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IRoleService
import com.netgrif.application.engine.security.service.ISecurityContextService
import com.netgrif.application.engine.startup.SuperCreator
import groovy.transform.CompileStatic
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
@CompileStatic
class SecurityContextTest {

    @Autowired
    private ISecurityContextService securityContextService

    @Autowired
    private UserDetailsServiceImpl userDetailsService

    @Autowired
    private ActionDelegate delegate

    @Autowired
    private IRoleService roleService

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IUserService userService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    private IUser user

    private Process net

    @BeforeEach
    void before() {
        testHelper.truncateDbs()

        user = userService.save(new User('test@email.com', 'password', 'Test', 'User'))
        assert user != null

        net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
        assert net != null
    }

    @Test
    void addRole() {
        // TODO: release/8.0.0 fix
//        Set<String> roleIds = net.getRoles().keySet()
        Set<String> roleIds = []

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.transformToLoggedUser(), user.transformToLoggedUser().getPassword(), user.transformToLoggedUser().getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token)

        // situation 1
        roleService.assignRolesToUser(user.getStringId(), roleIds, superCreator.getLoggedSuper())
        IUser updatedUser = userService.findById(user.getStringId())
        Set<String> updatedUserRoles = updatedUser.getRoles().stream().map(r -> r.getStringId()).collect(Collectors.toSet())
        assert ((LoggedUser) SecurityContextHolder.getContext().authentication.principal).getRoles() != updatedUserRoles

        securityContextService.reloadSecurityContext(updatedUser.transformToLoggedUser())
        updatedUserRoles = updatedUser.getRoles().stream().map(r -> r.getStringId()).collect(Collectors.toSet())
        assert ((LoggedUser) SecurityContextHolder.getContext().authentication.principal).getRoles() == updatedUserRoles

        // TODO: release/8.0.0 securityContextService.reloadSecurityContext(updatedUser.transformToLoggedUser())
        //        assert ((LoggedUser) SecurityContextHolder.getContext().authentication.principal).getProcessRoles() == updatedUser.getProcessRoles().stream().map(r -> r.getStringId()).collect(Collectors.toSet())
        //
        //        // situation 2
        //        processRoleService.assignRolesToUser(user.getStringId(), Collections.singleton(roleIds.getAt(0)), superCreator.getLoggedSuper())
        //        updatedUser = userService.findById(user.getStringId(), false)
        //        assert ((LoggedUser) SecurityContextHolder.getContext().authentication.principal).getProcessRoles() != updatedUser.getProcessRoles().stream().map(r -> r.getStringId()).collect(Collectors.toSet())
        //
        //        securityContextService.forceReloadSecurityContext((LoggedUser) SecurityContextHolder.getContext().authentication.principal)
        //        assert ((LoggedUser) SecurityContextHolder.getContext().authentication.principal).getProcessRoles() == updatedUser.getProcessRoles().stream().map(r -> r.getStringId()).collect(Collectors.toSet())
    }
}
