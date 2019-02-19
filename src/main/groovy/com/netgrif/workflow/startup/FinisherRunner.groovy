package com.netgrif.workflow.startup


import com.netgrif.workflow.petrinet.service.PetriNetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@ConditionalOnProperty(value = "admin.create-super", matchIfMissing = true)
@Component
class FinisherRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private PetriNetService petriNetService

    @Override
    void run(String... strings) throws Exception {
        superCreator.setAllToSuperUser()
    }
}
