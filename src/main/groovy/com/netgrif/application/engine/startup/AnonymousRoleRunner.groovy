package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authorization.domain.ProcessRole
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.importer.model.RoleEventType
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.events.RoleEvent
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
@CompileStatic
class AnonymousRoleRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IRoleService roleService

    @Override
    void run(String... strings) throws Exception {
        log.info("Creating anonymous process role")

        def role = roleService.findProcessRolesByDefaultTitle(ProcessRole.ANONYMOUS_ROLE)
        if (role && !role.isEmpty()) {
            log.info("Anonymous role already exists")
            return
        }

        ProcessRole anonymousRole = new ProcessRole(ProcessRole.ANONYMOUS_ROLE)
        anonymousRole.title = new I18nString(ProcessRole.ANONYMOUS_ROLE)
        anonymousRole.description = new I18nString("Anonymous system process role")
        anonymousRole.events = new LinkedHashMap<RoleEventType, RoleEvent>()

        if (roleService.save(anonymousRole) == null)
            log.error("Error saving anonymous process role")
    }
}
