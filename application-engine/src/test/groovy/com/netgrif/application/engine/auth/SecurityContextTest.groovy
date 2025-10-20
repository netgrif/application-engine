package com.netgrif.application.engine.auth

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.objects.auth.domain.LoggedUser
import com.netgrif.application.engine.objects.auth.domain.PasswordCredential
import com.netgrif.application.engine.auth.service.UserDetailsServiceImpl
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.objects.auth.domain.User
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.security.service.ISecurityContextService
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId
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
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreatorRunner superCreator

    private User user

    private PetriNet net


    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        user = new User()
        user.setUsername('test@email.com')
        user.setEmail('test@email.com')
        user.setCredential("password", new PasswordCredential('password', 0, true))
        user = userService.saveUser(user, null)
        assert user != null

        net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/all_data.xml"))
                .releaseType(VersionType.MAJOR)
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getNet()
        assert net != null
    }

    @Test
    void addRole() {
        Set<ProcessResourceId> roleIds = net.getRoles().values().stream().collect() {it -> it._id}

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(ActorTransformer.toLoggedUser(user), ActorTransformer.toLoggedUser(user).getPassword(), ActorTransformer.toLoggedUser(user).getAuthoritySet() as Set<AuthorityImpl>);
        SecurityContextHolder.getContext().setAuthentication(token)

        // situation 1
        processRoleService.assignRolesToUser(user, roleIds, superCreator.getLoggedSuper())
        def updatedUser = userService.findById(user.getStringId(), null)
        assert ((LoggedUser) SecurityContextHolder.getContext().authentication.principal).getProcessRoles().collect {it.stringId} != updatedUser.getProcessRoles().stream().map(r -> r.getStringId()).collect(Collectors.toSet())

        securityContextService.reloadSecurityContext(ActorTransformer.toLoggedUser(updatedUser))
        assert ((LoggedUser) SecurityContextHolder.getContext().authentication.principal).getProcessRoles().collect {it.stringId} as Set == updatedUser.getProcessRoles().stream().map(r -> r.getStringId()).collect(Collectors.toSet())

        // situation 2
        processRoleService.assignRolesToUser(user, Collections.singleton(roleIds.getAt(0)), superCreator.getLoggedSuper())
        updatedUser = userService.findById(user.getStringId(), null)
        assert ((LoggedUser) SecurityContextHolder.getContext().authentication.principal).getProcessRoles().collect {it.stringId} as Set  != updatedUser.getProcessRoles().stream().map(r -> r.getStringId()).collect(Collectors.toSet())

        securityContextService.forceReloadSecurityContext((LoggedUser) SecurityContextHolder.getContext().authentication.principal)
        assert ((LoggedUser) SecurityContextHolder.getContext().authentication.principal).getProcessRoles().collect {it.stringId} as Set  == updatedUser.getProcessRoles().stream().map(r -> r.getStringId()).collect(Collectors.toSet())
    }
}
