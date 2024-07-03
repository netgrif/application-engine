package com.netgrif.application.engine.startup

import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
class FunctionsCacheRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IFieldActionsCacheService cacheService

    @Override
    void run(String... args) throws Exception {
        log.info("Namespace function caching started")
        
        petriNetService.getAll().each {
            cacheService.cachePetriNetFunctions(it)
        }
    }
}
