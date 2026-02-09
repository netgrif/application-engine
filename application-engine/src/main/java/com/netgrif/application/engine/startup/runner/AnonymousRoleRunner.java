package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import com.netgrif.application.engine.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Slf4j
@Component
@RunnerOrder(70)
@RequiredArgsConstructor
public class AnonymousRoleRunner implements ApplicationEngineStartupRunner {

    private final ProcessRoleService processRoleService;
    private final WorkspaceService workspaceService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Creating anonymous process role");
        Page<ProcessRole> role = processRoleService.findAllByImportId(ProcessRole.ANONYMOUS_ROLE, Pageable.ofSize(1));
        if (role != null && !role.isEmpty()) {
            log.info("Anonymous role already exists");
            return;
        }

        ProcessRole anonymousRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        anonymousRole.setImportId(ProcessRole.ANONYMOUS_ROLE);
        anonymousRole.setName(new I18nString(ProcessRole.ANONYMOUS_ROLE));
        anonymousRole.setDescription("Anonymous system process role");
        anonymousRole.setEvents(new LinkedHashMap<>());
        anonymousRole.setWorkspaceId(workspaceService.getDefault().getId());
        processRoleService.save(anonymousRole);
    }

}
