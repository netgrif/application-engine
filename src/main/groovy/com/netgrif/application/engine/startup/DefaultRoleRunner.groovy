package com.netgrif.application.engine.startup

import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.events.Event
import com.netgrif.application.engine.petrinet.domain.events.EventType
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
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

        def role = repository.findAllByName_DefaultValue(ProcessRole.DEFAULT_ROLE)
        if (role) {
            log.info("Default role already exists")
            return
        }

        ProcessRole defaultRole = new ProcessRole(
                importId: ProcessRole.DEFAULT_ROLE,
                name: new I18nString(ProcessRole.DEFAULT_ROLE),
                description: "Default system process role",
                events: new LinkedHashMap<EventType, Event>()
        )
        defaultRole = repository.save(defaultRole)

        if (defaultRole == null)
            log.error("Error saving default process role")
    }
}
