package com.netgrif.application.engine.startup

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Slf4j
@Component
@ConditionalOnProperty(value = "admin.create-super", matchIfMissing = true)
@CompileStatic
class FinisherRunnerSuperCreator extends AbstractOrderedCommandLineRunner {

    @Autowired
    private SuperCreator superCreator

    @Override
    void run(String... strings) throws Exception {
        superCreator.setAllToSuperUser()
    }
}