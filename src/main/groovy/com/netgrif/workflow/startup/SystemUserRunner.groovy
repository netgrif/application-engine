package com.netgrif.workflow.startup

import com.netgrif.workflow.auth.domain.IUser
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.auth.domain.repositories.UserRepository
import com.netgrif.workflow.configuration.properties.NaeOAuthProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@ConditionalOnProperty(value = "admin.create-system-user", matchIfMissing = true)
@Component
class SystemUserRunner extends AbstractOrderedCommandLineRunner {

    public static final String SYSTEM_USER_EMAIL = "engine@netgrif.com"
    public static final String SYSTEM_USER_NAME = "application"
    public static final String SYSTEM_USER_SURNAME = "engine"

    @Autowired
    protected NaeOAuthProperties oAuthProperties

    @Autowired
    private UserRepository repository

    private IUser systemUser

    @Override
    void run(String... strings) throws Exception {
        this.systemUser = createSystemUser()
    }

    IUser createSystemUser() {
        def system = repository.findByEmail(SYSTEM_USER_EMAIL)
        if (system == null) {
            system = new User(
                    email: SYSTEM_USER_EMAIL,
                    name: SYSTEM_USER_NAME,
                    surname: SYSTEM_USER_SURNAME,
                    password: "n/a",
                    state: UserState.ACTIVE
            )
            repository.save(system)
        }
        return system
    }

}