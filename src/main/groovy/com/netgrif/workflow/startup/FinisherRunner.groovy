package com.netgrif.workflow.startup

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FinisherRunner extends AbstractOrderedCommandLineRunner{

    @Autowired
    private SuperCreator superCreator

    @Override
    void run(String... strings) throws Exception {
        superCreator.setAllToSuperUser()
    }
}
