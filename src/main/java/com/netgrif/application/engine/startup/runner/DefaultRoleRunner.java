package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.events.Event;
import com.netgrif.application.engine.petrinet.domain.events.EventType;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.application.engine.startup.AbstractOrderedApplicationRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Set;

@Slf4j
@Component
@Profile("!update")
@RunnerOrder(4)
@RequiredArgsConstructor
public class DefaultRoleRunner extends AbstractOrderedApplicationRunner {

    private final ProcessRoleRepository repository;

    @Override
    public void apply(ApplicationArguments args) throws Exception {
        log.info("Creating default process role");
        Set<ProcessRole> role = repository.findAllByName_DefaultValue(ProcessRole.DEFAULT_ROLE);
        if (role != null && !role.isEmpty()) {
            log.info("Default role already exists");
            return;
        }

        ProcessRole defaultRole = new ProcessRole();
        defaultRole.setImportId(ProcessRole.DEFAULT_ROLE);
        defaultRole.setName(new I18nString(ProcessRole.DEFAULT_ROLE));
        defaultRole.setDescription("Default system process role");
        defaultRole.setEvents(new LinkedHashMap<EventType, Event>());
        defaultRole = repository.save(defaultRole);

        if (defaultRole == null) {
            log.error("Error saving default process role");
        }
    }

}
