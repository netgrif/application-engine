package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.petrinet.domain.workspace.DefaultWorkspaceService;
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

    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Creating anonymous process role");
        Set<ProcessRole> role = repository.findAllByImportIdAndWorkspaceId(ProcessRole.ANONYMOUS_ROLE, userService.getLoggedOrSystem().getWorkspaceId());
        if (role != null && !role.isEmpty()) {
            log.info("Anonymous role already exists");
            return;
        }

        ProcessRole anonymousRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        anonymousRole.setImportId(ProcessRole.ANONYMOUS_ROLE);
        anonymousRole.setName(new I18nString(ProcessRole.ANONYMOUS_ROLE));
        anonymousRole.setDescription("Anonymous system process role");
        anonymousRole.setEvents(new LinkedHashMap<EventType, Event>());
        anonymousRole.setWorkspaceId(DefaultWorkspaceService.DEFAULT_WORKSPACE_ID);
        repository.save(anonymousRole);
    }

}
