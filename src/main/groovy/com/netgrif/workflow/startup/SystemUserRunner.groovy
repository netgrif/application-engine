package com.netgrif.workflow.startup

import com.netgrif.workflow.auth.domain.IUser
import com.netgrif.workflow.auth.domain.LoggedUser
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.configuration.properties.NaeOAuthProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = "admin.create-system-user", matchIfMissing = true)
class SystemUserRunner extends AbstractOrderedCommandLineRunner {

    public static final String SYSTEM_USER_EMAIL = "engine@netgrif.com"
    public static final String SYSTEM_USER_NAME = "application"
    public static final String SYSTEM_USER_SURNAME = "engine"

    @Autowired
    protected NaeOAuthProperties oAuthProperties

    @Autowired
    private IUserService service

    private IUser systemUser

    IUser createSystemUser() {
        return service.createSystemUser()
    }

    LoggedUser getLoggedSystem() {
        return this.systemUser.transformToLoggedUser()
    }

    @Override
    void run(String... strings) throws Exception {
        this.systemUser = createSystemUser()
    }

}
