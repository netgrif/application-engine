package com.netgrif.application.engine.startup

import com.netgrif.application.engine.importer.model.EventType
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.events.Event
import com.netgrif.application.engine.petrinet.domain.roles.Role
import com.netgrif.application.engine.petrinet.domain.roles.RoleRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
@CompileStatic
class AnonymousRoleRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private RoleRepository repository

    @Override
    void run(String... strings) throws Exception {
        log.info("Creating anonymous process role")

        def role = repository.findAllByImportId(Role.ANONYMOUS_ROLE)
        if (role && !role.isEmpty()) {
            log.info("Anonymous role already exists")
            return
        }

        Role anonymousRole = new Role(
                importId: Role.ANONYMOUS_ROLE,
                name: new I18nString(Role.ANONYMOUS_ROLE),
                description: new I18nString("Anonymous system process role"),
                events: new LinkedHashMap<EventType, Event>()
        )
        anonymousRole = repository.save(anonymousRole)

        if (anonymousRole == null)
            log.error("Error saving anonymous process role")
    }
}
