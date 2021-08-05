package com.netgrif.workflow.startup

import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.workflow.service.interfaces.IFieldActionsCacheService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class StaticFunctionsCacheRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IPetriNetService petriNetService

    private IFieldActionsCacheService cacheService

    @Override
    void run(String... args) throws Exception {
        petriNetService.getAll().each {
            cacheService.cachePetriNetFunctions(it)
        }
    }
}
