package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authentication.domain.Authority
import com.netgrif.application.engine.authentication.domain.Identity
import com.netgrif.application.engine.authentication.domain.LoggedIdentity
import com.netgrif.application.engine.authentication.domain.constants.AnonymIdentityConstants
import com.netgrif.application.engine.authentication.domain.params.IdentityParams
import com.netgrif.application.engine.authentication.service.interfaces.IAuthorityService
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService
import com.netgrif.application.engine.authorization.domain.Role
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Slf4j
@Component
@CompileStatic
@ConditionalOnProperty(value = "admin.create-anonym", matchIfMissing = true)
class AnonymIdentityRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IAuthorityService authorityService

    @Autowired
    private IIdentityService identityService

    @Autowired
    private IRoleService roleService

    private Identity anonymIdentity

    @Override
    void run(String... strings) {
        log.info("Creating Anonymous identity")
        createAnonymIdentity()
    }

    Identity getAnonymIdentity() {
        return this.anonymIdentity
    }

    LoggedIdentity getLoggedAnonym() {
        return this.anonymIdentity.toSession()
    }

    private void createAnonymIdentity() {
        Authority anonymAuthority = authorityService.getOrCreate(Authority.anonymous)

        Optional<Identity> anonymIdentityOpt = identityService.findByUsername(AnonymIdentityConstants.USERNAME)
        if (anonymIdentityOpt.isPresent()) {
            log.info("Anonymous identity detected")
            this.anonymIdentity = anonymIdentityOpt.get()
            return
        }
        this.anonymIdentity = identityService.createWithDefaultActor(IdentityParams.with()
                .username(new TextField(AnonymIdentityConstants.USERNAME))
                .firstname(new TextField(AnonymIdentityConstants.FIRSTNAME))
                .lastname(new TextField(AnonymIdentityConstants.LASTNAME))
                .password(new TextField("n/a"))
                .build())

        Role anonymousRole = roleService.findAnonymousRole()
        roleService.assignRolesToActor(this.anonymIdentity.stringId, Set.of(anonymousRole.stringId))
        // todo 2058 app role (authorities)

        log.info("Super identity created with actor")
    }

}
