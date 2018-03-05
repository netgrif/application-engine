package com.netgrif.workflow.startup

import com.netgrif.workflow.petrinet.domain.I18nString
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DefaultRoleRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = Logger.getLogger(DefaultRoleRunner.class)

    @Autowired
    private ProcessRoleRepository repository

    @Override
    void run(String... strings) throws Exception {
        log.info("Creating default process role")

        ProcessRole defaultRole = new ProcessRole(
                name: new I18nString(ProcessRole.DEFAULT_ROLE),
                description: "Default system process role"
        )
        defaultRole = repository.save(defaultRole)

        if (defaultRole == null)
            log.error("Error saving default process role")
    }
}
