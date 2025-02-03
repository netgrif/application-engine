package com.netgrif.application.engine.startup.runner;

import com.netgrif.core.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.events.Event;
import com.netgrif.application.engine.petrinet.domain.events.EventType;
import com.netgrif.core.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Set;

@Slf4j
@Component
@RunnerOrder(50)
@RequiredArgsConstructor
public class AnonymousRoleRunner implements ApplicationEngineStartupRunner {

    private final ProcessRoleRepository repository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Creating anonymous process role");
        Set<ProcessRole> role = repository.findAllByImportId(ProcessRole.ANONYMOUS_ROLE);
        if (role != null && !role.isEmpty()) {
            log.info("Anonymous role already exists");
            return;
        }

        ProcessRole anonymousRole = new ProcessRole();
        anonymousRole.setImportId(ProcessRole.ANONYMOUS_ROLE);
        anonymousRole.setName(new I18nString(ProcessRole.ANONYMOUS_ROLE));
        anonymousRole.setDescription("Anonymous system process role");
        //MODULARISATION: events to be resolved
//        anonymousRole.setEvents(new LinkedHashMap<EventType, Event>());
        anonymousRole = repository.save(anonymousRole);
    }

}
