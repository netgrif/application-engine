package com.netgrif.workflow.startup

import com.netgrif.workflow.petrinet.domain.I18nString
import com.netgrif.workflow.petrinet.domain.events.Event
import com.netgrif.workflow.petrinet.domain.events.EventType
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository
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

        def role = repository.findByName_DefaultValue(ProcessRole.ANONYMOUS_ROLE)
        if (role) {
            log.info("Anonymous role already exists")
            return
        }

        ProcessRole anonymousRole = new ProcessRole(
                importId: "anonymous",
                name: new I18nString(ProcessRole.ANONYMOUS_ROLE),
                description: "Anonymous system process role",
                events: new LinkedHashMap<EventType, Event>()
        )
        anonymousRole = repository.save(anonymousRole)

        if (anonymousRole == null)
            log.error("Error saving anonymous process role")
    }
}
