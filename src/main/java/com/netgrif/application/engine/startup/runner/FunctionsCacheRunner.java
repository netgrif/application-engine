package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.AbstractOrderedApplicationRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(9)
@RequiredArgsConstructor
public class FunctionsCacheRunner extends AbstractOrderedApplicationRunner {

    private final IPetriNetService petriNetService;
    private final IFieldActionsCacheService cacheService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Namespace function caching started");
        petriNetService.getAll().forEach(cacheService::cachePetriNetFunctions);
    }

}
