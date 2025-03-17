package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authentication.domain.IUser
import com.netgrif.application.engine.authentication.domain.Identity
import com.netgrif.application.engine.authentication.service.interfaces.IUserService
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = "admin.create-system-user", matchIfMissing = true)
@CompileStatic
class SystemUserRunner extends AbstractOrderedCommandLineRunner {

    public static final String SYSTEM_USER_EMAIL = "engine@netgrif.com"
    public static final String SYSTEM_USER_NAME = "application"
    public static final String SYSTEM_USER_SURNAME = "engine"

    @Autowired
    private IUserService service

    private IUser systemUser

    IUser createSystemUser() {
        return service.createSystemUser()
    }

    Identity getLoggedSystem() {
        return this.systemUser.transformToLoggedUser()
    }

    @Override
    void run(String... strings) throws Exception {
        this.systemUser = createSystemUser()
    }

}
