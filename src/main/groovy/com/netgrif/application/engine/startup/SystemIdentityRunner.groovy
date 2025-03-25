package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authentication.domain.Identity
import com.netgrif.application.engine.authentication.domain.LoggedIdentity
import com.netgrif.application.engine.authentication.domain.constants.SystemIdentityConstants
import com.netgrif.application.engine.authentication.domain.params.IdentityParams
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@CompileStatic
@ConditionalOnProperty(value = "admin.create-system-identity", matchIfMissing = true)
class SystemIdentityRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IIdentityService service
    private Identity systemIdentity

    @Override
    void run(String... strings) throws Exception {
        Optional<Identity> systemOpt = service.findByUsername(SystemIdentityConstants.USERNAME)
        if (systemOpt.isPresent()) {
            this.systemIdentity = systemOpt.get()
            return
        }
        this.systemIdentity = createSystemIdentityWithActor()
    }

    private Identity createSystemIdentityWithActor() {
        return service.createWithDefaultActor(IdentityParams.with()
                .username(new TextField(SystemIdentityConstants.USERNAME))
                .firstname(new TextField(SystemIdentityConstants.FIRSTNAME))
                .lastname(new TextField(SystemIdentityConstants.LASTNAME))
                .password(new TextField("n/a")) // todo 2058 test if login is denied
                .build())
    }

    LoggedIdentity getLoggedSystem() {
        return this.systemIdentity.toSession()
    }



}
