package com.netgrif.workflow.startup

import com.netgrif.workflow.petrinet.domain.I18nString
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!update")
class DefaultRoleRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultRoleRunner.class)

    @Autowired
    private ProcessRoleRepository repository

    @Override
    void run(String... strings) throws Exception {
        log.info("Creating default process role")

        def role = repository.findByName_DefaultValue(ProcessRole.DEFAULT_ROLE)
        if (role) {
            log.info("Default role already exists")
            return
        }

        ProcessRole defaultRole = new ProcessRole(
                importId: "0",
                name: new I18nString(ProcessRole.DEFAULT_ROLE),
                description: "Default system process role"
        )
        defaultRole = repository.save(defaultRole)

        if (defaultRole == null)
            log.error("Error saving default process role")
    }
}
