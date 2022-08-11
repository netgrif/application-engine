package com.netgrif.application.engine.startup

import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.events.Event
import com.netgrif.application.engine.petrinet.domain.events.EventType
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class AnonymousRoleRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private ProcessRoleRepository repository

    @Override
    void run(String... strings) throws Exception {
        log.info("Creating anonymous process role")

        def role = repository.findAllByImportId(ProcessRole.ANONYMOUS_ROLE)
        if (role && !role.isEmpty()) {
            log.info("Anonymous role already exists")
            return
        }

        ProcessRole anonymousRole = new ProcessRole(
                importId: ProcessRole.ANONYMOUS_ROLE,
                name: new I18nString(ProcessRole.ANONYMOUS_ROLE),
                description: "Anonymous system process role",
                events: new LinkedHashMap<EventType, Event>()
        )
        anonymousRole = repository.save(anonymousRole)

        if (anonymousRole == null)
            log.error("Error saving anonymous process role")
    }
}
