package com.netgrif.application.engine.startup

import com.netgrif.application.engine.importer.model.EventType
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.events.Event
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Slf4j
@Component
@Profile("!update")
@CompileStatic
class DefaultRoleRunner extends AbstractOrderedCommandLineRunner {

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

        if (defaultRole == null) {
            log.error("Error saving default process role")
        }
    }
}
