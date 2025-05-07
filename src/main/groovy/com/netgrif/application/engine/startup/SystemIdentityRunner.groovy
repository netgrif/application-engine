package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authentication.domain.Identity
import com.netgrif.application.engine.authentication.domain.LoggedIdentity
import com.netgrif.application.engine.authentication.domain.constants.SystemIdentityConstants
import com.netgrif.application.engine.authentication.domain.params.IdentityParams
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService
import com.netgrif.application.engine.authorization.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Slf4j
@Component
@CompileStatic
@ConditionalOnProperty(value = "admin.create-system-identity", matchIfMissing = true)
class SystemIdentityRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IIdentityService identityService

    @Lazy
    @Autowired
    private IUserService userService
    
    private Identity systemIdentity

    @Override
    void run(String... strings) throws Exception {
        Optional<Identity> systemOpt = identityService.findByUsername(SystemIdentityConstants.USERNAME)
        if (systemOpt.isPresent()) {
            this.systemIdentity = systemOpt.get()
            return
        }
        this.systemIdentity = createSystemIdentityWithActor()
    }

    private Identity createSystemIdentityWithActor() {
        Identity systemIdentity = identityService.createWithDefaultUser(IdentityParams.with()
                .username(new TextField(SystemIdentityConstants.USERNAME))
                .firstname(new TextField(SystemIdentityConstants.FIRSTNAME))
                .lastname(new TextField(SystemIdentityConstants.LASTNAME))
                .password(new TextField("n/a"))
                .build())

        identityService.registerForbiddenKeywords(Set.of(SystemIdentityConstants.USERNAME))
        userService.registerForbiddenKeywords(Set.of(SystemIdentityConstants.USERNAME))
        log.info("Created system identity [{}] with user [{}].", systemIdentity.stringId, systemIdentity.mainActorId)
        return systemIdentity
    }

    LoggedIdentity getLoggedSystem() {
        return this.systemIdentity.toSession()
    }
}
