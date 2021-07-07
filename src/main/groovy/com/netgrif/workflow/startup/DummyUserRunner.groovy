package com.netgrif.workflow.startup


import com.netgrif.workflow.auth.service.interfaces.IUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@ConditionalOnProperty(value = "runner.user", matchIfMissing = true)
@Profile("dev")
@Component
class DummyUserRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IUserService userService

    @Autowired
    protected NaeOAuthProperties oAuthProperties

    @Override
    void run(String... strings) throws Exception {
        return
    }
}
