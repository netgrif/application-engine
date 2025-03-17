package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authorization.domain.ProcessRole
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.importer.model.RoleEventType
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.events.RoleEvent
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
    private IRoleService roleService

    @Override
    void run(String... strings) throws Exception {
        log.info("Creating default process role")

        def role = roleService.findProcessRolesByDefaultTitle(ProcessRole.DEFAULT_ROLE)
        if (role) {
            log.info("Default role already exists")
            return
        }

        ProcessRole defaultRole = new ProcessRole(ProcessRole.DEFAULT_ROLE)
        defaultRole.title = new I18nString(ProcessRole.DEFAULT_ROLE)
        defaultRole.description = new I18nString("Default system process role")
        defaultRole.events = new LinkedHashMap<RoleEventType, RoleEvent>()

        if (roleService.save(defaultRole) == null) {
            log.error("Error saving default process role")
        }
    }
}
