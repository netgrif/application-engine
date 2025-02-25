package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(90)
@RequiredArgsConstructor
public class FunctionsCacheRunner implements ApplicationEngineStartupRunner {

    private final IPetriNetService petriNetService;
    private final IFieldActionsCacheService cacheService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Namespace function caching started");
        petriNetService.getAll().forEach(cacheService::cachePetriNetFunctions);
    }

}
