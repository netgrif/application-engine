package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.objects.petrinet.domain.workspace.DefaultWorkspaceService;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(100)
@RequiredArgsConstructor
public class DefaultNetImportRunner implements ApplicationEngineStartupRunner {

    private final IPetriNetService petriNetService;
    private final DefaultWorkspaceService defaultWorkspaceService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        petriNetService.importDefaultProcesses(defaultWorkspaceService.getDefaultWorkspace().getId());
    }
}
