package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.application.engine.objects.petrinet.domain.roles.PredefinedProcessRole;
import com.netgrif.application.engine.objects.petrinet.domain.workspace.DefaultWorkspaceService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!update")
@RunnerOrder(40)
@RequiredArgsConstructor
public class DefaultRoleRunner implements ApplicationEngineStartupRunner {

    private final ProcessRoleService processRoleService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        processRoleService.createDefaultOrAnonymousRole(PredefinedProcessRole.DEFAULT_ROLE, DefaultWorkspaceService.DEFAULT_WORKSPACE_ID);
    }

}
