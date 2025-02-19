package com.netgrif.application.engine.startup

import com.netgrif.application.engine.importer.model.EventType
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.events.Event
import com.netgrif.application.engine.petrinet.domain.roles.Role
import com.netgrif.application.engine.petrinet.domain.roles.RoleRepository
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
    private RoleRepository repository

    @Override
    void run(String... strings) throws Exception {
        log.info("Creating default process role")

        def role = repository.findAllByName_DefaultValue(Role.DEFAULT_ROLE)
        if (role) {
            log.info("Default role already exists")
            return
        }

        Role defaultRole = new Role(
                importId: Role.DEFAULT_ROLE,
                name: new I18nString(Role.DEFAULT_ROLE),
                description: new I18nString("Default system process role"),
                events: new LinkedHashMap<EventType, Event>()
        )
        defaultRole = repository.save(defaultRole)

        if (defaultRole == null) {
            log.error("Error saving default process role")
        }
    }
}
